package com.chimbori.hermitcrab.schema.manifest;

import com.chimbori.hermitcrab.schema.gson.Exclude;

import nl.qbusict.cupboard.annotation.Column;
import nl.qbusict.cupboard.annotation.Ignore;

/**
 * Stores mappings from Lite Apps to Native Apps, from information available in the Lite App manifest.
 * Native Apps are identified using their ID, i.e. the package name used by Android. If the "id"
 * field is present in the Lite App manifest, it’s used, otherwise, the URL (which looks like
 * {"url":"https://play.google.com/store/apps/details?id=com.facebook.katana"} ) is parsed to get
 * the "id" field instead.
 * <p>
 * The name of this class is RelatedApp, not RelatedApplication, to match the existing database
 * schema. The field for this class in Manifest is still named related_applications.
 */
@SuppressWarnings("unused")
public class RelatedApp {
  private static final String PLAY_STORE_URL_TEMPLATE = "https://play.google.com/store/apps/details?id=";
  private static final String GOOGLE_PLAY = "play";

  // Database fields.

  @Exclude // Gson
  public Long _id;

  @Exclude // Gson
  public Long shortcutId;

  @Column("appId")
  public String id;

  @Ignore // Cupboard
  public String platform;

  @Ignore // Cupboard
  public String url;

  /**
   * Zero argument constructor required for Cupboard.
   */
  public RelatedApp() {
  }

  public RelatedApp(String appId) {
    this.id = appId;
    this.platform = GOOGLE_PLAY;
    this.url = PLAY_STORE_URL_TEMPLATE + appId;
  }
}
