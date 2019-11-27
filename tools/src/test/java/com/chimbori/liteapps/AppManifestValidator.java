package com.chimbori.liteapps;

import com.chimbori.FilePaths;
import com.chimbori.hermitcrab.schema.appmanifest.AppManifest;
import com.chimbori.hermitcrab.schema.appmanifest.AppVersion;
import com.chimbori.hermitcrab.schema.appmanifest.Manifest;
import com.chimbori.hermitcrab.schema.common.MoshiAdapter;
import com.chimbori.hermitcrab.schema.common.SchemaDate;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import okio.Okio;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AppManifestValidator {
  @Test
  public void testLiveAppManifestIsValid() throws IOException {
    File appManifestFile = new File(FilePaths.APP_MANIFEST_OUTPUT_DIR, FilePaths.MANIFEST_JSON_FILE_NAME);

    AppManifest appManifest = MoshiAdapter.get(AppManifest.class).fromJson(Okio.buffer(Okio.source(appManifestFile)));
    assertNotNull(appManifest);

    AppVersion latestProdVersion = appManifest.getLatestProdVersion(27);
    assertEquals(150302, latestProdVersion.version_code);
    assertEquals(21, latestProdVersion.min_sdk_version);
    assertEquals(new SchemaDate(2019, 11, 26), latestProdVersion.released);

    Manifest manifest = appManifest.manifest;
    assertNotNull(manifest);

    assertEquals("en", manifest.locale);
    assertEquals(1, manifest.versions.size());

    AppVersion version = manifest.versions.get(0);
    assertEquals("android", version.os);
    assertEquals("production", version.track);
    assertEquals(21, version.min_sdk_version);

    // The changes link should be in the last feature in the list.
    String changesListLink = version.features.get(version.features.size() - 1);
    assertTrue(changesListLink.contains("https://hermit.chimbori.com/changes"));

    assertEquals(1, manifest.blocklists.size());
    assertEquals("https://lite-apps.chimbori.com/blocklists/v2/%s", manifest.blocklists.get(0).url_pattern);
    assertEquals("%s", manifest.blocklists.get(0).file_pattern);
    assertEquals("adware-malware.json", manifest.blocklists.get(0).files.get(0));
  }
}
