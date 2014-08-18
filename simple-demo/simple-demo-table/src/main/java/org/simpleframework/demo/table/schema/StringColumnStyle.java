package org.simpleframework.demo.table.schema;


public class StringColumnStyle implements ColumnStyle {

   private final String style;
   private final String template;
   private final String caption;
   private final String name;
   private final boolean sortable;
   private final boolean resizable;
   private final int width;

   public StringColumnStyle(String name, String template) {
      this(name, template, null);
   }
   
   public StringColumnStyle(String name, String template, String style) {
      this(name, template, style, name);
   }
   
   public StringColumnStyle(String name, String template,  String style, String caption) {
      this(name, template, style, caption, 60);
   }
   
   public StringColumnStyle(String name, String template,  String style, String caption, int width) {
      this(name, template, style, caption, width, true, false);
   }   
   
   public StringColumnStyle(String name, String template,  String style, String caption, int width, boolean resizable, boolean sortable) {
      this.template = template;
      this.resizable = resizable;
      this.sortable = sortable;
      this.caption = caption;
      this.width = width;
      this.style = style;
      this.name = name;
   }
   
   @Override
   public String getName() {
      return name;
   }
   
   @Override
   public String getStyle() {
      return style;
   }
   
   @Override
   public String getTemplate() {
      return template;
   }
   
   @Override
   public String getTitle() {
      return caption;
   }
   
   @Override
   public boolean isResizable() {
      return resizable;
   }
   
   @Override
   public boolean isSortable() {
      return sortable;
   }   
   
   @Override
   public boolean isHidden() {
      return false;
   } 
   
   @Override
   public int getWidth() {
      return width;
   }
}
