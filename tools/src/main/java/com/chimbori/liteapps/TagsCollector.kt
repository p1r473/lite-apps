package com.chimbori.liteapps

import com.chimbori.FilePaths.LITE_APPS_SRC_DIR
import com.chimbori.FilePaths.LITE_APPS_TAGS_JSON
import com.chimbori.FilePaths.MANIFEST_JSON_FILE_NAME
import com.chimbori.hermitcrab.schema.common.MoshiAdapter
import com.chimbori.hermitcrab.schema.library.LibraryTag
import com.chimbori.hermitcrab.schema.library.LibraryTagsList
import com.chimbori.hermitcrab.schema.manifest.Manifest
import okio.buffer
import okio.source
import java.io.File

internal object TagsCollector {
  /**
   * Read the list of all known tags from the tags.json file. In case we discover any new tags,
   * we will add them to this file, taking care not to overwrite those that already exist.
   */
  fun updateTagsJson() {
    println("LITE_APPS_TAGS_JSON: ${LITE_APPS_TAGS_JSON.absolutePath}")
    val libraryTagsList = MoshiAdapter.getAdapter(LibraryTagsList::class.java).fromJson(LITE_APPS_TAGS_JSON.source().buffer())
    libraryTagsList!!.updateTransientFields()

    val globalTags = mutableMapOf<String, LibraryTag>()
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

      // For all tags applied to this manifest, check if they exist in the global tags list.
      manifest.tags?.forEach { tagName ->
        require(tagName.isNotEmpty()) { "Tag should not be blank" }
        val tag = globalTags[tagName]
        if (tag == null) {
          // If this is the first time we are seeing this tag, create a new JSONArray to hold its contents.
          val newTag = LibraryTag(tagName)
          globalTags[tagName] = newTag
          libraryTagsList.addTag(newTag)
        }
      }
    }

    LITE_APPS_TAGS_JSON.writeText(MoshiAdapter.getAdapter(LibraryTagsList::class.java).toJson(libraryTagsList))
  }
}
