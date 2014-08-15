package org.simpleframework.http.socket.table;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketTableRow {

   private final Map<String, WebSocketTableCell> cells;
   private final WebSocketValueEncoder encoder;
   private final WebSocketTableSchema schema;
   private final int index;
   
   public WebSocketTableRow(WebSocketTableSchema schema, int index) {
      this.cells = new ConcurrentHashMap<String, WebSocketTableCell>();
      this.encoder = new WebSocketValueEncoder();
      this.index = index;
      this.schema = schema;
   }
   
   public int getIndex(){
      return index;
   }
   
   public void setValue(String column, String value){      
      WebSocketTableCell cell = getValue(column);
      
      if(cell == null) {
         WebSocketTableCell newCell = new WebSocketTableCell(column, value);
         List<String> columns = schema.columnNames();
         boolean match = false;
         for(String name : columns) {
            if(name.equals(column)) {
               match = true;
            }
         }
         if(!match) {
            throw new IllegalStateException("Could not find " + column + " in schema");
         }
         cells.put(column, newCell);
      } else {
         String previous = cell.getValue();
         
         if(previous != null && !previous.equals(value)) {
            WebSocketTableCell replaceCell = new WebSocketTableCell(column, value);
            cells.put(column, replaceCell);
         }
      }
   }
   
   public WebSocketTableCell getValue(String column) {
      return cells.get(column);
   }
   
   public String calculateChange(long lastUpdateDone) {
      StringBuilder builder = new StringBuilder();
      builder.append(index);
      builder.append(":");
      String delim = "";
      int count = 0;
      List<String> columns = schema.columnNames();
      for(int i = 0; i < columns.size(); i++){
         String column = columns.get(i);
         WebSocketTableCell cell = cells.get(column);
         if(cell != null) {
            long cellChanged = cell.getTimeStamp();
            long difference = cellChanged - lastUpdateDone;           
            
            if(difference > 0) { // positive means change happened later than update
               builder.append(delim);
               builder.append(i);
               builder.append("=");
               builder.append(cell.getValue());
               count++;
               delim = ",";
            }
         }
      }
      if(count <= 0) {
         return "";
      }
      return builder.toString();
   }
}
