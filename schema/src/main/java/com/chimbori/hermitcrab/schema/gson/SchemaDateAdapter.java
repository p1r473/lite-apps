package com.chimbori.hermitcrab.schema.gson;

import com.chimbori.hermitcrab.schema.common.SchemaDate;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class SchemaDateAdapter implements JsonSerializer<SchemaDate>, JsonDeserializer<SchemaDate> {
  @Override
  public SchemaDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    if (!(json instanceof JsonPrimitive)) {
      throw new JsonParseException("The date should be a string value");
    }
    return SchemaDate.fromString(json.getAsString());
  }

  @Override
  public JsonElement serialize(SchemaDate src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(src.toString());
  }
}
