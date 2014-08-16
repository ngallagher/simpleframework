package org.simpleframework.demo.table.extract;

public class StringColumnStyle implements ColumnStyle {

   private final ValueEncoder encoder;
   private final String snippet;
   private final String caption;
   private final String name;
   private final boolean sortable;
   private final boolean resizable;

   public StringColumnStyle(String snippet, String name, String caption) {
      this(snippet, name, caption, true, false);
   }
   
   public StringColumnStyle(String snippet, String name, String caption, boolean resizable, boolean sortable) {
      this.encoder = new ValueEncoder();
      this.resizable = resizable;
      this.sortable = sortable;
      this.caption = caption;  
      this.snippet = snippet;
      this.name = name;
   }
   
   @Override
   public String getStyle() {
      String template = encoder.encode(snippet);
      String title = encoder.encode(caption);
      
      return String.format("%s,%s,%s,%s,%s,%s", name, title, template, resizable, sortable);
   }
}
