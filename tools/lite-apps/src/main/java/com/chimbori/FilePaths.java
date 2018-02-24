package com.chimbori;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class FilePaths {
  // Filenames
  public static final String MANIFEST_JSON_FILE_NAME = "manifest.json";
  public static final String MESSAGES_JSON_FILE_NAME = "messages.json";
  public static final String LOCALES_DIR_NAME = "_locales";
  public static final String ICONS_DIR_NAME = "icons";
  public static final String ICON_EXTENSION = ".png";

  /**
   * The project root directory cannot be hard-coded in the code because it can and will be
   * different in different environments, e.g. local runs, continuous test environments, etc.
   * Using the ClassLoader offers us the most hermetic way of determining the correct paths.
   */
  public static File PROJECT_ROOT = null;
  static {
    try {
      PROJECT_ROOT = new File(new File(
          ClassLoader.getSystemResource(".").toURI()), "../../../../../../").getCanonicalFile();
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
    }
  }

  // Lite Apps
  public static final File LITE_APPS_SRC_DIR        = new File(PROJECT_ROOT, "lite-apps/src");
  public static final File LITE_APPS_TAGS_JSON      = new File(PROJECT_ROOT, "lite-apps/src/tags.json");
  public static final File LITE_APPS_OUTPUT_DIR     = new File(PROJECT_ROOT, "lite-apps/v2/");

  // Library
  public static final File LIBRARY_ICONS_DIR        = new File(PROJECT_ROOT, "library/112x112/");
  public static final File LIBRARY_JSON             = new File(PROJECT_ROOT, "library/lite-apps.json");

  // Block Lists
  public static final File BLOCKLISTS_SRC_DIR       = new File(PROJECT_ROOT, "blocklists/src/");
  public static final File BLOCKLISTS_SOURCES_JSON  = new File(PROJECT_ROOT, "blocklists/src/sources.json");
  public static final File BLOCKLISTS_OUTPUT_DIR    = new File(PROJECT_ROOT, "blocklists/v2/");

  // App Manifest
  public static final File APP_MANIFEST_OUTPUT_DIR  = new File(PROJECT_ROOT, "app/v2/");

  // Fonts
  public static final File FONTS_OUTPUT_DIR         = new File(PROJECT_ROOT, "fonts/v2/");

  // Styles
  public static final File STYLES_OUTPUT_DIR        = new File(PROJECT_ROOT, "styles/v2/");

  static {
    LITE_APPS_OUTPUT_DIR.mkdirs();
    LIBRARY_ICONS_DIR.mkdirs();
    BLOCKLISTS_OUTPUT_DIR.mkdirs();
    APP_MANIFEST_OUTPUT_DIR.mkdirs();
    FONTS_OUTPUT_DIR.mkdirs();
    STYLES_OUTPUT_DIR.mkdirs();
  }
}