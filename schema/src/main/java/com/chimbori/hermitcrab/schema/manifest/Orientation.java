package com.chimbori.hermitcrab.schema.manifest;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Keep order in sync with {@code @string-array/orientation_entries}.
 */
public enum Orientation {
  @SerializedName("auto") AUTO,
  @SerializedName("portrait") PORTRAIT,
  @SerializedName("landscape") LANDSCAPE,
  @SerializedName("reverse_portrait") REVERSE_PORTRAIT,
  @SerializedName("reverse_landscape") REVERSE_LANDSCAPE;

  public static Orientation fromSnakeCaseString(String snakeCaseString) {
    for (Orientation orientation : Orientation.values()) {
      if (orientation.name().equalsIgnoreCase(snakeCaseString)) {
        return orientation;
      }
    }
    return AUTO;  // Default.
  }

  public static String[] toSnakeCaseStrings() {
    List<String> snakeCaseStrings = new ArrayList<>();
    for (Orientation orientation : Orientation.values()) {
      snakeCaseStrings.add(orientation.toString().toLowerCase(Locale.US));
    }
    return snakeCaseStrings.toArray(new String[snakeCaseStrings.size()]);
  }
}
