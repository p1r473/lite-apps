package com.chimbori.liteapps

import com.chimbori.FilePaths.ICONS_DIR_NAME
import com.chimbori.FilePaths.ICON_EXTENSION
import com.chimbori.FilePaths.LIBRARY_ICONS_DIR
import com.chimbori.FilePaths.LIBRARY_ICONS_URL_PREFIX
import com.chimbori.FilePaths.LIBRARY_JSON
import com.chimbori.FilePaths.LITE_APPS_SRC_DIR
import com.chimbori.FilePaths.LITE_APPS_TAGS_JSON
import com.chimbori.FilePaths.MANIFEST_JSON_FILE_NAME
import com.chimbori.hermitcrab.schema.common.MoshiAdapter
import com.chimbori.hermitcrab.schema.library.Library
import com.chimbori.hermitcrab.schema.library.LibraryApp
import com.chimbori.hermitcrab.schema.library.LibraryTagsList
import com.chimbori.hermitcrab.schema.library.LiteAppCategoryWithApps
import com.chimbori.hermitcrab.schema.manifest.DEFAULT_PRIORITY
import com.chimbori.hermitcrab.schema.manifest.IconFile.FAVICON_FILE
import com.chimbori.hermitcrab.schema.manifest.Manifest
import net.coobird.thumbnailator.Thumbnails
import okio.buffer
import okio.source
import java.awt.image.BufferedImage
import java.io.File
import java.net.URLEncoder

/**
 * Generates the Library Data JSON file, which is used as the basis for generating the Hermit Library page at
 * https://lite-apps.chimbori.com/library.
 */
internal object LibraryGenerator {
  private const val LIBRARY_ICON_SIZE = 112

  fun generateLibraryData() {
    // The order of categories displayed in the library is based on the order in [LITE_APPS_TAGS_JSON] file.
    val outputLibrary = Library()
    val categoryMap = mutableMapOf<String, LiteAppCategoryWithApps>()

    MoshiAdapter.getAdapter(LibraryTagsList::class.java)
        .fromJson(LITE_APPS_TAGS_JSON.source().buffer())!!
        .tags.forEach { tag ->
          val categoryWithApps = LiteAppCategoryWithApps(tag)
          categoryMap.put(tag.name, categoryWithApps)
          outputLibrary.categories.add(categoryWithApps)
        }

    LITE_APPS_SRC_DIR.listFiles()?.forEach { liteAppDirectory ->
      if (!liteAppDirectory.isDirectory) {
        return@forEach  // Probably a temporary file, like .DS_Store.
      }

      val manifestJsonFile = File(liteAppDirectory, MANIFEST_JSON_FILE_NAME)
      if (!manifestJsonFile.exists()) {
        return@forEach
      }

      val manifest = MoshiAdapter.getAdapter(Manifest::class.java).fromJson(manifestJsonFile.source().buffer())
          ?: return@forEach

      // Create an entry for this Lite App to be put in the directory index file.
      val outputApp = LibraryApp(
          name = manifest.name,
          theme_color = manifest.theme_color ?: "#ffffff",
          url = manifest.start_url,
          manifestUrl = manifest.manifest_url,
          imageUrl = "${LIBRARY_ICONS_URL_PREFIX}${URLEncoder.encode(liteAppDirectory.name, "utf-8").replace("+", "%20")}$ICON_EXTENSION",
          priority = manifest.priority ?: DEFAULT_PRIORITY)

      manifest.tags?.forEach { tagName ->
        val category = categoryMap.get(tagName)
        if (category == null) {
          throw IllegalStateException("New tag “$tagName” not present in ${LITE_APPS_TAGS_JSON.name}. " +
              "Add manually in the correct position, then re-run this.")
        }
        category.apps.add(outputApp)
      }

      // Resize the icon to be suitable for the Web, and copy it to the Web-accessible icons directory.
      val thumbnailImage = File(LIBRARY_ICONS_DIR, liteAppDirectory.name + ICON_EXTENSION)
      if (!thumbnailImage.exists()) {
        val iconsDirectory = File(liteAppDirectory, ICONS_DIR_NAME)
        Thumbnails.of(File(iconsDirectory, FAVICON_FILE.fileName))
            .outputQuality(1.0f)
            .useOriginalFormat()
            .size(LIBRARY_ICON_SIZE, LIBRARY_ICON_SIZE)
            .imageType(BufferedImage.TYPE_INT_ARGB)
            .toFile(thumbnailImage)
      }
    }

    outputLibrary.categories.forEach { categoryWithApps ->
      categoryWithApps.apps.sortWith(
          compareByDescending<LibraryApp> { app -> app.priority }
              .thenBy { app -> app.name?.toLowerCase() })
    }

    LIBRARY_JSON.writeText(MoshiAdapter.getAdapter(Library::class.java).toJson(outputLibrary))
  }
}
