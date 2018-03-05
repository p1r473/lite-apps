package com.chimbori.hermitcrab.schema.manifest;

import com.google.gson.annotations.SerializedName;

public enum EndpointSource {
  @SerializedName("user")USER,
  @SerializedName("manifest")MANIFEST
}
