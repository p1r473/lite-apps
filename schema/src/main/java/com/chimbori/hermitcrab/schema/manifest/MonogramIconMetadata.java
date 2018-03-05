package com.chimbori.hermitcrab.schema.manifest;

import com.chimbori.hermitcrab.schema.common.GsonInstance;

public class MonogramIconMetadata {
  public String color;
  public String text;

  public String toJson() {
    return GsonInstance.getMinifier().toJson(this);
  }

  public static MonogramIconMetadata fromJson(String json) {
    return GsonInstance.getMinifier().fromJson(json, MonogramIconMetadata.class);
  }

  @Override
  public String toString() {
    return "{" +
        "color='" + color + '\'' +
        ", text='" + text + '\'' +
        '}';
  }
}
