package com.chimbori.liteapps

import com.chimbori.FilePaths
import com.chimbori.FilePaths.BLOCKLISTS_CONFIG_JSON
import com.chimbori.FilePaths.BLOCKLISTS_SRC_DIR
import com.chimbori.common.FileUtils
import com.chimbori.hermitcrab.schema.blocklists.BlockList
import com.chimbori.hermitcrab.schema.blocklists.Config
import com.chimbori.hermitcrab.schema.common.MoshiAdapter
import com.chimbori.hermitcrab.schema.common.SchemaDate.Companion.fromTimestamp
import okio.buffer
import okio.source
import java.io.File
import java.lang.Boolean.parseBoolean

/**
 * Parses a meta-list of block-lists, fetches the original blocklists from various remote URLs,
 * and combines them into a single JSON file suitable for consumption in Hermit.
 */
internal object BlockListPackager {
  /** Downloads and packages all block lists if the corresponding flags are set correctly in gradle.properties. */
  @JvmStatic
  fun main(arguments: Array<String>) {
    if (parseBoolean(System.getProperty("blocklists.download.enabled"))) {
      downloadFromSources()
    } else {
      System.err.println("Skipping downloadFromSources")
    }
    if (parseBoolean(System.getProperty("blocklists.packaging.enabled"))) {
      packageBlockLists()
    } else {
      System.err.println("Skipping packageBlockLists")
    }
  }

  /** Downloads all the meta-lists from index.json and saves them locally. */
  fun downloadFromSources() {
    val metaList = MoshiAdapter.get(Config::class.java).fromJson(BLOCKLISTS_CONFIG_JSON.source().buffer())
    metaList!!.packs!!.forEach { (displayName, fileName, sources) ->
      val blockListDirectory = File(BLOCKLISTS_SRC_DIR, displayName ?: fileName ?: "Default")
      blockListDirectory.mkdirs()
      sources!!.forEach { (url, fileName) ->
        // A blank URL means it’s a local file, so no need to fetch it from a remote server.
        if (url != null && !url.isEmpty()) {
          File(blockListDirectory, fileName).writeText(FileUtils.fetch(url))
        }
      }
    }
  }

  /** Package multiple blocklists into a single JSON file, as specified in index.json. */
  fun packageBlockLists() {
    val metaList = MoshiAdapter.get(Config::class.java).fromJson(BLOCKLISTS_CONFIG_JSON.source().buffer())
    metaList!!.packs!!.forEach { (displayName, fileName, sources) ->
      val hosts = mutableSetOf<String>()
      val blockListDirectory = File(BLOCKLISTS_SRC_DIR, displayName)
      println("Merging $fileName ($displayName)…")
      blockListDirectory.mkdirs()
      sources!!.forEach { (_, fileName) ->
        print("  - $fileName: ")
        // Since we don’t want to download the blocklists to keep the test hermetic, and we want to
        // still run the test on blocklists that are uploaded to the repo (i.e. first-party owned),
        // we skip adding hosts from a file if it doesn’t already exist.
        val blockListsFile = File(blockListDirectory, fileName!!)
        val hostsAdded = if (blockListsFile.exists()) {
          parseBlockList(blockListsFile, hosts)
        } else 0
        println("${hostsAdded} hosts")
      }

      val packagedBlockList = BlockList(
          name = fileName!!,
          updated = fromTimestamp(System.currentTimeMillis()),
          hosts = hosts.sortedBy { host -> host.toLowerCase() }
      )
      File(FilePaths.BLOCKLISTS_OUTPUT_DIR, fileName).writeText(
          MoshiAdapter.get(BlockList::class.java).toJson(packagedBlockList))
      println("  - Wrote ${hosts.size} hosts.")
    }
  }

  private fun parseBlockList(blockListsFile: File, hosts: MutableSet<String>): Int {
    var hostsAdded = 0
    blockListsFile.source().buffer().use {
      while (true) {
        var line = it.readUtf8Line()
            ?: return@use   // Null means EoF; empty means blank line.

        if (line.isEmpty() || line.startsWith(COMMENT)) {
          continue
        }

        line = line.replace(LOCAL_IP_V4, EMPTY)
            .replace(LOCAL_IP_V4_ALT, EMPTY)
            .replace(LOCAL_IP_V6, EMPTY)
            .replace(TAB, EMPTY)
        val comment = line.indexOf(COMMENT)
        if (comment >= 0) {
          line = line.substring(0, comment)
        }
        line = line.trim { it <= ' ' }
        if (!line.isEmpty() && line != LOCALHOST) {
          while (line.contains(SPACE)) {
            val space = line.indexOf(SPACE)
            if (addHostIfNotNullOrWhiteListed(line.substring(0, space), hosts)) {
              hostsAdded++
            }
            line = line.substring(space, line.length).trim { it <= ' ' }
          }
          if (addHostIfNotNullOrWhiteListed(line.trim { it <= ' ' }, hosts)) {
            hostsAdded++
          }
        }
      }
    }
    return hostsAdded
  }

  private fun addHostIfNotNullOrWhiteListed(host: String?, hosts: MutableSet<String>): Boolean {
    if (host != null && !isHostWhitelisted(host)) {
      hosts.add(host.trim { it <= ' ' })
      return true
    }
    return false
  }

  /**
   * In order to allow Hermit to continue to be distributed via Google Play, certain ads domains
   * cannot be blocked. We apologize for the inconvenience, but this is not in our control.
   */
  private fun isHostWhitelisted(host: String): Boolean {
    for (whitelistedSubstring in WHITELISTED_SUBSTRINGS) {
      if (host.contains(whitelistedSubstring)) {
        return true
      }
    }
    return false
  }

  private val WHITELISTED_SUBSTRINGS: List<String> = listOf("youtube")
  private const val LOCAL_IP_V4 = "127.0.0.1"
  private const val LOCAL_IP_V4_ALT = "0.0.0.0"
  private const val LOCAL_IP_V6 = "::1"
  private const val LOCALHOST = "localhost"
  private const val COMMENT = "#"
  private const val TAB = "\t"
  private const val SPACE = " "
  private const val EMPTY = ""
}
