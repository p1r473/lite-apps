package com.chimbori.hermit.appmanifest;

import com.chimbori.FilePaths;
import com.chimbori.common.FileUtils;
import com.chimbori.hermitcrab.schema.appmanifest.AppManifest;
import com.chimbori.hermitcrab.schema.appmanifest.AppVersion;
import com.chimbori.hermitcrab.schema.appmanifest.AssetArchive;
import com.chimbori.hermitcrab.schema.appmanifest.AssetFormat;
import com.chimbori.hermitcrab.schema.appmanifest.Manifest;
import com.chimbori.hermitcrab.schema.common.GsonInstance;
import com.chimbori.hermitcrab.schema.common.SchemaDate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Writes an app manifest with the provided values. This is a convenience writer class for the JSON
 * to avoid having to write (and then validate) JSON by hand.
 */
class AppManifestWriter {
  /**
   * Static entry point into this class. It writes the current manifest as a minified JSON file
   * to the output bin/ directory.
   */
  public static void writeManifest() throws IOException {
    AppManifest appManifest = new AppManifest().manifest(createManifestForHardCodedVersion());
    FileUtils.writeFile(
        new File(FilePaths.OUT_APP_MANIFEST_DIR, FilePaths.APP_MANIFEST_FILE_NAME),
        GsonInstance.getPrettyPrinter().toJson(appManifest));
  }

  /**
   * Update this method for every release to include features for that release.
   */
  private static Manifest createManifestForHardCodedVersion() {
    Manifest manifest = createManifestWithDefaults();
    manifest.versions = new ArrayList<>();
    AppVersion version = new AppVersion()
        .track("production")
        .versionCode(100102)
        .os("android")
        .versionName("10.1.2")
        .released(new SchemaDate(2017, 9, 19))
        .minSdkVersion(19);

    version.features = new ArrayList<>();
    version.features.add("This upgrade contains important fixes for many recent issues.");
    version.features.add("Google acknowledged that Notification Channels on Oreo cause phone reboots, so we are temporarily removing Notification Channels until the next Android Oreo system update. https://issuetracker.google.com/issues/65650999");
    version.features.add("Lite App customizations are now correctly saved.");
    version.features.add("AMP Cache was broken by Google; this version does not use it any more.");
    version.features.add("Night Mode has been temporarily disabled for main app, but still available in Lite Apps, until we can reliably fix an issue on Android Oreo.");
    version.features.add("Added French translation!");
    version.features.add("See the full list at https://hermit.chimbori.com/changes");
    manifest.versions.add(version);

    return manifest;
  }

  /**
   * Creates a manifest that has values that don’t depend on a specific release version.
   * When malware blocklists, fonts, or styles are updated, changes should be made here.
   */
  private static Manifest createManifestWithDefaults() {
    Manifest manifest = new Manifest();
    manifest.locale = "en";

    manifest.blocklists = new ArrayList<>();
    manifest.blocklists.add(new AssetArchive()
        .name("Adware and Malware")
        .updated(new SchemaDate(2017, 8, 16))
        .format(AssetFormat.ZIP)
        .url("https://hermit.chimbori.com/app/adware-malware.json.zip"));

    manifest.fonts = new ArrayList<>();
    manifest.fonts.add(new AssetArchive()
        .name("Basic Fonts")
        .updated(new SchemaDate(2016, 12, 7))
        .format(AssetFormat.ZIP)
        .url("https://hermit.chimbori.com/app/basic-fonts.zip"));

    manifest.styles = new ArrayList<>();
    manifest.styles.add(new AssetArchive()
        .name("Night Styles")
        .updated(new SchemaDate(2017, 1, 4))
        .format(AssetFormat.ZIP)
        .url("https://hermit.chimbori.com/app/night-styles.zip"));

    return manifest;
  }
}
