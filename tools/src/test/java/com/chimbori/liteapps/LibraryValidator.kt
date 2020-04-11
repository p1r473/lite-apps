package com.chimbori.liteapps

import com.chimbori.liteapps.LibraryGenerator.generateLibraryData
import org.junit.Test

/** Generates the library JSON file, updates tags.json, and reformat it. */
class LibraryValidator {
  @Test
  fun testLibraryDataIsGeneratedSuccessfully() {
    generateLibraryData()
  }
}
