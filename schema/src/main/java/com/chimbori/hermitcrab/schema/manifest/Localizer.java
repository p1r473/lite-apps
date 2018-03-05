package com.chimbori.hermitcrab.schema.manifest;

/**
 * Temporary class during migration.
 */
public class Localizer {
  /**
   * Hermit follows the Chrome Extension model for i18n in manifests. Each manifest may contain
   * zero or more locale-specific messages.json files. Strings in the messages.json are of the form
   * some_key_with_underscores. To use the value of this string in the main manifest.json, a prefix
   * and suffix are added, which look like __MSG_some_key_with_underscores__. If the string value
   * in manifest.json does not begin with __MSG_, then it is assumed to be a non-localized string
   * and can be used as is. If it begins with __MSG_, then the appropriate matching key is looked up
   * in the messages.json file appropriate for the current locale, e.g. _locales/en/messages.json.
   */
  public static String getLocalizedString(String value, Messages messages) {
    if (messages == null) {
      return value;
    }
    if (value.startsWith("__MSG_") && value.endsWith("__")) {
      // This is an internationalized string.
      String i18nMessageKey = value.replaceAll("^__MSG_(.+)__$", "$1");
      if (messages.strings.containsKey(i18nMessageKey)) {
        return messages.strings.get(i18nMessageKey).message;
      }
    }
    return value;
  }
}
