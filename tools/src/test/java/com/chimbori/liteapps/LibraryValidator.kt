package com.chimbori.liteapps

import com.chimbori.liteapps.LibraryGenerator.generateLibraryData
import com.chimbori.liteapps.TagsCollector.updateTagsJson
import org.junit.Test

/** Generates the library JSON file, updates tags.json, and reformat it. */
class LibraryValidator {
  @Test
  fun testUpdateTagsJSON() {
    updateTagsJson()
  }

  @Test
  fun testLibraryDataIsGeneratedSuccessfully() {
    generateLibraryData()
  }
}
