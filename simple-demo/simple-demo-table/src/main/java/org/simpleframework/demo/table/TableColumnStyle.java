package org.simpleframework.demo.table;

public class TableColumnStyle {

   private final String template;
   private final String caption;
   private final String name;
   private final boolean sortable;
   private final boolean resizable;
   
   public TableColumnStyle(String name, String caption, String template, boolean resizable, boolean sortable) {
      this.name = name;
      this.caption = caption;
      this.template = template;
      this.resizable = resizable;
      this.sortable = sortable;
   }
   
   public String createStyle() {
      StringBuilder builder = new StringBuilder();
      TableValueEncoder encoder = new TableValueEncoder();
      
      builder.append(name);
      builder.append(",");
      builder.append(encoder.encode(caption));
      builder.append(",");      
      builder.append(encoder.encode(template));
      builder.append(",");
      builder.append(resizable);
      builder.append(",");
      builder.append(sortable);
      
      return builder.toString();
   }
}
