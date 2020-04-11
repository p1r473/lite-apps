package com.chimbori.liteapps

import com.chimbori.FilePaths
import com.chimbori.FilePaths.LITE_APPS_OUTPUT_DIR
import com.chimbori.FilePaths.LITE_APPS_SRC_DIR
import com.chimbori.FilePaths.MANIFEST_JSON_FILE_NAME
import com.chimbori.hermitcrab.schema.common.MoshiAdapter
import com.chimbori.hermitcrab.schema.manifest.Endpoint
import com.chimbori.hermitcrab.schema.manifest.EndpointRole
import com.chimbori.hermitcrab.schema.manifest.EndpointRole.*
import com.chimbori.hermitcrab.schema.manifest.IconFile.FAVICON_FILE
import com.chimbori.hermitcrab.schema.manifest.Manifest
import com.chimbori.liteapps.LiteAppPackager.packageManifest
import com.eclipsesource.json.Json
import com.eclipsesource.json.ParseException
import com.eclipsesource.json.WriterConfig
import okio.buffer
import okio.source
import org.junit.Assert
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.regex.Pattern
import javax.imageio.ImageIO

/**
 * A test that validates that each Lite App contains all the required fields in manifest.json.
 * Invalid behavior that should be added to this test:
 * - Invalid localizations (Text not correctly found in any messages.json).
 * - Missing localizations (manifest.json references a string, but string is not found in manifest.json).
 * - Extra files that are not part of the expected structure.
 */
@RunWith(Parameterized::class)
class LiteAppsValidator(private val liteApp: File) {
  @Before
  fun setUp() {
    LITE_APPS_OUTPUT_DIR.delete()
  }

  @Test
  fun testPackageAllLiteApps() {
    assertTrue("Packaging failed for " + liteApp.name, packageManifest(liteApp))
  }

  @Test
  fun testParseJSONStrictlyAndCheckWellFormed() {
    Files.walkFileTree(liteApp.toPath(), object : SimpleFileVisitor<Path>() {
      override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        if (attrs.isRegularFile && file.toFile().name.endsWith(".json")) {
          assertJsonIsWellFormedAndReformat(file.toFile())
        }
        return CONTINUE
      }

      override fun visitFileFailed(file: Path, e: IOException): FileVisitResult {
        Assert.fail(e.message)
        return CONTINUE
      }
    })
  }

  @Test
  fun testIconIs300x300() {
    val iconsDirectory = File(liteApp, FilePaths.ICONS_DIR_NAME)
    assertThatIconIs300x300(File(iconsDirectory, FAVICON_FILE.fileName))
  }

  @Test
  fun testManifestIsValid() {
    val manifestFile = File(liteApp, MANIFEST_JSON_FILE_NAME)
    val manifest = readManifest(manifestFile)
    val tag = liteApp.name
    assertNotNull(manifest)
    assertFieldExists(tag, "name", manifest!!.name)
    assertFieldExists(tag, "start_url", manifest.start_url)
    assertFieldExists(tag, "manifest_url", manifest.manifest_url)
    assertFieldExists(tag, "theme_color", manifest.theme_color)
    assertFieldExists(tag, "secondary_color", manifest.secondary_color)
    assertFieldExists(tag, "manifest_version", manifest.manifest_version)
    assertFieldExists(tag, "icon", manifest.icon)
    Assert.assertNotEquals(String.format("priority not defined for %s", liteApp),
        0, manifest.priority!!.toLong())

    // Test that the "manifest_url" field contains a valid URL.
    try {
      val manifestUrl = URL(manifest.manifest_url)
      Assert.assertEquals("https", manifestUrl.protocol)
      Assert.assertEquals("hermit.chimbori.com", manifestUrl.host)
      assertTrue(manifestUrl.path.startsWith("/lite-apps/"))
      assertTrue(manifestUrl.path.endsWith(".hermit"))
      Assert.assertEquals(liteApp.name + ".hermit", File(URLDecoder.decode(manifestUrl.file, "UTF-8")).name)
    } catch (e: MalformedURLException) {
      Assert.fail(e.message)
    }

    // Test that colors are valid hex colors.
    assertTrue(String.format("[%s] theme_color should be a valid hex color", tag),
        HEX_COLOR_PATTERN.matcher(manifest.theme_color).matches())
    assertTrue(String.format("[%s] secondary_color should be a valid hex color", tag),
        HEX_COLOR_PATTERN.matcher(manifest.secondary_color).matches())

    // Test that the name of the icon file is "icon.png" & that the file exists.
    // Although any filename should work, having it be consistent in the library can let us
    // avoid a filename lookup in automated tests and refactors.
    Assert.assertEquals(FAVICON_FILE, manifest.icon)
    val iconsDirectory = File(liteApp, FilePaths.ICONS_DIR_NAME)
    assertTrue(File(iconsDirectory, FAVICON_FILE.fileName).exists())

    // Test Endpoints for basic parseability.
    validateEndpoints(tag, manifest.bookmarks, BOOKMARK)
    validateEndpoints(tag, manifest.feeds, FEED)
    validateEndpoints(tag, manifest.share, SHARE)
    validateEndpoints(tag, manifest.search, SEARCH)
    validateEndpoints(tag, manifest.monitors, MONITOR)

    // Test all Settings to see whether they belong to our whitelisted set of allowable strings.
    MoshiAdapter.getAdapter(Manifest::class.java)
        .failOnUnknown()
        .fromJson(manifestFile.source().buffer())

    // Test "related_apps" for basic sanity, that if one exists, then it’s pointing to a Play Store app.
    if (manifest.related_applications != null) {
      for ((id, platform, url) in manifest.related_applications!!) {
        Assert.assertEquals(GOOGLE_PLAY, platform)
        Assert.assertFalse(id!!.isEmpty())
        assertTrue(url!!.startsWith("https://play.google.com/store/apps/details?id="))
        assertTrue(url.endsWith(id))
      }
    }
  }

  private fun validateEndpoints(tag: String, endpoints: Collection<Endpoint>?, role: EndpointRole) {
    if (endpoints != null) {
      for ((_, name, url, _, _, _, _, _, _, _, _, selector) in endpoints) {
        assertIsNotEmpty("Endpoint name should not be empty: $tag", name)
        assertIsURL("Endpoint should have a valid URL: $tag", url)
        if (role === SEARCH) {
          assertTrue(url, url!!.contains("%s"))
        } else if (role === SHARE) {
          assertTrue(url, url!!.contains("%s")
              || url.contains("%t")
              || url.contains("%u"))
        } else if (role === MONITOR) {
          assertIsNotEmpty("Endpoint name should not be empty: $tag", selector)
        }
      }
    }
  }

  private fun readManifest(file: File?): Manifest? {
    if (file == null || !file.exists()) {
      Assert.fail("Not found: " + file!!.absolutePath)
    }
    return try {
      if (file.exists()) MoshiAdapter.getAdapter(Manifest::class.java).fromJson(file.source().buffer()) else null
    } catch (e: IOException) {
      Assert.fail(String.format("Invalid JSON: %s", file.name))
      null
    }
  }

  companion object {
    private const val HEX_COLOR_REGEXP = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"
    private val HEX_COLOR_PATTERN = Pattern.compile(HEX_COLOR_REGEXP)
    private const val GOOGLE_PLAY = "play"

    @JvmStatic
    @Parameterized.Parameters
    fun listOfLiteApps(): Collection<*> {
      return listOf(*LITE_APPS_SRC_DIR.listFiles { obj: File -> obj.isDirectory })
    }

    private fun assertIsURL(message: String?, url: String?) {
      try {
        URL(url)
      } catch (e: MalformedURLException) {
        Assert.fail(message)
      }
    }

    private fun assertIsNotEmpty(message: String?, string: String?) {
      assertTrue(message, string != null && string.isNotEmpty())
    }

    private fun assertFieldExists(tag: String, field: String, value: Any?) {
      assertNotNull(String.format("File [%s] is missing the field [%s]", tag, field), value)
    }

    private fun assertJsonIsWellFormedAndReformat(file: File) {
      try {
        // Use a stricter parser than {@code Gson}, so we can catch issues such as
        // extra commas after the last element.
        val manifest = Json.parse(file.source().buffer().readUtf8())
        // Re-indent the <b>source file</b> by saving the JSON back to the same file.
        file.writeText(manifest.toString(WriterConfig.PRETTY_PRINT))
      } catch (e: ParseException) {
        Assert.fail(String.format("%s: %s", file.path, e.message))
      }
    }

    private fun assertThatIconIs300x300(icon: File) {
      var bufferedImage: BufferedImage? = null
      try {
        bufferedImage = ImageIO.read(icon)
      } catch (e: IOException) {
        Assert.fail(String.format("%s: %s", icon.path, e.message))
      }
      Assert.assertEquals(String.format("[%s] is not the correct size.", icon.path), 300, bufferedImage!!.width.toLong())
      Assert.assertEquals(String.format("[%s] is not the correct size.", icon.path), 300, bufferedImage.height.toLong())
    }
  }
}
