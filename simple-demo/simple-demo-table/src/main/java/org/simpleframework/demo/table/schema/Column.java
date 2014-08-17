package org.simpleframework.demo.table.schema;


public class Column {

   private final ColumnStyle style;
   private final String name;
   private final int index;
   
   public Column(ColumnStyle style, String name, int index) {
      this.style = style;
      this.name = name;
      this.index = index;
   }
   
   public ColumnStyle getStyle() {
      return style;
   }
   
   public String getName() {
      return name;
   }
   
   public int getIndex(){ 
      return index;
   }
   
   @Override
   public String toString() {
      return String.format("%s@%s", name, index);
   }
}
