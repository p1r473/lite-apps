package com.chimbori.liteapps;

import com.chimbori.liteapps.BlockListPackager;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class BlockListValidator {
  private static final boolean FETCH_REMOTE_FILES = false;

  @Test
  public void testAssembleAllBlockLists() {
    try {
      if (FETCH_REMOTE_FILES) {
        BlockListPackager.downloadFromSources();
      }
      BlockListPackager.packageBlockLists();
    } catch (IOException e) {
      Assert.fail(e.getMessage());
    }
  }
}
