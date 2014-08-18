package org.simpleframework.demo.table.schema;

public class HiddenColumnStyle implements ColumnStyle {

   private final String name;

   public HiddenColumnStyle(String name) {
      this.name = name;
   }
   
   @Override
   public String getName() {
      return name;
   }
   
   @Override
   public String getStyle() {
      return null;
   }
   
   @Override
   public String getTemplate() {
      return null;
   }
   
   @Override
   public String getTitle() {
      return name;
   }
   
   @Override
   public boolean isResizable() {
      return false;
   }
   
   @Override
   public boolean isSortable() {
      return false;
   }   
   
   @Override
   public boolean isHidden() {
      return true;
   }   
   
   @Override
   public int getWidth() {
      return 0;
   }
}
