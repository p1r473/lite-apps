package com.chimbori.hermitcrab.schema.appmanifest;

/**
 * Represents an AppManifest, downloaded from the Hermit server as a central place for all
 * app related data. This is directly parsed from JSON, and may be cached for a period of multiple
 * days. Do not attempt to refresh this too often, to minimize load on servers.
 */
@SuppressWarnings("CanBeFinal")
public class AppManifest {
  public Manifest manifest;

  public AppVersion getLatestProdVersion() {
    AppVersion latestStable = null;
    for (AppVersion version : manifest.versions) {
      if (!version.track.equals("production") || !version.os.equals("android")) {
        continue;
      }
      if (latestStable == null || version.versionCode >= latestStable.versionCode) {
        latestStable = version;
      }
    }
    return latestStable;
  }

  public AppManifest manifest(Manifest manifest) {
    this.manifest = manifest;
    return this;
  }
}
