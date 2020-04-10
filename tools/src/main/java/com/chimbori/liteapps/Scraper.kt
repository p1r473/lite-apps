package com.chimbori.liteapps

import com.chimbori.hermitcrab.schema.manifest.Endpoint
import com.chimbori.hermitcrab.schema.manifest.Manifest
import com.chimbori.hermitcrab.schema.manifest.RelatedApp
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

/**
 * Scrapes a web site using JSoup to identify elements such as the name of a site, a dominant theme
 * color, and a list of likely top-level bookmarks. This may be used to bootstrap the manifest for
 * a Lite App.
 */
class Scraper(private val url: String) {
  private var doc: Document? = null

  fun fetch(): Scraper {
    try {
      doc = Jsoup.connect(url)
          .ignoreContentType(true)
          .userAgent(CHROME_MOBILE_USER_AGENT)
          .referrer(GOOGLE_COM_HOME_PAGE)
          .timeout(FETCH_TIMEOUT_MS)
          .followRedirects(true)
          .execute().parse()
    } catch (e: IOException) {
      System.err.println(e.message)
    }
    return this
  }

  fun extractManifest(): Manifest? {
    doc ?: return null  // Fetch failed or never fetched.

    val manifest = Manifest()
    // First try to get the Site’s name (not just the current page’s title) via OpenGraph tags.
    manifest.name = doc!!.select("meta[property=og:site_name]").attr("content")
    if (manifest.name == null || manifest.name!!.isEmpty()) {
      // But if the page isn’t using OpenGraph tags, then fallback to using the current page’s title.
      manifest.name = doc!!.select("title").text()
    }
    manifest.theme_color = doc!!.select("meta[name=theme-color]").attr("content")
    manifest.bookmarks = findBookmarkableLinks()
    manifest.feeds = findAtomAndRssFeeds()
    manifest.related_applications = findRelatedApps()
    return manifest
  }

  fun extractIconUrl(): String? {
    // The "abs:" prefix is a JSoup shortcut that converts this into an absolute URL.
    var iconUrl = doc!!.select("link[rel=apple-touch-icon]").attr("abs:href")
    if (iconUrl == null || iconUrl.isEmpty()) {
      iconUrl = doc!!.select("link[rel=apple-touch-icon-precomposed]").attr("abs:href")
    }
    return iconUrl
  }

  private fun findRelatedApps(): List<RelatedApp>? {
    val relatedApps: MutableList<RelatedApp> = ArrayList()
    val playStoreLinks = doc!!.select("a[href*=play.google.com]")
    for (playStoreLink in playStoreLinks) {
      val playStoreUrl = playStoreLink.attr("href")
      println(playStoreUrl)
      val matcher = Pattern.compile("id=([^&]+)").matcher(playStoreUrl)
      while (matcher.find()) {
        relatedApps.add(RelatedApp(matcher.group(1)))
      }
    }
    return if (relatedApps.isEmpty()) null else relatedApps
  }

  private fun findAtomAndRssFeeds(): MutableList<Endpoint>? {
    val feeds: MutableList<Endpoint> = ArrayList()
    val atomOrRssFeeds = doc!!.select("link[type=application/rss+xml], link[type=application/atom+xml]")
    for (feed in atomOrRssFeeds) {
      val feedLink = Endpoint()
      feedLink.url = feed.attr("abs:href")
      feedLink.name = feed.attr("title")
      scrubFields(feedLink)
      feeds.add(feedLink)
    }
    return if (feeds.isEmpty()) null else feeds
  }

  private fun findBookmarkableLinks(): MutableList<Endpoint>? {
    // Use a Map so we can ensure that we only keep one Endpoint per URL if the same URL appears
    // more than once in the navigation links.
    val bookmarkableLinks: MutableMap<String, Endpoint> = HashMap()
    val ariaRoleNavigation = doc!!.select("*[role=navigation]").select("a[href]")
    for (navLink in ariaRoleNavigation) {
      val linkUrl = navLink.attr("abs:href")
      val linkText = navLink.text()
      if (linkText == null || linkText.isEmpty()) {
        continue  // Skip links where we could not recognize the anchor text.
      }
      val newBookmark = Endpoint()
      newBookmark.url = linkUrl
      newBookmark.name = linkText
      bookmarkableLinks[linkUrl] = newBookmark
    }
    if (bookmarkableLinks.isEmpty()) {
      val likelyNavigationLinks = doc!!.select("nav, .nav, #nav, .navbar, #navbar, .navigation, #navigation").select("a[href]")
      for (navLink in likelyNavigationLinks) {
        val linkUrl = navLink.attr("abs:href")
        val bookmarkableLink = Endpoint()
        bookmarkableLink.url = linkUrl
        bookmarkableLink.name = navLink.text()
        scrubFields(bookmarkableLink)
        bookmarkableLinks[linkUrl] = bookmarkableLink
      }
    }
    return if (bookmarkableLinks.isEmpty()) null else ArrayList(bookmarkableLinks.values)
  }

  /**
   * No need to save timestamps and many other fields when scraping Endpoints.
   */
  private fun scrubFields(endpoint: Endpoint) {
    endpoint.enabled = null
    endpoint.display_order = null
    endpoint.key = null
    endpoint.icon = null
  }

  companion object {
    private const val FETCH_TIMEOUT_MS = 10000
    private const val CHROME_MOBILE_USER_AGENT = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.23 Mobile Safari/537.36"
    private const val GOOGLE_COM_HOME_PAGE = "https://www.google.com"
  }
}
