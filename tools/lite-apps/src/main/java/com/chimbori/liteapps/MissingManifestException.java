package com.chimbori.liteapps;

class MissingManifestException extends RuntimeException {
  public MissingManifestException(String liteAppName) {
    super("Error: Missing manifest.json for " + liteAppName);
  }
}
