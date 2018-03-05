package com.chimbori.hermitcrab.schema.manifest;

import com.chimbori.hermitcrab.schema.common.DateFormats;
import com.chimbori.hermitcrab.schema.gson.Exclude;

import java.util.Date;

import nl.qbusict.cupboard.annotation.Column;

@SuppressWarnings("WeakerAccess")
public class Endpoint {
  @SuppressWarnings("unused") private static final String TAG = "Endpoint";

  // Database fields.
  @SuppressWarnings("unused") @Exclude
  public Long _id;

  @Exclude
  public Long shortcutId;

  // Basic fields.
  public String url;

  @Column("title")  // Current database schema refers to this field as "title", not "name".
  public String name;

  public Boolean enabled;

  @Exclude
  public Integer displayOrder;

  public EndpointRole role;
  public EndpointSource source;  // Who added this, the manifest, the user, or someone else.

  // Fields used only for Feeds.
  public String soundUri;
  public String soundTitle;

  @Column("vibratePattern")
  public String vibrate;  // Current database schema refers to this field as "vibratePattern", not "vibrate".

  public ResourceIcon icon;

  // Field used only for Monitors.
  @Column("monitorSelector")
  // Current database schema refers to this field as "monitorSelector", not "selector".
  public String selector;

  // Time metadata.
  @Exclude
  public final Long createdAtMs;
  @Exclude
  public Long modifiedAtMs;
  @Exclude
  public Long accessedAtMs;

  /**
   * A zero-argument constructor is needed for Cupboard.
   */
  public Endpoint() {
    createdAtMs = modifiedAtMs = System.currentTimeMillis();
    accessedAtMs = 0L;
  }

  public Endpoint withDefaults() {
    icon = ResourceIcon.COMMENT;
    enabled = true;
    return this;
  }

  public Endpoint url(String url) {
    this.url = url;
    return this;
  }

  public Endpoint title(String title) {
    this.name = title;
    return this;
  }

  public Endpoint shortcutId(long shortcutId) {
    this.shortcutId = shortcutId;
    return this;
  }

  public Endpoint role(EndpointRole role) {
    this.role = role;
    return this;
  }

  public Endpoint source(EndpointSource source) {
    this.source = source;
    return this;
  }

  public Endpoint sound(String soundUri, String soundTitle) {
    this.soundUri = soundUri;
    this.soundTitle = soundTitle;
    return this;
  }

  public Endpoint selector(String selector) {
    this.selector = selector;
    return this;
  }

  public Endpoint vibrate(String vibrate) {
    this.vibrate = vibrate;
    return this;
  }

  @Override
  public String toString() {
    return "Endpoint{" +
        "_id=" + _id +
        ", shortcutId=" + shortcutId +
        ", url='" + url + '\'' +
        ", title='" + name + '\'' +
        ", enabled=" + enabled +
        ", displayOrder=" + displayOrder +
        ", role='" + role + '\'' +
        ", source='" + source + '\'' +
        ", soundUri='" + soundUri + '\'' +
        ", soundTitle='" + soundTitle + '\'' +
        ", vibrate='" + vibrate + '\'' +
        ", icon='" + icon + '\'' +
        ", selector='" + selector + '\'' +
        ", createdAtMs=" + DateFormats.DATE_FORMAT_ISO_8601_DATE_TIME.format(new Date(createdAtMs)) +
        ", modifiedAtMs=" + DateFormats.DATE_FORMAT_ISO_8601_DATE_TIME.format(new Date(modifiedAtMs)) +
        ", accessedAtMs=" + DateFormats.DATE_FORMAT_ISO_8601_DATE_TIME.format(new Date(accessedAtMs)) +
        '}';
  }
}
