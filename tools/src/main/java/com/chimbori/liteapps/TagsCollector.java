package com.chimbori.liteapps;

import com.chimbori.FilePaths;
import com.chimbori.common.FileUtils;
import com.chimbori.hermitcrab.schema.common.MoshiAdapter;
import com.chimbori.hermitcrab.schema.library.LibraryTag;
import com.chimbori.hermitcrab.schema.library.LibraryTagsList;
import com.chimbori.hermitcrab.schema.manifest.Manifest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okio.Okio;

class TagsCollector {
  public static void updateTagsJson() throws IOException {
    // Read the list of all known tags from the tags.json file. In case we discover any new tags,
    // we will add them to this file, taking care not to overwrite those that already exist.
    LibraryTagsList libraryTagsList = MoshiAdapter.get(LibraryTagsList.class)
        .fromJson(Okio.buffer(Okio.source(FilePaths.LITE_APPS_TAGS_JSON)))
        .updateTransientFields();

    Map<String, LibraryTag> globalTags = new HashMap<>();
    File[] liteAppDirs = FilePaths.LITE_APPS_SRC_DIR.listFiles();
    for (File liteAppDirectory : liteAppDirs) {
      if (!liteAppDirectory.isDirectory()) {
        continue; // Probably a temporary file, like .DS_Store.
      }

      File manifestJsonFile = new File(liteAppDirectory, FilePaths.MANIFEST_JSON_FILE_NAME);
      if (!manifestJsonFile.exists()) {
        throw new MissingManifestException(liteAppDirectory.getName());
      }

      Manifest manifest = MoshiAdapter.get(Manifest.class).fromJson(Okio.buffer(Okio.source(manifestJsonFile)));

      // For all tags applied to this manifest, check if they exist in the global tags list.
      if (manifest.tags != null) {
        for (String tagName : manifest.tags) {
          if (tagName == null || tagName.isEmpty()) {
            throw new IllegalArgumentException("Tag should not be blank");
          }
          LibraryTag tag = globalTags.get(tagName);
          if (tag == null) {
            // If this is the first time we are seeing this tag, create a new JSONArray to hold its contents.
            LibraryTag newTag = new LibraryTag(tagName);
            globalTags.put(tagName, newTag);
            libraryTagsList.addTag(newTag);
          }
        }
      }
    }

    // Write the tags to JSON
    FileUtils.writeFile(FilePaths.LITE_APPS_TAGS_JSON,
        MoshiAdapter.get(LibraryTagsList.class).toJson(libraryTagsList));
  }
}
