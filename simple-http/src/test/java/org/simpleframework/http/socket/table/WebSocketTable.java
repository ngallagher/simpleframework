package org.simpleframework.http.socket.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WebSocketTable {
   
   private final List<WebSocketTableRow> rows;
   private final WebSocketTableRowAnnotator annotator;
   private final WebSocketTableSchema schema;
   private final String key;
   
   public WebSocketTable(String key, WebSocketTableSchema schema, WebSocketTableRowAnnotator annotator) {
      this.rows = new LinkedList<WebSocketTableRow>();
      this.annotator = annotator;
      this.schema = schema;
      this.key = key;
   }   
   
   public String getKey(){
      return key;
   }
   
   public WebSocketTableSchema getSchema(){
      return schema;
   }
   
   public int getRows(){
      return rows.size();
   }

   public WebSocketTableRow updateRow(int index, String value) {
      Map<String, String> map = new HashMap<String, String>();
      String[] cells = value.split(",");
      for(String cell : cells){
         String[] pair = cell.split("=");
         map.put(pair[0], pair[1]);
      }
      return updateRow(index, map);
      
   }
   
   public WebSocketTableRow updateRow(int index, Map<String, String> data) {    
      WebSocketTableRow row = rows.get(index);
      if(row != null) {
         Set<String> columns = data.keySet();
         for(String column : columns) {
            if(!schema.validColumn(column)) {
               throw new IllegalArgumentException("Schema does not match row " + data);
            }
               
         }
         for(String column : columns){
            String value = data.get(column);
            WebSocketTableCell tableCell = row.getValue(column);
            
            if(tableCell == null) {
               row.setValue(column, value);
            } else {
               if(!tableCell.getValue().equals(value)) {
                  row.setValue(column, value);
               }
            }
         }
      }
      return row;
   }
   
   public WebSocketTableRow addRow(String value) {
      Map<String, String> map = new HashMap<String, String>();
      String[] cells = value.split(",");
      for(String cell : cells){
         String[] pair = cell.split("=");
         map.put(pair[0], pair[1]);
      }
      return addRow(map);
      
   }
   
   public WebSocketTableRow addRow(Map<String, String> data) {
      Set<String> columns = data.keySet();
      for(String column : columns) {
         if(!schema.validColumn(column)) {
            throw new IllegalArgumentException("Schema does not match row " + data);
         }            
      }
      int length = rows.size();
      WebSocketTableRow row = new WebSocketTableRow(schema, length);
      for(String column : columns){
         String value = data.get(column);
         row.setValue(column, value);
      }
      rows.add(row);
      return row;
   }
   
   public WebSocketTableRow getRow(int row) {
      int size = rows.size();
      
      if(row < size) {
         return rows.get(row);
      }
      return null;
   }
   
   public String calculateHighlight(long since) {
      StringBuilder builder = new StringBuilder();
      String delim = "";
      int size = rows.size();
      
      for(int i = 0; i < size; i++) {
         WebSocketTableRow row = rows.get(i);
         long time = since;         
         String text = annotator.calculateHighlight(row, time);
         
         if(text != null && text.length() > 0) {
            builder.append(delim);
            builder.append(text);
            delim = "|";            
         }         
      }
      return builder.toString();
   }
   
   public String calculateChange(long since) {
      StringBuilder builder = new StringBuilder();
      String delim = "";
      int size = rows.size();
      
      for(int i = 0; i < size; i++) {
         WebSocketTableRow row = rows.get(i);
         long time = since;         
         String text = row.calculateChange(time);
         
         if(text != null && text.length() > 0) {
            builder.append(delim);
            builder.append(text);
            delim = "|";            
         }         
      }
      return builder.toString();
   }   
   
   public void clearTable() {
      rows.clear();
   }
}
