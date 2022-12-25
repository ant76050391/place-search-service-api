package org.example.util;

public class StringUtil {
  private final static String REGEX_FIND_WHITE_SPACE = "\\s";
  private final static String REGEX_FIND_HTML_TAGE = "<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>";

  public static String removeWhiteSpace(String... strings) {
    StringBuilder sb = new StringBuilder();
    for (String s: strings) {
      sb.append(s.replaceAll(REGEX_FIND_WHITE_SPACE, ""));
    }
    return sb.toString();
  }

  public static String removeHtmlTag(String string) {
    return string.replaceAll(REGEX_FIND_HTML_TAGE, "");
  }
}
