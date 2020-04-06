package com.chimbori.liteapps;


import com.chimbori.hermitcrab.schema.manifest.Endpoint;
import com.chimbori.hermitcrab.schema.manifest.Manifest;
import com.chimbori.hermitcrab.schema.manifest.RelatedApp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scrapes a web site using JSoup to identify elements such as the name of a site, a dominant theme
 * color, and a list of likely top-level bookmarks. This may be used to bootstrap the manifest for
 * a Lite App.
 */
public class Scraper {
  private static final int FETCH_TIMEOUT_MS = 10000;
  private static final String CHROME_MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.23 Mobile Safari/537.36";
  private static final String GOOGLE_COM_HOME_PAGE = "https://www.google.com";

  private final String url;
  private Document doc;

  public Scraper(String url) {
    this.url = url;
  }

  public Scraper fetch() {
    try {
      doc = Jsoup.connect(url)
          .ignoreContentType(true)
          .userAgent(CHROME_MOBILE_USER_AGENT)
          .referrer(GOOGLE_COM_HOME_PAGE)
          .timeout(FETCH_TIMEOUT_MS)
          .followRedirects(true)
          .execute().parse();
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
    return this;
  }

  public Manifest extractManifest() throws ManifestUnavailableException {
    Manifest manifest = new Manifest();
    if (doc == null) {  // Fetch failed or never fetched.
      throw new ManifestUnavailableException();
    }

    // First try to get the Site’s name (not just the current page’s title) via OpenGraph tags.
    manifest.setName(doc.select("meta[property=og:site_name]").attr("content"));
    if (manifest.getName() == null || manifest.getName().isEmpty()) {
      // But if the page isn’t using OpenGraph tags, then fallback to using the current page’s title.
      manifest.setName(doc.select("title").text());
    }
    manifest.setTheme_color(doc.select("meta[name=theme-color]").attr("content"));
    manifest.setBookmarks(findBookmarkableLinks());
    manifest.setFeeds(findAtomAndRssFeeds());
    manifest.setRelated_applications(findRelatedApps());

    return manifest;
  }

  public String extractIconUrl() {
    // The "abs:" prefix is a JSoup shortcut that converts this into an absolute URL.
    String iconUrl = doc.select("link[rel=apple-touch-icon]").attr("abs:href");
    if (iconUrl == null || iconUrl.isEmpty()) {
      iconUrl = doc.select("link[rel=apple-touch-icon-precomposed]").attr("abs:href");
    }
    return iconUrl;
  }

  private List<RelatedApp> findRelatedApps() {
    List<RelatedApp> relatedApps = new ArrayList<>();
    Elements playStoreLinks = doc.select("a[href*=play.google.com]");
    for (Element playStoreLink : playStoreLinks) {
      String playStoreUrl = playStoreLink.attr("href");
      System.out.println(playStoreUrl);
      Matcher matcher = Pattern.compile("id=([^&]+)").matcher(playStoreUrl);
      while (matcher.find()) {
        relatedApps.add(new RelatedApp(matcher.group(1)));
      }
    }
    return relatedApps.isEmpty() ? null : relatedApps;
  }

  private List<Endpoint> findAtomAndRssFeeds() {
    List<Endpoint> feeds = new ArrayList<>();
    Elements atomOrRssFeeds = doc.select("link[type=application/rss+xml], link[type=application/atom+xml]");
    for (Element feed : atomOrRssFeeds) {
      Endpoint feedLink = new Endpoint();
      feedLink.setUrl(feed.attr("abs:href"));
      feedLink.setName(feed.attr("title"));
      scrubFields(feedLink);
      feeds.add(feedLink);
    }
    return feeds.isEmpty() ? null : feeds;
  }

  private List<Endpoint> findBookmarkableLinks() {
    // Use a Map so we can ensure that we only keep one Endpoint per URL if the same URL appears
    // more than once in the navigation links.
    Map<String, Endpoint> bookmarkableLinks = new HashMap<>();

    Elements ariaRoleNavigation = doc.select("*[role=navigation]").select("a[href]");
    for (Element navLink : ariaRoleNavigation) {
      String linkUrl = navLink.attr("abs:href");
      String linkText = navLink.text();
      if (linkText == null || linkText.isEmpty()) {
        continue; // Skip links where we could not recognize the anchor text.
      }

      Endpoint newBookmark = new Endpoint();
      newBookmark.setUrl(linkUrl);
      newBookmark.setName(linkText);
      bookmarkableLinks.put(linkUrl, newBookmark);
    }

    if (bookmarkableLinks.isEmpty()) {
      Elements likelyNavigationLinks = doc.select("nav, .nav, #nav, .navbar, #navbar, .navigation, #navigation").select("a[href]");
      for (Element navLink : likelyNavigationLinks) {
        String linkUrl = navLink.attr("abs:href");
        Endpoint bookmarkableLink = new Endpoint();
        bookmarkableLink.setUrl(linkUrl);
        bookmarkableLink.setName(navLink.text());
        scrubFields(bookmarkableLink);
        bookmarkableLinks.put(linkUrl, bookmarkableLink);
      }
    }

    return bookmarkableLinks.isEmpty() ? null : new ArrayList<>(bookmarkableLinks.values());
  }

  /**
   * No need to save timestamps and many other fields when scraping Endpoints.
   */
  private void scrubFields(Endpoint endpoint) {
//    endpoint.setEnabled(null);  // TODO:KOTLIN: Can’t fix this in Java because of auto-unboxing.
//    endpoint.setDisplay_order(null);  // TODO:KOTLIN: Can’t fix this in Java because of auto-unboxing.
    endpoint.setKey(null);
    endpoint.setIcon(null);
  }

  static class ManifestUnavailableException extends Exception {
    ManifestUnavailableException() {
      super("Did you call Scraper.fetch() first?");
    }
  }
}
