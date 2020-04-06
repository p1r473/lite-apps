package com.chimbori.liteapps;

import com.chimbori.FilePaths;
import com.chimbori.common.FileUtils;
import com.chimbori.hermitcrab.schema.blocklists.BlockList;
import com.chimbori.hermitcrab.schema.blocklists.BlockListsLibrary;
import com.chimbori.hermitcrab.schema.blocklists.CombinedBlockList;
import com.chimbori.hermitcrab.schema.blocklists.SourceBlockList;
import com.chimbori.hermitcrab.schema.common.MoshiAdapter;
import com.chimbori.hermitcrab.schema.common.SchemaDate;
import com.google.common.collect.ImmutableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okio.Okio;

/**
 * Parses a meta-list of block-lists, fetches the original blocklists from various remote URLs,
 * and combines them into a single JSON file suitable for consumption in Hermit.
 */
class BlockListPackager {
  private static final List<String> WHITELISTED_SUBSTRINGS = ImmutableList.of(
      "youtube"
  );

  private static final String LOCAL_IP_V4 = "127.0.0.1";
  private static final String LOCAL_IP_V4_ALT = "0.0.0.0";
  private static final String LOCAL_IP_V6 = "::1";
  private static final String LOCALHOST = "localhost";
  private static final String COMMENT = "#";
  private static final String TAB = "\t";
  private static final String SPACE = " ";
  private static final String EMPTY = "";

  /**
   * Downloads and packages all block lists if the corresponding flags are set correctly in
   * gradle.properties.
   */
  public static void main(String[] arguments) throws IOException {
    if (Boolean.parseBoolean(System.getProperty("blocklists.download.enabled"))) {
      BlockListPackager.downloadFromSources();
    } else {
      System.err.println("Skipping downloadFromSources");
    }

    if (Boolean.parseBoolean(System.getProperty("blocklists.packaging.enabled"))) {
      BlockListPackager.packageBlockLists();
    } else {
      System.err.println("Skipping packageBlockLists");
    }
  }

  /**
   * Downloads all the meta-lists from index.json and saves them locally.
   */
  public static void downloadFromSources() throws IOException {
    BlockListsLibrary blockListsLibrary = readBlockListsLibrary();
    for (CombinedBlockList combinedBlockList : blockListsLibrary.getBlocklists()) {
      File blockListDirectory = new File(FilePaths.BLOCKLISTS_SRC_DIR, combinedBlockList.getBlocklist());
      blockListDirectory.mkdirs();
      for (SourceBlockList source : combinedBlockList.getSources()) {
        // A blank URL means it’s a local file, so no need to fetch it from a remote server.
        if (source.getUrl() != null && !source.getUrl().isEmpty()) {
          FileUtils.writeFile(new File(blockListDirectory, source.getName()), FileUtils.fetch(source.getUrl()));
        }
      }
    }
  }

  /**
   * Package multiple blocklists into a single JSON file, as specified in index.json.
   */
  public static void packageBlockLists() throws IOException {
    System.out.println(new File(".").getAbsolutePath());

    BlockListsLibrary blockListsLibrary = readBlockListsLibrary();

    for (CombinedBlockList combinedBlockList : blockListsLibrary.getBlocklists()) {
      Set<String> hosts = new HashSet<>();

      File blockListDirectory = new File(FilePaths.BLOCKLISTS_SRC_DIR, combinedBlockList.getBlocklist());
      blockListDirectory.mkdirs();

      for (SourceBlockList source : combinedBlockList.getSources()) {
        // Since we don’t want to download the blocklists to keep the test hermetic, and we want to
        // still run the test on blocklists that are uploaded to the repo (i.e. first-party owned),
        // we skip adding hosts from a file if it doesn’t already exist.
        File hostsList = new File(blockListDirectory, source.getName());
        if (hostsList.exists()) {
          parseBlockList(source.getName(), new FileInputStream(hostsList), hosts);
        }
      }

      writeToDisk(FilePaths.BLOCKLISTS_OUTPUT_DIR, combinedBlockList.getFilename(), hosts);
      hosts.clear();  // Empty the list before writing each one.
    }
  }

  private static void parseBlockList(String sourceName, InputStream inputStream, Set<String> hosts) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(inputStream));
      String line;
      int hostsAdded = 0;
      while ((line = reader.readLine()) != null) {
        if (!line.isEmpty() && !line.startsWith(COMMENT)) {
          line = line.replace(LOCAL_IP_V4, EMPTY)
              .replace(LOCAL_IP_V4_ALT, EMPTY)
              .replace(LOCAL_IP_V6, EMPTY)
              .replace(TAB, EMPTY);
          int comment = line.indexOf(COMMENT);
          if (comment >= 0) {
            line = line.substring(0, comment);
          }
          line = line.trim();
          if (!line.isEmpty() && !line.equals(LOCALHOST)) {
            while (line.contains(SPACE)) {
              int space = line.indexOf(SPACE);
              if (addHostIfNotNullOrWhiteListed(line.substring(0, space), hosts)) {
                hostsAdded++;
              }
              line = line.substring(space, line.length()).trim();
            }
            if (addHostIfNotNullOrWhiteListed(line.trim(), hosts)) {
              hostsAdded++;
            }
          }
        }
      }
      System.out.println(String.format("%s: %d", sourceName, hostsAdded));

    } catch (IOException e) {
      e.printStackTrace();

    } finally {
      if (reader == null)
        return;
      try {
        reader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static boolean addHostIfNotNullOrWhiteListed(String host, Set<String> hosts) {
    if (host != null && !isHostWhitelisted(host)) {
      hosts.add(host.trim());
      return true;
    }
    return false;
  }

  private static void writeToDisk(File rootDirectory, String relativeFileName, Set<String> hosts) throws IOException {
    List<String> hostsArray = new ArrayList<>(hosts);
    hostsArray.sort(String::compareToIgnoreCase);

    File jsonFile = new File(rootDirectory, relativeFileName);

    BlockList appManifestBlockList = new BlockList(
        jsonFile.getName(),
        SchemaDate.Companion.fromTimestamp(System.currentTimeMillis()),
        hostsArray
    );

    FileUtils.writeFile(jsonFile, MoshiAdapter.get(BlockList.class).toJson(appManifestBlockList));

    System.out.println(String.format("Wrote %d hosts.\n", hosts.size()));
  }

  /**
   * In order to allow Hermit to continue to be distributed via Google Play, certain ads domains
   * cannot be blocked. We apologize for the inconvenience, but this is not in our control.
   */
  private static boolean isHostWhitelisted(String host) {
    for (String whitelistedSubstring : WHITELISTED_SUBSTRINGS) {
      if (host.contains(whitelistedSubstring)) {
        return true;
      }
    }
    return false;
  }

  private static BlockListsLibrary readBlockListsLibrary() throws IOException {
    return MoshiAdapter.get(BlockListsLibrary.class)
        .fromJson(Okio.buffer(Okio.source(FilePaths.BLOCKLISTS_SOURCES_JSON)));
  }
}
