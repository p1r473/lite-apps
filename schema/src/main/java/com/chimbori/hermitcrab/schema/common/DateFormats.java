package com.chimbori.hermitcrab.schema.common;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class DateFormats {
  public static final SimpleDateFormat DATE_FORMAT_ISO_8601_DATE_TIME;
  public static final SimpleDateFormat DATE_FORMAT_FILENAME_DATE_TIME;
  public static final SimpleDateFormat DATE_FORMAT_LOG_ENTRY_DATE_TIME;

  // Use only Arabic numerals in the Latin script [0-9] in all dates.
  // Use local timezones.
  static {
    DATE_FORMAT_ISO_8601_DATE_TIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.US);
    DATE_FORMAT_ISO_8601_DATE_TIME.setTimeZone(TimeZone.getDefault());

    DATE_FORMAT_FILENAME_DATE_TIME = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss", Locale.US);
    DATE_FORMAT_FILENAME_DATE_TIME.setTimeZone(TimeZone.getDefault());

    DATE_FORMAT_LOG_ENTRY_DATE_TIME = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss", Locale.US);
    DATE_FORMAT_LOG_ENTRY_DATE_TIME.setTimeZone(TimeZone.getDefault());
  }

  private DateFormats() {
  }
}
