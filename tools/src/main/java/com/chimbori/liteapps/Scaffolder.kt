package com.chimbori.liteapps

import com.chimbori.FilePaths.ICONS_DIR_NAME
import com.chimbori.FilePaths.LITE_APPS_SRC_DIR
import com.chimbori.FilePaths.MANIFEST_JSON_FILE_NAME
import com.chimbori.common.ColorExtractor.getDominantColor
import com.chimbori.common.FileUtils
import com.chimbori.common.Log
import com.chimbori.hermitcrab.schema.common.MoshiAdapter.getAdapter
import com.chimbori.hermitcrab.schema.manifest.IconFile.FAVICON_FILE
import com.chimbori.hermitcrab.schema.manifest.Manifest
import okio.buffer
import okio.source
import org.apache.commons.cli.*
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import javax.imageio.ImageIO

/**
 * Generates a skeleton Lite App with as many fields pre-filled as possible.
 */
internal object Scaffolder {
  private const val MANIFEST_URL_TEMPLATE = "https://hermit.chimbori.com/lite-apps/%s.hermit"
  private const val COMMAND_LINE_OPTION_URL = "url"
  private const val COMMAND_LINE_OPTION_NAME = "name"
  private const val CURRENT_MANIFEST_VERSION = 3

  /**
   * The Library Data JSON file (containing metadata about all Lite Apps) is used as the basis for
   * generating scaffolding for the Lite App manifests.
   */
  private fun createScaffolding(startUrl: String, appName: String) {
    val liteAppDirectoryRoot = File(LITE_APPS_SRC_DIR, appName)

    val manifest: Manifest?

    val manifestJsonFile = File(liteAppDirectoryRoot, MANIFEST_JSON_FILE_NAME)
    // If the manifest.json exists, read it before modifying, else create a new JSON object.
    if (manifestJsonFile.exists()) {
      manifest = getAdapter(Manifest::class.java).fromJson(manifestJsonFile.source().buffer())
    } else {
      Log.i("Creating new Lite App “%s”", appName)
      // Create the root directory if it doesn’t exist yet.
      liteAppDirectoryRoot.mkdirs()

      // Scrape the Web looking for RSS & Atom feeds, theme colors, and site metadata.
      Log.i("Fetching %s…", startUrl)
      val scraper = Scraper(startUrl).fetch()

      manifest = scraper.extractManifest()
      if (manifest == null) {
        System.err.println("Did you call Scraper.fetch() first?")
        System.exit(1)
      }

      // Constant fields, same for all apps.
      manifest!!.manifest_version = CURRENT_MANIFEST_VERSION

      // Fields that can be populated from the data provided on the command-line.
      manifest.name = appName
      manifest.start_url = startUrl
      manifest.manifest_url = String.format(MANIFEST_URL_TEMPLATE,
          URLEncoder.encode(appName, "UTF-8").replace("+", "%20"))

      // Empty fields that must be manually populated.
      manifest.priority = 10
      manifest.tags = mutableListOf()

      // Put the icon JSON entry even if we don’t manage to fetch an icon successfully.
      // This way, we can avoid additional typing, and the validator will check for the presence
      // of the file anyway (and fail as expected).
      manifest.icon = FAVICON_FILE
      val iconsDirectory = File(liteAppDirectoryRoot, ICONS_DIR_NAME)
      iconsDirectory.mkdirs()
      val iconFile = File(iconsDirectory, FAVICON_FILE.fileName)
      val remoteIconUrl = scraper.extractIconUrl()
      if (!iconFile.exists() && remoteIconUrl != null && !remoteIconUrl.isEmpty()) {
        Log.i("Fetching icon from %s…", remoteIconUrl)
        var iconUrl: URL? = null
        try {
          iconUrl = URL(remoteIconUrl)
        } catch (e: MalformedURLException) {
          e.printStackTrace()
        }
        if (iconUrl != null) {
          try {
            iconUrl.openStream().use { inputStream -> Files.copy(inputStream, iconFile.toPath(), REPLACE_EXISTING) }
          } catch (e: IOException) {
            e.printStackTrace()
            // But still continue with the rest of the manifest generation.
          }
        }
      }

      // Extract the color from the icon (either newly downloaded, or from existing icon).
      if (iconFile.exists()) {
        val themeColor = getDominantColor(ImageIO.read(iconFile))
        if (themeColor != null) {
          // Overwrite the dummy values already inserted, if we are able to extract real values.
          manifest.theme_color = themeColor.toString()
          manifest.secondary_color = themeColor.darken(0.9f).toString()
        } else {
          // Insert a placeholder for theme_color and secondary_color so we don’t have to
          // type it in manually, but put invalid values so that the validator will catch it
          // in case we forget to replace with valid values.
          manifest.theme_color = "#"
          manifest.secondary_color = "#"
        }
      } else {
        // Insert a placeholder for theme_color and secondary_color so we don’t have to
        // type it in manually, but put invalid values so that the validator will catch it
        // in case we forget to replace with valid values.
        manifest.theme_color = "#"
        manifest.secondary_color = "#"
      }
    }

    // Write the output manifest.
    println(manifest)
    FileUtils.writeFile(manifestJsonFile, getAdapter(Manifest::class.java).toJson(manifest))
  }

  @JvmStatic
  fun main(arguments: Array<String>) {
    val parser: CommandLineParser = DefaultParser()
    val options = Options()
        .addOption(Option.builder().required(true).hasArg(true)
            .longOpt(COMMAND_LINE_OPTION_URL)
            .argName("https://example.com/")
            .desc("Root URL of the Lite App")
            .build())
        .addOption(Option.builder().required(true).hasArg(true)
            .longOpt(COMMAND_LINE_OPTION_NAME)
            .argName("Example_Lite_App")
            .desc("Name of the Lite App")
            .build())
    try {
      // The Gradle wrapper makes it hard to pass spaces within arguments, so allow users
      // to type in underscores instead of spaces, and we strip them out here. This is simpler
      // than trying to parse the parameters in Groovy/Gradle, so we chose this slightly-hacky
      // approach.
      val command = parser.parse(options, arguments)
      createScaffolding(
          command.getOptionValue(COMMAND_LINE_OPTION_URL),
          command.getOptionValue(COMMAND_LINE_OPTION_NAME).replace("_".toRegex(), " "))
    } catch (e: ParseException) {
      val writer = PrintWriter(System.out)
      HelpFormatter().printHelp("Scaffolder", options)
      writer.flush()
      System.exit(1)
    } catch (e: IOException) {
      e.printStackTrace()
      System.exit(1)
    }
  }
}
