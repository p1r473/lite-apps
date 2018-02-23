package com.chimbori.liteapps;

import com.chimbori.common.ResourceNotFoundException;
import com.chimbori.common.TestUtils;
import com.chimbori.hermitcrab.schema.appmanifest.AppManifest;
import com.chimbori.hermitcrab.schema.appmanifest.AppVersion;
import com.chimbori.hermitcrab.schema.appmanifest.Manifest;
import com.chimbori.hermitcrab.schema.common.GsonInstance;
import com.chimbori.hermitcrab.schema.common.SchemaDate;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AppManifestValidator {
  @Test
  public void testAppManifestParsing() throws FileNotFoundException, ResourceNotFoundException {
    File manifestFile = TestUtils.getResource(this.getClass(), "manifest.json");
    AppManifest appManifest = GsonInstance.getPrettyPrinter().fromJson(
        new FileReader(manifestFile), AppManifest.class);
    assertNotNull(appManifest);

    Manifest manifest = appManifest.manifest;
    assertNotNull(manifest);

    assertEquals("en", manifest.locale);
    assertEquals(1, manifest.versions.size());

    AppVersion version = manifest.versions.get(0);
    assertEquals("android", version.os);
    assertEquals("production", version.track);
    assertEquals(new SchemaDate(2017, 5, 4), version.released);
    assertEquals(19, version.minSdkVersion);
    assertEquals("8.1.2", version.versionName);
    assertEquals(80102, version.versionCode);

    // The changes link should be in the last feature in the list.
    String changesListLink = version.features.get(version.features.size() - 1);
    assertTrue(changesListLink.contains("https://hermit.chimbori.com/changes"));

    assertEquals(1, manifest.blocklists.size());
    assertEquals("Adware and Malware", manifest.blocklists.get(0).name);
    assertEquals("https://hermit.chimbori.com/app/adware-malware.json.zip", manifest.blocklists.get(0).url);
    assertEquals(new SchemaDate(2017, 4, 24), manifest.blocklists.get(0).updated);

    assertEquals(1, manifest.fonts.size());
    assertEquals("Basic Fonts", manifest.fonts.get(0).name);
    assertEquals("https://hermit.chimbori.com/app/basic-fonts.zip", manifest.fonts.get(0).url);
    assertEquals(new SchemaDate(2016, 12, 7), manifest.fonts.get(0).updated);

    assertEquals(1, manifest.styles.size());
    assertEquals("Night Styles", manifest.styles.get(0).name);
    assertEquals("https://hermit.chimbori.com/app/night-styles.zip", manifest.styles.get(0).url);
    assertEquals(new SchemaDate(2017, 1, 4), manifest.styles.get(0).updated);
  }
}
