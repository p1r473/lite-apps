package com.chimbori.liteapps;

import com.chimbori.FilePaths;
import com.chimbori.common.FileUtils;
import com.chimbori.hermitcrab.schema.common.MoshiAdapter;
import com.chimbori.hermitcrab.schema.library.Library;
import com.chimbori.hermitcrab.schema.library.LibraryApp;
import com.chimbori.hermitcrab.schema.library.LibraryTagsList;
import com.chimbori.hermitcrab.schema.manifest.IconFile;
import com.chimbori.hermitcrab.schema.manifest.Manifest;

import net.coobird.thumbnailator.Thumbnails;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import okio.Okio;

class LibraryGenerator {
  private static final int LIBRARY_ICON_SIZE = 112;

  private static final String USER_AGENT_DESKTOP = "desktop";

  /**
   * Individual manifest.json files do not contain any information about the organization of the
   * Lite Apps in the Library (e.g. categories, order within category, whether it should be
   * displayed or not. This metadata is stored in a separate index.json file. To minimize
   * duplication & to preserve a single source of truth, this file does not contain actual URLs
   * or anything about a Lite App other than its name (same as the directory name).
   * <p>
   * This generator tool combines the basic organizational metadata from index.json & detailed
   * Lite Apps data from * / manifest.json files. It outputs the Library Data JSON file,
   * which is used as the basis for generating the Hermit Library page at
   * https://lite-apps.chimbori.com/library.
   */
  public static void generateLibraryData() throws IOException {
    // Read the list of all known tags from the tags.json file. In case we discover any new tags,
    // we will add them to this file, taking care not to overwrite those that already exist.
    LibraryTagsList globalTags = MoshiAdapter.get(LibraryTagsList.class)
        .fromJson(Okio.buffer(Okio.source(FilePaths.LITE_APPS_TAGS_JSON)));
    globalTags.updateTransientFields();
    Library outputLibrary = new Library(globalTags);

    File[] liteAppDirs = FilePaths.LITE_APPS_SRC_DIR.listFiles();
    for (File liteAppDirectory : liteAppDirs) {
      if (!liteAppDirectory.isDirectory()) {
        continue; // Probably a temporary file, like .DS_Store.
      }

      File iconsDirectory = new File(liteAppDirectory, FilePaths.ICONS_DIR_NAME);

      String appName = liteAppDirectory.getName();
      File manifestJsonFile = new File(liteAppDirectory, FilePaths.MANIFEST_JSON_FILE_NAME);
      if (!manifestJsonFile.exists()) {
        throw new MissingManifestException(appName);
      }

      // Create an entry for this Lite App to be put in the directory index file.
      Manifest manifest = MoshiAdapter.get(Manifest.class).fromJson(Okio.buffer(Okio.source(manifestJsonFile)));

      LibraryApp outputApp = new LibraryApp(
          manifest.getTheme_color(),
          appName,
          manifest.getStart_url(),
          "",
          manifest.getPriority()
      );

      // Set user-agent from the settings stored in the Lite App’s manifest.json.
      String userAgent = manifest.getSettings() != null ? manifest.getSettings().getUser_agent() : null;
      if (USER_AGENT_DESKTOP.equals(userAgent)) {
        outputApp.setUser_agent(USER_AGENT_DESKTOP);
      }

      outputLibrary.addAppToCategories(outputApp, manifest.getTags());

      // Resize the icon to be suitable for the Web, and copy it to the Web-accessible icons directory.
      File thumbnailImage = new File(FilePaths.LIBRARY_ICONS_DIR, appName + FilePaths.ICON_EXTENSION);
      if (!thumbnailImage.exists()) {
        Thumbnails.of(new File(iconsDirectory, IconFile.FAVICON_FILE.getFileName()))
            .outputQuality(1.0f)
            .useOriginalFormat()
            .size(LIBRARY_ICON_SIZE, LIBRARY_ICON_SIZE)
            .imageType(BufferedImage.TYPE_INT_ARGB)
            .toFile(thumbnailImage);
      }
    }

    FileUtils.writeFile(FilePaths.LIBRARY_JSON, MoshiAdapter.get(Library.class).toJson(outputLibrary));
  }
}
