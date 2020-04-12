package com.chimbori

import java.io.File
import java.io.IOException
import java.net.URISyntaxException

object FilePaths {
  const val MANIFEST_JSON_FILE_NAME = "manifest.json"
  const val ICONS_DIR_NAME = "icons"
  const val ICON_EXTENSION = ".png"

  /**
   * The project root directory cannot be hard-coded in the code because it can and will be
   * different in different environments, e.g. local runs, continuous test environments, etc.
   * Using the ClassLoader offers us the most hermetic way of determining the correct paths.
   */
  var PROJECT_ROOT: File? = try {
    File(File(ClassLoader.getSystemResource(".").toURI()), "../../../../../").canonicalFile
  } catch (e: URISyntaxException) {
    e.printStackTrace()
    null
  } catch (e: IOException) {
    e.printStackTrace()
    null
  }

  // Lite Apps
  val LITE_APPS_SRC_DIR = File(PROJECT_ROOT, "src/lite-apps")
  val LITE_APPS_TAGS_JSON = File(PROJECT_ROOT, "src/tags.json")

  // Block Lists
  val BLOCKLISTS_SRC_DIR = File(PROJECT_ROOT, "src/blocklists")
  val BLOCKLISTS_CONFIG_JSON = File(PROJECT_ROOT, "src/blocklists/config.json")

  // Output Directories, under `/docs`.
  val LITE_APPS_OUTPUT_DIR = File(PROJECT_ROOT, "docs/lite-apps/v3/")
  val LIBRARY_ICONS_DIR = File(PROJECT_ROOT, "docs/library/112x112/")
  val LIBRARY_JSON = File(PROJECT_ROOT, "docs/library/lite-apps.json")
  val BLOCKLISTS_OUTPUT_DIR = File(PROJECT_ROOT, "docs/blocklists/v2/")
  val APP_MANIFEST_OUTPUT_DIR = File(PROJECT_ROOT, "docs/app/v2/")

  init {
    LITE_APPS_OUTPUT_DIR.mkdirs()
    LIBRARY_ICONS_DIR.mkdirs()
    BLOCKLISTS_OUTPUT_DIR.mkdirs()
    APP_MANIFEST_OUTPUT_DIR.mkdirs()
  }
}
