package com.chimbori.liteapps;

import com.chimbori.FilePaths;
import com.chimbori.hermitcrab.schema.appmanifest.AppManifest;
import com.chimbori.hermitcrab.schema.appmanifest.AppVersion;
import com.chimbori.hermitcrab.schema.appmanifest.Manifest;
import com.chimbori.hermitcrab.schema.common.GsonInstance;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AppManifestValidator {
  @Test
  public void testLiveAppManifestIsValid() throws FileNotFoundException {
    File appManifestFile = new File(FilePaths.APP_MANIFEST_OUTPUT_DIR, FilePaths.MANIFEST_JSON_FILE_NAME);

    AppManifest appManifest = GsonInstance.getPrettyPrinter().fromJson(
        new FileReader(appManifestFile), AppManifest.class);
    assertNotNull(appManifest);
    assertEquals(110006, appManifest.getLatestProdVersion().versionCode);

    Manifest manifest = appManifest.manifest;
    assertNotNull(manifest);

    assertEquals("en", manifest.locale);
    assertEquals(1, manifest.versions.size());

    AppVersion version = manifest.versions.get(0);
    assertEquals("android", version.os);
    assertEquals("production", version.track);
    assertEquals(19, version.minSdkVersion);

    // The changes link should be in the last feature in the list.
    String changesListLink = version.features.get(version.features.size() - 1);
    assertTrue(changesListLink.contains("https://hermit.chimbori.com/changes"));

    assertEquals(1, manifest.blocklists.size());
    assertEquals("https://hermit.chimbori.com/blocklists/v2/%s", manifest.blocklists.get(0).urlPattern);
    assertEquals("%s", manifest.blocklists.get(0).filePattern);
    assertEquals("adware-malware.json", manifest.blocklists.get(0).files.get(0));
  }
}
