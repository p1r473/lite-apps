package com.chimbori.liteapps;

import com.chimbori.FilePaths;
import com.chimbori.common.ColorExtractor;
import com.chimbori.common.FileUtils;
import com.chimbori.hermitcrab.schema.common.MoshiAdapter;
import com.chimbori.hermitcrab.schema.manifest.IconFile;
import com.chimbori.hermitcrab.schema.manifest.Manifest;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;

import okio.Okio;

class PaletteExtractor {
  private static void extractPaletteIfMissing() throws IOException {
    File[] liteAppDirs = FilePaths.LITE_APPS_SRC_DIR.listFiles();
    for (File liteAppDirectory : liteAppDirs) {
      if (!liteAppDirectory.isDirectory()) {
        continue; // Probably a temporary file, like .DS_Store.
      }

      String appName = liteAppDirectory.getName();
      File manifestJsonFile = new File(liteAppDirectory, FilePaths.MANIFEST_JSON_FILE_NAME);
      if (!manifestJsonFile.exists()) {
        throw new MissingManifestException(appName);
      }

      // Create an entry for this Lite App to be put in the directory index file.
      Manifest manifest = MoshiAdapter.get(Manifest.class).fromJson(Okio.buffer(Okio.source(manifestJsonFile)));

      if (isUndefinedColor(manifest.getTheme_color()) || isUndefinedColor(manifest.getSecondary_color())) {
        System.out.println("manifest: " + manifest.getName());

        File iconsDirectory = new File(liteAppDirectory, FilePaths.ICONS_DIR_NAME);
        iconsDirectory.mkdirs();
        File iconFile = new File(iconsDirectory, IconFile.FAVICON_FILE.getFileName());

        // Extract the color from the icon (either newly downloaded, or from existing icon).
        if (iconFile.exists()) {
          ColorExtractor.Color themeColor = ColorExtractor.getDominantColor(ImageIO.read(iconFile));
          if (themeColor != null) {
            // Overwrite the dummy values already inserted, if we are able to extract real values.
            manifest.setTheme_color(themeColor.toString());
            manifest.setSecondary_color(themeColor.darken(0.9f).toString());

            FileUtils.writeFile(manifestJsonFile, MoshiAdapter.get(Manifest.class).toJson(manifest));
          }
        }
      }
    }
  }

  private static boolean isUndefinedColor(@Nullable String color) {
    return color == null || color.isEmpty() || color.equals("#");
  }

  public static void main(String[] arguments) {
    try {
      extractPaletteIfMissing();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
