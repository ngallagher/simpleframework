package org.simpleframework.demo.table.extract;

public class StaticCellExtractor implements CellExtractor {
   
   private final String text;
   
   public StaticCellExtractor(String text) {
      this.text = text;
   }

   @Override
   public Object extract(Object value) {    
      return text;
   }

}
