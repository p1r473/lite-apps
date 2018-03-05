package com.chimbori.hermitcrab.schema.manifest;

import com.chimbori.hermitcrab.schema.common.GsonInstance;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ManifestTest {
  @Test
  public void testBasicManifestParsing() throws FileNotFoundException, URISyntaxException {
    File manifestFile = Paths.get(getClass().getClassLoader().getResource("facebook-manifest.json").toURI()).toFile();
    Manifest manifest = GsonInstance.getPrettyPrinter().fromJson(new FileReader(manifestFile), Manifest.class);
    assertNotNull(manifest);

    assertEquals("__MSG_facebook__", manifest.name);
    assertEquals(1, manifest.manifestVersion.longValue());
    assertEquals("en", manifest.lang);
    assertEquals("https://m.facebook.com", manifest.startUrl);
    assertEquals("https://hermit.chimbori.com/lite-apps/Facebook.hermit", manifest.manifestUrl);
    assertEquals("#3b5998", manifest.themeColor);
    assertEquals("#3b5998", manifest.secondaryColor);
    assertEquals(100, manifest.priority.longValue());

    assertEquals(1, manifest.tags.size());
    assertEquals("Social", manifest.tags.get(0));

    assertEquals(IconFile.FAVICON_FILE, manifest.icon);

    assertEquals("#3b5998", manifest.monogram.color);
    assertEquals("F", manifest.monogram.text);

    assertEquals(9, manifest.hermitBookmarks.size());
    assertEquals("__MSG_most_recent__", manifest.hermitBookmarks.get(0).name);
    assertEquals("https://m.facebook.com/home.php?sk=h_chr", manifest.hermitBookmarks.get(0).url);

    assertEquals(1, manifest.hermitFeeds.size());
    assertEquals("https://m.facebook.com/notifications/rss", manifest.hermitFeeds.get(0).url);

    assertEquals(1, manifest.hermitSearch.size());
    assertEquals("__MSG_search_facebook__", manifest.hermitSearch.get(0).name);
    assertEquals("https://m.facebook.com/search/top/?q=%s", manifest.hermitSearch.get(0).url);

    assertEquals(1, manifest.hermitShare.size());
    assertEquals("__MSG_post_to_facebook__", manifest.hermitShare.get(0).name);
    assertEquals("https://www.facebook.com/sharer/sharer.php?u=%u", manifest.hermitShare.get(0).url);

    assertEquals(1, manifest.hermitMonitors.size());
    assertEquals("__MSG_notifications__", manifest.hermitMonitors.get(0).name);
    assertEquals("https://m.facebook.com/notifications", manifest.hermitMonitors.get(0).url);
    assertEquals("#notifications_list .touchable-notification", manifest.hermitMonitors.get(0).selector);
    assertEquals("facebook", manifest.hermitMonitors.get(0).icon.toString());

    assertEquals(1, manifest.relatedApplications.size());
    assertEquals("com.facebook.katana", manifest.relatedApplications.get(0).id);
    assertEquals("play", manifest.relatedApplications.get(0).platform);
    assertEquals("https://play.google.com/store/apps/details?id=com.facebook.katana", manifest.relatedApplications.get(0).url);
  }

  @Test
  public void testSettingsParsing() throws URISyntaxException, FileNotFoundException {
    File manifestFile = Paths.get(getClass().getClassLoader().getResource("facebook-manifest.json").toURI()).toFile();
    Manifest manifest = GsonInstance.getPrettyPrinter().fromJson(new FileReader(manifestFile), Manifest.class);

    assertNotNull(manifest);
    assertNotNull(manifest.hermitSettings);

    assertTrue(manifest.hermitSettings.blockMalware);
    assertFalse(manifest.hermitSettings.blockPopups);
    assertTrue(manifest.hermitSettings.blockThirdPartyCookies);
    assertEquals("day", manifest.hermitSettings.dayNightMode);
    assertFalse(manifest.hermitSettings.doNotTrack);
    assertTrue(manifest.hermitSettings.javascript);
    assertTrue(manifest.hermitSettings.loadImages);
    assertEquals("", manifest.hermitSettings.nightModePageStyle);
    assertEquals("in_app", manifest.hermitSettings.openLinks);
    assertTrue(manifest.hermitSettings.pullToRefresh);
    assertFalse(manifest.hermitSettings.saveData);
    assertFalse(manifest.hermitSettings.scrollToTop);
    assertEquals(100, manifest.hermitSettings.textZoom.longValue());
  }
}
