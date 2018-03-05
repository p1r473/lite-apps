package com.chimbori.hermitcrab.schema.library;

@SuppressWarnings({"CanBeFinal", "unused"})
public class LibraryTag {
  public String name;

  public LibraryTag(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "LibraryTag{" +
        "name='" + name + '\'' +
        '}';
  }
}
