package org.simpleframework.demo.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableSchema {
   
   private final Map<String, TableColumnStyle> columns;

   public TableSchema(Map<String, TableColumnStyle> columns) {     
      this.columns = columns;
   }
   
   public List<String> columnNames(){
      return new ArrayList<String>(columns.keySet());
   }
   
   public boolean validColumn(String name) {
      return columns.containsKey(name);
   }
   
   public String createStyle() {
      StringBuilder builder = new StringBuilder();
      Set<String> keys = columns.keySet();
      int count = 0;
      
      for(String key : keys){
         TableColumnStyle style = columns.get(key);
         String columnStyle = style.createStyle();
         
         if(count++ > 0) {
            builder.append("|");
         }
         builder.append(columnStyle);
      }
      return builder.toString();
   }
}
