package com.chimbori.hermitcrab.schema.manifest;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

public enum ResourceIcon {
  @SerializedName("amazon")AMAZON,
  @SerializedName("bell")BELL,
  @SerializedName("calendar_today")CALENDAR_TODAY,
  @SerializedName("comment")COMMENT,
  @SerializedName("facebook")FACEBOOK,
  @SerializedName("heart")HEART,
  @SerializedName("lightbulb")LIGHTBULB,
  @SerializedName("mail")MAIL,
  @SerializedName("news")NEWS,
  @SerializedName("reddit")REDDIT,
  @SerializedName("star")STAR,
  @SerializedName("thumb_up")THUMB_UP,
  @SerializedName("tumblr")TUMBLR,
  @SerializedName("twitter")TWITTER;


  @Override
  public String toString() {
    return name().toLowerCase(Locale.US);
  }
}
