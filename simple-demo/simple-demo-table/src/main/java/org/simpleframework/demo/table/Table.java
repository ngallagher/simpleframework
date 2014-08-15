package org.simpleframework.demo.table;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Table {
   
   private final List<TableRow> rows;
   private final TableRowAnnotator annotator;
   private final TableSchema schema;
   private final String key;
   
   public Table(String key, TableSchema schema, TableRowAnnotator annotator) {
      this.rows = new LinkedList<TableRow>();
      this.annotator = annotator;
      this.schema = schema;
      this.key = key;
   }   
   
   public String getKey(){
      return key;
   }
   
   public TableSchema getSchema(){
      return schema;
   }
   
   public int getRows(){
      return rows.size();
   }

   public TableRow updateRow(int index, String value) {
      Map<String, String> map = new HashMap<String, String>();
      String[] cells = value.split(",");
      for(String cell : cells){
         String[] pair = cell.split("=");
         map.put(pair[0], pair[1]);
      }
      return updateRow(index, map);
      
   }
   
   public TableRow updateRow(int index, Map<String, String> data) {    
      TableRow row = rows.get(index);
      if(row != null) {
         Set<String> columns = data.keySet();
         for(String column : columns) {
            if(!schema.validColumn(column)) {
               throw new IllegalArgumentException("Schema does not match row " + data);
            }
               
         }
         for(String column : columns){
            String value = data.get(column);
            TableCell tableCell = row.getValue(column);
            
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
   
   public TableRow addRow(String value) {
      Map<String, String> map = new HashMap<String, String>();
      String[] cells = value.split(",");
      for(String cell : cells){
         String[] pair = cell.split("=");
         map.put(pair[0], pair[1]);
      }
      return addRow(map);
      
   }
   
   public TableRow addRow(Map<String, String> data) {
      Set<String> columns = data.keySet();
      for(String column : columns) {
         if(!schema.validColumn(column)) {
            throw new IllegalArgumentException("Schema does not match row " + data);
         }            
      }
      int length = rows.size();
      TableRow row = new TableRow(schema, length);
      for(String column : columns){
         String value = data.get(column);
         row.setValue(column, value);
      }
      rows.add(row);
      return row;
   }
   
   public TableRow getRow(int row) {
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
         TableRow row = rows.get(i);
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
         TableRow row = rows.get(i);
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
