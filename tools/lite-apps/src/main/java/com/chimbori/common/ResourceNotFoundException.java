package com.chimbori.common;

/**
 * When a requested test resource could not be loaded.
 */
public class ResourceNotFoundException extends Throwable {
  ResourceNotFoundException(String filename) {
    super(filename);
  }
}
