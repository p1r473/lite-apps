package com.chimbori.liteapps

import com.chimbori.FilePaths.LITE_APPS_OUTPUT_DIR
import com.chimbori.FilePaths.MANIFEST_JSON_FILE_NAME
import com.eclipsesource.json.Json
import java.io.*
import java.nio.file.attribute.FileTime
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

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
    createZipFile(liteAppDirectory, liteAppZippedFile)
    return true
  }

  private const val BUFFER_SIZE = 8192

  private fun createZipFile(rootDir: File, zipFile: File): Boolean {
    zipFile.parentFile.mkdirs()
    return try {
      ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { out ->
        out.setLevel(9)
        rootDir.listFiles { pathname -> pathname.name != ".DS_Store" }?.forEach { containedFile ->
          // Add files contained under the root directory, instead of the root directory itself,
          // so that the individual files appear at the root of the zip file instead of one directory down.
          addFileToZip(out, containedFile, parentDirectoryName = "")
        }
      }
      true
    } catch (e: IOException) {
      e.printStackTrace()
      false
    }
  }

  private fun addFileToZip(out: ZipOutputStream, file: File, parentDirectoryName: String) {
    val zipEntryName = if (parentDirectoryName.isNotEmpty()) {
      parentDirectoryName + "/"
    } else {
      ""
    } + file.name

    if (file.isDirectory) {
      file.listFiles()?.forEach { containedFile ->
        addFileToZip(out, containedFile, zipEntryName)
      }
    } else {
      val buffer = ByteArray(BUFFER_SIZE)
      try {
        FileInputStream(file).use { fileInputStream ->
          // Intentionally set the last-modified date to the epoch, so running the zip command
          // multiple times on the same (unchanged) source does not result in a different (binary)
          // zip file everytime.
          val epochTime = FileTime.fromMillis(0)
          out.putNextEntry(ZipEntry(zipEntryName).apply {
            creationTime = epochTime
            lastModifiedTime = epochTime
            lastAccessTime = epochTime
          })
          var length: Int
          while (fileInputStream.read(buffer).also { length = it } > 0) {
            out.write(buffer, 0, length)
          }
        }
      } finally {
        out.closeEntry()
      }
    }
  }
}
