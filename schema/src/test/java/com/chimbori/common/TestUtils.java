package com.chimbori.common;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class TestUtils {
  public static File getResource(Class clazz, String filename) throws ResourceNotFoundException {
    try {
      return Paths.get(clazz.getClassLoader().getResource(filename).toURI()).toFile();
    } catch (URISyntaxException | NullPointerException e) {
      // Catch NPE when a resource could not be found.
      throw new ResourceNotFoundException(filename);
    }
  }
}
