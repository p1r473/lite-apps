package com.chimbori.hermitcrab.schema.common;

import com.chimbori.hermitcrab.schema.gson.AnnotationExclusionStrategy;
import com.chimbori.hermitcrab.schema.gson.SchemaDateAdapter;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonInstance {
  private static Gson minifierInstance;
  private static Gson prettyPrinterInstance;
  private static Gson serializerInstance;

  public static Gson getMinifier() {
    if (minifierInstance == null) {
      minifierInstance = createBasicInstance()
          .setExclusionStrategies(new AnnotationExclusionStrategy())
          .create();
    }
    return minifierInstance;
  }

  @SuppressWarnings("unused")
  public static Gson getPrettyPrinter() {
    if (prettyPrinterInstance == null) {
      prettyPrinterInstance = createBasicInstance()
          .setPrettyPrinting()
          .setExclusionStrategies(new AnnotationExclusionStrategy())
          .create();
    }
    return prettyPrinterInstance;
  }

  /**
   * For internal use only; Serializer keeps all fields, even those marked exclude. This is used
   * when serializing/deserializing an Object for use within the app, as a replacement for
   * Parcelable.
   */
  public static Gson getSerializer() {
    if (serializerInstance == null) {
      serializerInstance = createBasicInstance().create();
    }
    return serializerInstance;
  }

  private static GsonBuilder createBasicInstance() {
    return new GsonBuilder()
        .registerTypeAdapter(SchemaDate.class, new SchemaDateAdapter())
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .setLenient();
  }
}
