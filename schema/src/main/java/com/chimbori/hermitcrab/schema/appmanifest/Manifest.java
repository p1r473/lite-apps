package com.chimbori.hermitcrab.schema.appmanifest;

import java.util.List;

@SuppressWarnings({"CanBeFinal", "unused"})
public class Manifest {
  public String locale;
  public List<AppVersion> versions;
  public List<AssetArchive> blocklists;
  public List<AssetArchive> fonts;
  public List<AssetArchive> styles;
}
