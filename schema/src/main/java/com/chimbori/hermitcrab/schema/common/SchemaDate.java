package com.chimbori.hermitcrab.schema.common;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A very basic date class that completely ignores time.
 *
 * Compare to JodaTime’s LocalDate class, but without bringing in the entire JodaTime dependency.
 * Joda on Android requires processing the entire timezone database (stored as a resource), which
 * is both slow and wastes valuable space in the APK.
 *
 * For the purposes required here, a simple holder for YYYY-MM-DD type dates is sufficient.
 */
@SuppressWarnings("WeakerAccess")
public class SchemaDate {
  private static final String YYYY_MM_DD_REGEX = "^(\\d{4})\\-(0?[1-9]|1[012])\\-(0?[1-9]|[12][0-9]|3[01])$";
  private static final Pattern YYYY_MM_DD_PATTERN = Pattern.compile(YYYY_MM_DD_REGEX);

  public final int yyyy;
  public final int mm;
  public final int dd;

  public SchemaDate(int yyyy, int mm, int dd) {
    this.yyyy = yyyy;
    this.mm = mm;
    this.dd = dd;
  }

  public static SchemaDate fromTimestamp(long date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date(date));
    return new SchemaDate(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,  // Because java.util.Date is silly.
        calendar.get(Calendar.DAY_OF_MONTH));
  }

  public static SchemaDate fromString(String yyyymmdd) {
    if (yyyymmdd == null || yyyymmdd.isEmpty()) {
      throw new IllegalArgumentException("yyyymmdd must not be null");
    }

    Matcher matcher = YYYY_MM_DD_PATTERN.matcher(yyyymmdd);
    if (matcher.find()) {
      try {
        int yyyy = Integer.parseInt(matcher.group(1));
        int mm = Integer.parseInt(matcher.group(2));
        int dd = Integer.parseInt(matcher.group(3));
        return new SchemaDate(yyyy, mm, dd);
      } catch (NumberFormatException e) {
        // Proceed to throw below.
      }
    }
    throw new IllegalArgumentException(String.format("Unable to parse [%s]", yyyymmdd));
  }

  @Override
  public String toString() {
    return String.format(Locale.ENGLISH, "%04d-%02d-%02d", yyyy, mm, dd);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SchemaDate that = (SchemaDate) o;

    if (yyyy != that.yyyy) return false;
    if (mm != that.mm) return false;
    return dd == that.dd;

  }

  @Override
  public int hashCode() {
    int result = yyyy;
    result = 31 * result + mm;
    result = 31 * result + dd;
    return result;
  }
}
