package com.chimbori.liteapps

import com.chimbori.FilePaths
import com.chimbori.hermitcrab.schema.appmanifest.AppManifest
import com.chimbori.hermitcrab.schema.appmanifest.getLatestProdVersion
import com.chimbori.hermitcrab.schema.common.MoshiAdapter.getAdapter
import com.chimbori.hermitcrab.schema.common.SchemaDate
import okio.buffer
import okio.source
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File

class AppManifestValidator {
  @Test
  fun testLiveAppManifestIsValid() {
    val appManifestFile = File(FilePaths.APP_MANIFEST_OUTPUT_DIR, FilePaths.MANIFEST_JSON_FILE_NAME)
    val appManifest = getAdapter(AppManifest::class.java).fromJson(appManifestFile.source().buffer())
    assertNotNull(appManifest)

    val (_, _, version_code, _, released, min_sdk_version) = appManifest!!.getLatestProdVersion(currentSdk = 27)!!
    assertEquals(150302, version_code.toLong())
    assertEquals(21, min_sdk_version.toLong())
    assertEquals(SchemaDate(2019, 11, 26), released)
    val manifest = appManifest.manifest
    assertNotNull(manifest)
    assertEquals("en", manifest.locale)
    assertEquals(1, manifest.versions.size.toLong())
    val (track, os, _, _, _, min_sdk_version1, features) = manifest.versions[0]
    assertEquals("android", os)
    assertEquals("production", track)
    assertEquals(21, min_sdk_version1.toLong())

    // The changes link should be in the last feature in the list.
    val changesListLink = features[features.size - 1]
    Assert.assertTrue(changesListLink.contains("https://hermit.chimbori.com/changes"))
    assertEquals(1, manifest.blocklists.size.toLong())
    assertEquals("https://lite-apps.chimbori.com/blocklists/v2/%s", manifest.blocklists[0].url_pattern)
    assertEquals("%s", manifest.blocklists[0].file_pattern)
    assertEquals("adware-malware.json", manifest.blocklists[0].files[0])
  }
}
