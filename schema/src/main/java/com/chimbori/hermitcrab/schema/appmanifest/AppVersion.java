package com.chimbori.hermitcrab.schema.appmanifest;

import com.chimbori.hermitcrab.schema.common.SchemaDate;

import java.util.List;

/**
 * Encapsulates data about an app version. Can be parsed from the App Manifest JSON.
 */
@SuppressWarnings({"CanBeFinal", "unused"})
public class AppVersion {
  public String track;
  public String os;
  public int versionCode;
  public String versionName;
  public SchemaDate released; // Can’t save to database as a Date, change this to long if needed.
  public int minSdkVersion;
  public List<String> features;

  public AppVersion track(String track) {
    this.track = track;
    return this;
  }

  public AppVersion versionCode(int versionCode) {
    this.versionCode = versionCode;
    return this;
  }

  public AppVersion os(String os) {
    this.os = os;
    return this;
  }

  public AppVersion versionName(String versionName) {
    this.versionName = versionName;
    return this;
  }

  public AppVersion released(SchemaDate released) {
    this.released = released;
    return this;
  }

  public AppVersion minSdkVersion(int minSdkVersion) {
    this.minSdkVersion = minSdkVersion;
    return this;
  }
}
