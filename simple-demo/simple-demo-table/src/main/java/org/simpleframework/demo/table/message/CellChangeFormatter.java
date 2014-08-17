package org.simpleframework.demo.table.message;

import java.text.DecimalFormat;

public class CellChangeFormatter {
   
   private final DecimalFormat format;

   public CellChangeFormatter() {
      this.format = new DecimalFormat("##.####");      
   }
   
   public String formatCell(Object value) {
      if(value != null) {
         if(value instanceof Double) {
            return format.format(value);
         }
         if(value instanceof Float) {
            return format.format(value);
         }
         return String.valueOf(value);
      }
      return "";
   }
}
