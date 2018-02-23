package com.chimbori;

import com.chimbori.common.FileUtils;

import java.io.File;

public class FilePaths {
  // Filenames
  public static final String MANIFEST_JSON_FILE_NAME = "manifest.json";
  public static final String MESSAGES_JSON_FILE_NAME = "messages.json";
  public static final String LOCALES_DIR_NAME = "_locales";
  public static final String ICONS_DIR_NAME = "icons";
  public static final String ICON_EXTENSION = ".png";

  // App Manifest
  public static final String APP_MANIFEST_FILE_NAME = "manifest";  // No extension.

  // Inputs
  public static final File SRC_ROOT_DIR_LITE_APPS   = new File(FileUtils.PROJECT_ROOT, "lite-apps/");
  public static final File SRC_TAGS_JSON_FILE       = new File(FileUtils.PROJECT_ROOT, "lite-apps/tags.json");
  public static final File SRC_ROOT_DIR_BLOCK_LISTS = new File(FileUtils.PROJECT_ROOT, "blocklists/");
  public static final File SRC_BLOCK_LISTS_JSON     = new File(FileUtils.PROJECT_ROOT, "blocklists/index.json");

  // Outputs
  public static final File OUT_APP_MANIFEST_DIR     = new File(FileUtils.PROJECT_ROOT, "bin/app/");
  public static final File OUT_LITE_APPS_DIR        = new File(FileUtils.PROJECT_ROOT, "bin/lite-apps/");
  public static final File OUT_LIBRARY_ICONS_DIR    = new File(FileUtils.PROJECT_ROOT, "bin/library/112x112/");
  public static final File OUT_LIBRARY_JSON         = new File(FileUtils.PROJECT_ROOT, "bin/library/library.json");
  public static final File OUT_ROOT_DIR_BLOCK_LISTS = new File(FileUtils.PROJECT_ROOT, "bin/blocklists/");

  static {
    OUT_LITE_APPS_DIR.mkdirs();
    OUT_LIBRARY_ICONS_DIR.mkdirs();
  }
}