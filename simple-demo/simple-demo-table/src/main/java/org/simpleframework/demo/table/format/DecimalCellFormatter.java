package org.simpleframework.demo.table.format;

import java.text.DecimalFormat;

public class DecimalCellFormatter implements CellFormatter {

   private final ThreadLocalDecimalFormat format;

   public DecimalCellFormatter(String format) {
      this.format = new ThreadLocalDecimalFormat(format);
   }

   @Override
   public String formatCell(Object value) {
      DecimalFormat local = format.get();
   
      if(value instanceof Number) {
         return local.format(value);
      }
      return String.valueOf(value);
   }

   private static class ThreadLocalDecimalFormat extends ThreadLocal<DecimalFormat> {
      
      private final String format;

      public ThreadLocalDecimalFormat(String format) {
         this.format = format;
      }

      @Override
      protected DecimalFormat initialValue() {
         return new DecimalFormat(format);
      }
   }
}
