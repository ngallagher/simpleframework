package org.simpleframework.http.message;

import java.text.DecimalFormat;

public class TimeCalculator {

   public static String calculateOpsPerSecond(long ops, long durationMs) {
      DecimalFormat format = new DecimalFormat("##################.#######");
      double opsPerMs = (double)ops / durationMs;
      return format.format(opsPerMs * 1000);
   }

   public static String calculateMicrosPerOp(long ops, long durationMs) {
      DecimalFormat format = new DecimalFormat("##################.#######");
      double millisPerOp = (double)durationMs / ops;
      return format.format(millisPerOp * 1000);
   }
}
