package org.example.util;

import org.springframework.util.StringUtils;

public class StringUtil {
  private static final String REGEX_FIND_WHITE_SPACE = "\\s";
  private static final String REGEX_FIND_HTML_TAGE =
      "<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>";

  public static String removeWhiteSpace(String... strings) {
    StringBuilder sb = new StringBuilder();
    for (String s : strings) {
      if (!StringUtils.hasText(s)) {
        continue;
      }
      sb.append(s.replaceAll(REGEX_FIND_WHITE_SPACE, ""));
    }
    return sb.toString();
  }

  public static String removeHtmlTag(String string) {
    if (!StringUtils.hasText(string)) {
      return "";
    }
    return string.replaceAll(REGEX_FIND_HTML_TAGE, "");
  }
}
