package com.chimbori.hermitcrab.schema.manifest;

import java.util.List;

/**
 * A representation of the W3C Web Manifest Format with additional vendor-specific fields under the
 * "hermit_*" prefix. This is meant to be a full-fidelity import/export format for Hermit, as well
 * as a full-fidelity format for fetching pre-configured Lite Apps from a curated library.
 */
@SuppressWarnings({"CanBeFinal", "unused"})
public class Manifest {
  public Integer manifestVersion;
  public String lang;
  public String name;
  public String startUrl;
  public String manifestUrl;
  public String themeColor;
  public String secondaryColor;
  public String display;

  public Settings hermitSettings;

  /** A unique random key used to identify this Lite App from others for the same hostname. */
  public String key;

  public IconFile icon;
  public MonogramIconMetadata monogram;

  public List<Endpoint> hermitBookmarks;
  public List<Endpoint> hermitFeeds;
  public List<Endpoint> hermitMonitors;
  public List<Endpoint> hermitSearch;
  public List<Endpoint> hermitShare;
  public List<RelatedApp> relatedApplications;

  // Library Metadata.
  public Integer priority;
  public List<String> tags;
}
