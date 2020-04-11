package com.chimbori.liteapps

import com.chimbori.FilePaths.ICONS_DIR_NAME
import com.chimbori.FilePaths.LITE_APPS_SRC_DIR
import com.chimbori.FilePaths.MANIFEST_JSON_FILE_NAME
import com.chimbori.common.ColorExtractor.getDominantColor
import com.chimbori.hermitcrab.schema.common.MoshiAdapter.getAdapter
import com.chimbori.hermitcrab.schema.manifest.IconFile.FAVICON_FILE
import com.chimbori.hermitcrab.schema.manifest.Manifest
import okio.buffer
import okio.source
import java.io.File
import javax.imageio.ImageIO

internal object PaletteExtractor {
  @JvmStatic
  fun main(arguments: Array<String>) {
    LITE_APPS_SRC_DIR.listFiles()?.forEach { liteAppDirectory ->
      if (!liteAppDirectory.isDirectory) {
        return@forEach  // Probably a temporary file, like .DS_Store.
      }

      val manifestJsonFile = File(liteAppDirectory, MANIFEST_JSON_FILE_NAME)
      if (!manifestJsonFile.exists()) {
        return@forEach
      }

      val manifest = getAdapter(Manifest::class.java).fromJson(manifestJsonFile.source().buffer())
          ?: return@forEach

      if (!(manifest.theme_color.isUndefinedColor() || manifest.secondary_color.isUndefinedColor())) {
        return@forEach
      }

      println("- Colors missing: [${manifest.name}]")

      // Extract the color from the icon (either newly downloaded, or from existing icon).
      val iconFile = File("$liteAppDirectory/$ICONS_DIR_NAME/${FAVICON_FILE.fileName}")
      if (!iconFile.exists()) {
        System.err.println("  * Icon missing!")
        return@forEach
      }

      getDominantColor(ImageIO.read(iconFile))?.let { themeColor ->
        // Overwrite the dummy values already inserted, if we are able to extract real values.
        println("  - Palette extracted")

        manifest.theme_color = themeColor.toString()
        manifest.secondary_color = themeColor.darken(0.9f).toString()
        manifestJsonFile.writeText(getAdapter(Manifest::class.java).toJson(manifest))
      }
    }
  }

  private fun String?.isUndefinedColor() = this == null || isEmpty() || equals("#")
}
