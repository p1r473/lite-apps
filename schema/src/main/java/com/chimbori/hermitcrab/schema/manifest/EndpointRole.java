package com.chimbori.hermitcrab.schema.manifest;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

public enum EndpointRole {
  @SerializedName("bookmark")BOOKMARK,
  @SerializedName("search")SEARCH,
  @SerializedName("feed")FEED,
  @SerializedName("share")SHARE,
  @SerializedName("monitor")MONITOR;

  @Override
  public String toString() {
    // The database names as well as serialized JSON names of each EndpointRole are the same as the
    // enum constant name, except in lower case.
    return name().toLowerCase(Locale.US);
  }
}
