package com.chimbori.liteapps

import com.chimbori.FilePaths.LITE_APPS_OUTPUT_DIR
import com.chimbori.FilePaths.MANIFEST_JSON_FILE_NAME
import com.chimbori.common.FileUtils
import com.eclipsesource.json.Json
import java.io.File
import java.io.FileReader
import java.io.IOException

/**
 * Packages all Lite Apps into their corresponding .hermit packages.
 * Does not validate each Lite App prior to packaging: it is assumed that this is run on a green
 * build which has already passed all validation tests.
 */
internal object LiteAppPackager {
  /**
   * Packages a single manifest from a source directory & individual files into a zipped file and
   * places it in the correct location.
   */
  fun packageManifest(liteAppDirectory: File): Boolean {
    val manifestJsonFile = File(liteAppDirectory, MANIFEST_JSON_FILE_NAME)
    val liteAppZippedFile = File(LITE_APPS_OUTPUT_DIR, "${liteAppDirectory.name}.hermit")
    try {
      Json.parse(FileReader(manifestJsonFile))
    } catch (e: IOException) {
      return false
    }
    FileUtils.zip(liteAppDirectory, liteAppZippedFile)
    return true
  }
}
