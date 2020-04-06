package com.chimbori.liteapps

internal class MissingManifestException(liteAppName: String) : RuntimeException("Error: Missing manifest.json for $liteAppName")
