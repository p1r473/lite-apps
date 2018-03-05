package com.chimbori.hermitcrab.schema.manifest;

import com.chimbori.common.FileUtils;
import com.chimbori.common.ResourceNotFoundException;
import com.chimbori.common.TestUtils;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MessagesTest {
  @Test
  public void testFromJson() throws ResourceNotFoundException, IOException {
    Messages messages = Messages.fromJson(FileUtils.readFully(
        new FileInputStream(TestUtils.getResource(getClass(), "messages.json"))));

    assertEquals("Photos", messages.strings.get("photos").message);
    assertEquals("Notifications", messages.strings.get("notifications").message);
    assertEquals("Settings", messages.strings.get("settings").message);
  }
}
