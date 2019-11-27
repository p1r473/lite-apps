package com.chimbori.liteapps;

import com.chimbori.FilePaths;
import com.chimbori.common.ColorExtractor;
import com.chimbori.common.FileUtils;
import com.chimbori.common.Log;
import com.chimbori.hermitcrab.schema.common.MoshiAdapter;
import com.chimbori.hermitcrab.schema.manifest.IconFile;
import com.chimbori.hermitcrab.schema.manifest.Manifest;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;

import javax.imageio.ImageIO;

import okio.Okio;

/**
 * Generates a skeleton Lite App with as many fields pre-filled as possible.
 */
class Scaffolder {
  private static final String MANIFEST_URL_TEMPLATE = "https://hermit.chimbori.com/lite-apps/%s.hermit";

  private static final String COMMAND_LINE_OPTION_URL = "url";
  private static final String COMMAND_LINE_OPTION_NAME = "name";

  private static final Integer CURRENT_MANIFEST_VERSION = 3;

  /**
   * The Library Data JSON file (containing metadata about all Lite Apps) is used as the basis for
   * generating scaffolding for the Lite App manifests.
   */
  private static void createScaffolding(String startUrl, String appName) throws IOException, Scraper.ManifestUnavailableException {
    File liteAppDirectoryRoot = new File(FilePaths.LITE_APPS_SRC_DIR, appName);

    Manifest manifest;
    File manifestJsonFile = new File(liteAppDirectoryRoot, FilePaths.MANIFEST_JSON_FILE_NAME);
    // If the manifest.json exists, read it before modifying, else create a new JSON object.
    if (manifestJsonFile.exists()) {
      manifest = MoshiAdapter.get(Manifest.class).fromJson(Okio.buffer(Okio.source(manifestJsonFile)));

    } else {
      Log.i("Creating new Lite App “%s”", appName);
      // Create the root directory if it doesn’t exist yet.
      liteAppDirectoryRoot.mkdirs();

      // Scrape the Web looking for RSS & Atom feeds, theme colors, and site metadata.
      Log.i("Fetching %s…", startUrl);
      Scraper scraper = new Scraper(startUrl).fetch();
      manifest = scraper.extractManifest();

      // Constant fields, same for all apps.
      manifest.manifest_version = CURRENT_MANIFEST_VERSION;

      // Fields that can be populated from the data provided on the command-line.
      manifest.name = appName;
      manifest.start_url = startUrl;
      manifest.manifest_url = String.format(MANIFEST_URL_TEMPLATE,
          URLEncoder.encode(appName, "UTF-8").replace("+", "%20"));

      // Empty fields that must be manually populated.
      manifest.priority = 10;
      manifest.tags = Collections.singletonList("");

      // Put the icon JSON entry even if we don’t manage to fetch an icon successfully.
      // This way, we can avoid additional typing, and the validator will check for the presence
      // of the file anyway (and fail as expected).
      manifest.icon = IconFile.FAVICON_FILE;

      File iconsDirectory = new File(liteAppDirectoryRoot, FilePaths.ICONS_DIR_NAME);
      iconsDirectory.mkdirs();
      File iconFile = new File(iconsDirectory, IconFile.FAVICON_FILE.fileName);

      String remoteIconUrl = scraper.extractIconUrl();
      if (!iconFile.exists() && remoteIconUrl != null && !remoteIconUrl.isEmpty()) {
        Log.i("Fetching icon from %s…", remoteIconUrl);
        URL iconUrl = null;
        try {
          iconUrl = new URL(remoteIconUrl);
        } catch (MalformedURLException e) {
          e.printStackTrace();
        }
        if (iconUrl != null) {
          try (InputStream inputStream = iconUrl.openStream()) {
            Files.copy(inputStream, iconFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
          } catch (IOException e) {
            e.printStackTrace();
            // But still continue with the rest of the manifest generation.
          }
        }
      }

      // Extract the color from the icon (either newly downloaded, or from existing icon).
      if (iconFile.exists()) {
        ColorExtractor.Color themeColor = ColorExtractor.getDominantColor(ImageIO.read(iconFile));
        if (themeColor != null) {
          // Overwrite the dummy values already inserted, if we are able to extract real values.
          manifest.theme_color = themeColor.toString();
          manifest.secondary_color = themeColor.darken(0.9f).toString();
        } else {
          // Insert a placeholder for theme_color and secondary_color so we don’t have to
          // type it in manually, but put invalid values so that the validator will catch it
          // in case we forget to replace with valid values.
          manifest.theme_color = "#";
          manifest.secondary_color = "#";
        }
      } else {
        // Insert a placeholder for theme_color and secondary_color so we don’t have to
        // type it in manually, but put invalid values so that the validator will catch it
        // in case we forget to replace with valid values.
        manifest.theme_color = "#";
        manifest.secondary_color = "#";
      }
    }

    // Write the output manifest.
    System.out.println(manifest);
    FileUtils.writeFile(manifestJsonFile, MoshiAdapter.get(Manifest.class).toJson(manifest));
  }

  public static void main(String[] arguments) {
    CommandLineParser parser = new DefaultParser();
    Options options = new Options()
        .addOption(Option.builder().required(true).hasArg(true)
            .longOpt(COMMAND_LINE_OPTION_URL)
            .argName("https://example.com/")
            .desc("Root URL of the Lite App")
            .build())
        .addOption(Option.builder().required(true).hasArg(true)
            .longOpt(COMMAND_LINE_OPTION_NAME)
            .argName("Example_Lite_App")
            .desc("Name of the Lite App")
            .build());
    try {
      // The Gradle wrapper makes it hard to pass spaces within arguments, so allow users
      // to type in underscores instead of spaces, and we strip them out here. This is simpler
      // than trying to parse the parameters in Groovy/Gradle, so we chose this slightly-hacky
      // approach.
      CommandLine command = parser.parse(options, arguments);
      Scaffolder.createScaffolding(
          command.getOptionValue(COMMAND_LINE_OPTION_URL),
          command.getOptionValue(COMMAND_LINE_OPTION_NAME).replaceAll("_", " "));

    } catch (ParseException e) {
      final PrintWriter writer = new PrintWriter(System.out);
      new HelpFormatter().printHelp("Scaffolder", options);
      writer.flush();
      System.exit(1);

    } catch (IOException | Scraper.ManifestUnavailableException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}
