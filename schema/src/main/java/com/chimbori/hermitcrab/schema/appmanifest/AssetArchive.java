package com.chimbori.hermitcrab.schema.appmanifest;

import com.chimbori.hermitcrab.schema.common.SchemaDate;

@SuppressWarnings({"CanBeFinal", "unused"})
public class AssetArchive {
  public String name;
  public String url;
  public SchemaDate updated;
  public AssetFormat format;

  public AssetArchive name(String name) {
    this.name = name;
    return this;
  }

  public AssetArchive updated(SchemaDate updated) {
    this.updated = updated;
    return this;
  }

  public AssetArchive url(String url) {
    this.url = url;
    return this;
  }

  public AssetArchive format(AssetFormat format) {
    this.format = format;
    return this;
  }
}
