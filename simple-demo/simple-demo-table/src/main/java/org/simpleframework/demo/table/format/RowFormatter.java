package org.simpleframework.demo.table.format;

import java.util.Collections;
import java.util.Map;

public class RowFormatter {

   private final Map<String, CellFormatter> formatters;
   
   public RowFormatter() {
      this(Collections.EMPTY_MAP);
   }

   public RowFormatter(Map<String, CellFormatter> formatters) {
      this.formatters = formatters;
   }

   public String formatRow(String name, Object value) {
      if (value != null) {
         CellFormatter formatter = formatters.get(name);

         if (formatter != null) {
            return formatter.formatCell(value);
         }
         return String.valueOf(value);
      }
      return null;
   }

}
