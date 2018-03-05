package com.chimbori.hermitcrab.schema.manifest;

import com.google.gson.annotations.SerializedName;

public enum IconFile {
  @SerializedName("favicon.png") FAVICON_FILE("favicon.png"),
  @SerializedName("monogram.png") MONOGRAM_FILE("monogram.png"),
  @SerializedName("custom.png") CUSTOM_ICON_FILE("custom.png");

  public final String fileName;

  IconFile(String fileName) {
    this.fileName = fileName;
  }
}
