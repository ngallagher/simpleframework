package org.simpleframework.demo.table.message;

import org.simpleframework.demo.table.extract.ColumnStyle;
import org.simpleframework.demo.table.extract.ValueEncoder;

public class ColumnStyleFormatter {
   
   private final ValueEncoder encoder;

   public ColumnStyleFormatter() {
      this.encoder = new ValueEncoder();
   }
   
   public String formatColumnStyle(ColumnStyle column) {      
      String name = column.getName();
      String template = column.getTemplate();
      String style = column.getStyle();
      String title = column.getTitle();
      boolean resizable = column.isResizable();
      boolean sortable = column.isSortable();
      boolean hidden = column.isHidden();
      
      if(template != null) {
         template = template.trim();
         template = template.replaceAll("\\s+", " ");
      }
      if(style != null) {
         style = style.trim();
         style = style.replaceAll("\\s+", " ");
      }
      return name + "," + encoder.encode(title) + "," + encoder.encode(template) + "," + encoder.encode(style) + "," + resizable + "," + sortable + "," + hidden;
   }
}
