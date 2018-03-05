package com.chimbori.hermitcrab.schema.manifest;

import com.chimbori.hermitcrab.schema.common.GsonInstance;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Schema for i18n messages based on the Hermit messages.json format, a subset of the Chrome
 * extensions manifest.json format.
 */
@SuppressWarnings("CanBeFinal")
public class Messages {
  public Map<String, Message> strings;

  public Messages() {
    strings = new HashMap<>();
  }

  public static Messages fromJson(String json) {
    Messages messages = new Messages();

    JsonParser parser = new JsonParser();
    JsonElement root = parser.parse(json);

    Set<Map.Entry<String, JsonElement>> entries = root.getAsJsonObject().entrySet();
    for (Map.Entry<String, JsonElement> entry : entries) {
      messages.strings.put(entry.getKey(),
          GsonInstance.getPrettyPrinter().fromJson(entry.getValue(), Message.class));
    }

    return messages;
  }
}
