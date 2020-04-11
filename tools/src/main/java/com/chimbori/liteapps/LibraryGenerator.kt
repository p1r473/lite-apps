package com.chimbori.liteapps

import com.chimbori.FilePaths.ICONS_DIR_NAME
import com.chimbori.FilePaths.ICON_EXTENSION
import com.chimbori.FilePaths.LIBRARY_ICONS_DIR
import com.chimbori.FilePaths.LIBRARY_JSON
import com.chimbori.FilePaths.LITE_APPS_SRC_DIR
import com.chimbori.FilePaths.LITE_APPS_TAGS_JSON
import com.chimbori.FilePaths.MANIFEST_JSON_FILE_NAME
import com.chimbori.hermitcrab.schema.common.MoshiAdapter
import com.chimbori.hermitcrab.schema.library.Library
import com.chimbori.hermitcrab.schema.library.LibraryApp
import com.chimbori.hermitcrab.schema.library.LibraryTagsList
import com.chimbori.hermitcrab.schema.manifest.DEFAULT_PRIORITY
import com.chimbori.hermitcrab.schema.manifest.IconFile.FAVICON_FILE
import com.chimbori.hermitcrab.schema.manifest.Manifest
import net.coobird.thumbnailator.Thumbnails
import okio.buffer
import okio.source
import java.awt.image.BufferedImage
import java.io.File

internal object LibraryGenerator {
  private const val LIBRARY_ICON_SIZE = 112
  private const val USER_AGENT_DESKTOP = "desktop"

  /**
   * Individual manifest.json files do not contain any information about the organization of the
   * Lite Apps in the Library (e.g. categories, order within category, whether it should be
   * displayed or not. This metadata is stored in a separate index.json file. To minimize
   * duplication & to preserve a single source of truth, this file does not contain actual URLs
   * or anything about a Lite App other than its name (same as the directory name).
   *
   * This generator tool combines the basic organizational metadata from index.json & detailed
   * Lite Apps data from * / manifest.json files. It outputs the Library Data JSON file,
   * which is used as the basis for generating the Hermit Library page at
   * https://lite-apps.chimbori.com/library.
   */
  fun generateLibraryData() {
    // Read the list of all known tags from the tags.json file. In case we discover any new tags,
    // we will add them to this file, taking care not to overwrite those that already exist.
    val globalTags = MoshiAdapter.getAdapter(LibraryTagsList::class.java)
        .fromJson(LITE_APPS_TAGS_JSON.source().buffer())
        ?.apply { updateTransientFields() }
        ?: return

    val outputLibrary = Library(globalTags)
    val liteAppDirs = LITE_APPS_SRC_DIR.listFiles()
    liteAppDirs?.forEach { liteAppDirectory ->
      if (!liteAppDirectory.isDirectory) {
        return@forEach  // Probably a temporary file, like .DS_Store.
      }

      val iconsDirectory = File(liteAppDirectory, ICONS_DIR_NAME)
      val appName = liteAppDirectory.name

      val manifestJsonFile = File(liteAppDirectory, MANIFEST_JSON_FILE_NAME)
      if (!manifestJsonFile.exists()) {
        return@forEach
      }

      // Create an entry for this Lite App to be put in the directory index file.
      val manifest = MoshiAdapter.getAdapter(Manifest::class.java).fromJson(manifestJsonFile.source().buffer())
          ?: return@forEach
      val outputApp = LibraryApp(
          name = appName,
          theme_color = manifest.theme_color ?: "#ffffff",
          url = manifest.start_url,
          priority = manifest.priority ?: DEFAULT_PRIORITY)

      // Set user-agent from the settings stored in the Lite App’s manifest.json.
      if (USER_AGENT_DESKTOP == manifest.settings?.user_agent) {
        outputApp.user_agent = USER_AGENT_DESKTOP
      }
      manifest.tags?.let { tags -> outputLibrary.addAppToCategories(outputApp, tags) }

      // Resize the icon to be suitable for the Web, and copy it to the Web-accessible icons directory.
      val thumbnailImage = File(LIBRARY_ICONS_DIR, appName + ICON_EXTENSION)
      if (!thumbnailImage.exists()) {
        Thumbnails.of(File(iconsDirectory, FAVICON_FILE.fileName))
            .outputQuality(1.0f)
            .useOriginalFormat()
            .size(LIBRARY_ICON_SIZE, LIBRARY_ICON_SIZE)
            .imageType(BufferedImage.TYPE_INT_ARGB)
            .toFile(thumbnailImage)
      }
    }
    LIBRARY_JSON.writeText(MoshiAdapter.getAdapter(Library::class.java).toJson(outputLibrary))
  }
}
