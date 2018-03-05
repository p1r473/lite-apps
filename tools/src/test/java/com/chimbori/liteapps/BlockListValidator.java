package com.chimbori.liteapps;

import org.junit.Test;

import java.io.IOException;

public class BlockListValidator {
  /**
   * Whether running this test should download blocklist files from remote servers.
   * Set to false to speed up processing by avoiding a network fetch.
   */
  private final boolean SHOULD_DOWNLOAD;

  /**
   * Whether running this test should re-package blocklists.
   * Since blocklists don’t need to be updated every time a new Lite App is added, this
   * can be safely set to true unless explicitly updating the blocklists.
   */
  private final boolean SHOULD_PACKAGE;

  public BlockListValidator() {
    SHOULD_DOWNLOAD = Boolean.parseBoolean(System.getProperty("blocklists.download.enabled"));
    SHOULD_PACKAGE = Boolean.parseBoolean(System.getProperty("blocklists.packaging.enabled"));
  }

  @Test
  public void testAssembleAllBlockLists() throws IOException {
    if (SHOULD_DOWNLOAD) {
      BlockListPackager.downloadFromSources();
    }

    if (SHOULD_PACKAGE) {
      BlockListPackager.packageBlockLists();
    }
  }
}
