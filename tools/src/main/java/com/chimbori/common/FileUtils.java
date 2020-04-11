package com.chimbori.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {
  private static final int BUFFER_SIZE = 8192;

  public static boolean zip(File rootDir, File zipFile) {
    zipFile.getParentFile().mkdirs();
    try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {
      out.setLevel(9);
      for (File containedFile : rootDir.listFiles(pathname -> !pathname.getName().equals(".DS_Store"))) {
        // Add files contained under the root directory, instead of the root directory itself,
        // so that the individual files appear at the root of the zip file instead of one directory down.
        addFileToZip(out, containedFile, "" /* parentDirectoryName */);
      }
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  private static void addFileToZip(ZipOutputStream zipOutputStream, File file, String parentDirectoryName) throws IOException {
    if (file == null || !file.exists()) {
      return;
    }

    String zipEntryName = file.getName();
    if (parentDirectoryName != null && !parentDirectoryName.isEmpty()) {
      zipEntryName = parentDirectoryName + "/" + file.getName();
    }

    if (file.isDirectory()) {
      for (File containedFile : file.listFiles()) {
        addFileToZip(zipOutputStream, containedFile, zipEntryName);
      }
    } else {
      byte[] buffer = new byte[BUFFER_SIZE];
      try (FileInputStream fis = new FileInputStream(file)) {
        ZipEntry zipEntry = new ZipEntry(zipEntryName);
        // Intentionally set the last-modified date to the epoch, so running the zip command
        // multiple times on the same (unchanged) source does not result in a different (binary)
        // zip file everytime.
        FileTime epochTime = FileTime.fromMillis(0);
        zipEntry.setCreationTime(epochTime);
        zipEntry.setLastModifiedTime(epochTime);
        zipEntry.setLastAccessTime(epochTime);
        zipOutputStream.putNextEntry(zipEntry);
        int length;
        while ((length = fis.read(buffer)) > 0) {
          zipOutputStream.write(buffer, 0, length);
        }
      } finally {
        zipOutputStream.closeEntry();
      }
    }
  }
}
