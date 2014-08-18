package org.simpleframework.demo.table.format;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateCellFormatter implements CellFormatter {

   private final ThreadLocalDateFormat format;

   public DateCellFormatter(String format) {
      this.format = new ThreadLocalDateFormat(format);
   }

   @Override
   public String formatCell(Object value) {
      DateFormat local = format.get();
   
      if(value instanceof Date) {
         return local.format(value);
      }
      return String.valueOf(value);
   }

   private static class ThreadLocalDateFormat extends ThreadLocal<DateFormat> {
      
      private final String format;

      public ThreadLocalDateFormat(String format) {
         this.format = format;
      }

      @Override
      protected DateFormat initialValue() {
         return new SimpleDateFormat(format);
      }
   }
}
