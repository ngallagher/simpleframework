package org.simpleframework.demo.table.service;

import org.simpleframework.demo.table.extract.ValueEncoder;
import org.simpleframework.demo.table.schema.ColumnStyle;

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
      int width = column.getWidth();
      
      if(template != null) {
         template = template.trim();
         template = template.replaceAll("\\s+", " ");
      }
      if(style != null) {
         style = style.trim();
         style = style.replaceAll("\\s+", " ");
      }
      if(title != null) {
         title = title.trim();
         title = title.replaceAll("\\s+", " ");        
      }
      template = encoder.encode(template);
      style = encoder.encode(style);
      title = encoder.encode(title);
      
      return name + "," + title + "," + template + "," + style + "," + resizable + "," + sortable + "," + hidden + "," + width;
   }
}
