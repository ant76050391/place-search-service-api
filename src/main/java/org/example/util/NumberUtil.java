package org.example.util;

import java.text.DecimalFormat;

public class NumberUtil {
  private static String DIGIT_FORMAT = "#";

  public static double decimalFormat(double d, int count) {
    DecimalFormat df = new DecimalFormat("#." + DIGIT_FORMAT.repeat(count));
    return Double.parseDouble(df.format(d));
  }
}

