package org.simpleframework.http.socket.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WebSocketTableSchema {
   
   private final Map<String, WebSocketTableColumnStyle> columns;

   public WebSocketTableSchema(Map<String, WebSocketTableColumnStyle> columns) {     
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
         WebSocketTableColumnStyle style = columns.get(key);
         String columnStyle = style.createStyle();
         
         if(count++ > 0) {
            builder.append("|");
         }
         builder.append(columnStyle);
      }
      return builder.toString();
   }
}
