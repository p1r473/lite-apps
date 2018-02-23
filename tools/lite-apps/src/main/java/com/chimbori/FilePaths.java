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

  // Inputs
  public static final File SRC_ROOT_DIR_LITE_APPS    = new File(PROJECT_ROOT, "lite-apps/src");
  public static final File SRC_TAGS_JSON_FILE        = new File(PROJECT_ROOT, "lite-apps/src/tags.json");
  public static final File SRC_ROOT_DIR_BLOCK_LISTS  = new File(PROJECT_ROOT, "blocklists/src/");
  public static final File SRC_BLOCK_LISTS_JSON      = new File(PROJECT_ROOT, "blocklists/src/sources.json");

  // Outputs
  public static final File OUT_LITE_APPS_DIR         = new File(PROJECT_ROOT, "lite-apps/v2/");
  public static final File OUT_LIBRARY_ICONS_DIR     = new File(PROJECT_ROOT, "library/112x112/");
  public static final File OUT_LIBRARY_JSON          = new File(PROJECT_ROOT, "library/lite-apps.json");

  public static final File OUT_ROOT_DIR_APP_MANIFEST = new File(PROJECT_ROOT, "app/v2/");
  public static final File OUT_ROOT_DIR_BLOCK_LISTS  = new File(PROJECT_ROOT, "blocklists/v2/");
  public static final File OUT_ROOT_DIR_FONTS        = new File(PROJECT_ROOT, "fonts/v2/");
  public static final File OUT_ROOT_DIR_STYLES       = new File(PROJECT_ROOT, "styles/v2/");

  static {
    OUT_LITE_APPS_DIR.mkdirs();
    OUT_LIBRARY_ICONS_DIR.mkdirs();
  }
}