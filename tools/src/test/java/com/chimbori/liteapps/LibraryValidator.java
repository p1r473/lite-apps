package com.chimbori.liteapps;

import com.chimbori.FilePaths;

import org.junit.Test;

import java.io.IOException;

/**
 * Generates the library JSON file, updates tags.json, and reformat it.
 */
public class LibraryValidator {
  @Test
  public void testTagsJSONIsWellFormedAndReformat() throws IOException {
    TestHelpers.assertJsonIsWellFormedAndReformat(FilePaths.LITE_APPS_TAGS_JSON);
  }

  @Test
  public void testUpdateTagsJSON() throws IOException {
    TagsCollector.updateTagsJson();
  }

  @Test
  public void testLibraryDataIsGeneratedSuccessfully() throws IOException {
    LibraryGenerator.generateLibraryData();
  }
}
