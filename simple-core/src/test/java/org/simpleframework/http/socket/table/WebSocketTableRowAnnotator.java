package org.simpleframework.http.socket.table;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WebSocketTableRowAnnotator {
   
   private final WebSocketTableSchema schema;
   private final WebSocketValueEncoder encoder;
   
   public WebSocketTableRowAnnotator(WebSocketTableSchema schema) {
      this.encoder = new WebSocketValueEncoder();
      this.schema = schema;
   }

   public String calculateHighlight(WebSocketTableRow row, long lastUpdateDone) {      
      Map<String, String> pairs = new LinkedHashMap<String, String>();
      Map<String, String> values = new LinkedHashMap<String, String>();
      
      pairs.put("bidEFP", "bidEFPVolume");
      pairs.put("bidEFPVolume", "bidEFP");      
      pairs.put("offerEFP", "offerEFPVolume");
      pairs.put("offerEFPVolume", "offerEFP");      
      pairs.put("bidOutright", "bidOutrightVolume");
      pairs.put("bidOutrightVolume", "bidOutright");      
      pairs.put("offerOutright", "offerOutrightVolume");
      pairs.put("offerOutrightVolume", "offerOutright");
      
      StringBuilder builder = new StringBuilder();      
      int index = row.getIndex();
      builder.append(index);
      builder.append(":");
      String delim = "";
      int count = 0;
      List<String> columns = schema.columnNames();
      for(int i = 0; i < columns.size(); i++){
         String column = columns.get(i);         
         WebSocketTableCell cell = row.getValue(column);
         
         if(cell == null) {
            throw new IllegalStateException("Could not find column " + column);
         }
         String value = cell.getValue();
         
         if(cell != null) {
            long cellChanged = cell.getTimeStamp();
            long difference = cellChanged - lastUpdateDone;           
            
            if(difference > 0 || values.containsKey(column)) { // positive means change happened later than update
               builder.append(delim);
               builder.append(i);             
               builder.append("=");
               String doneAlready = values.get(column);
               
               if(doneAlready != null) {
                  builder.append(doneAlready);
               } else {
                  if(String.valueOf(value).indexOf("20") != -1 && (column.indexOf("bid") != -1 || column.indexOf("offer") != -1)) {
                     String style = encoder.encode("background-color: #32cd32;");
                     String other = pairs.get(column);
                     
                     if(other != null) {
                        values.put(other, style);
                     }
                     values.put(column, style);                 
                     builder.append(style);
                  } else {
                     String style = encoder.encode("");
                     String other = pairs.get(column);
                     
                     if(other != null) {
                        values.put(other, style);
                     }                     
                     values.put(column, style);                 
                     builder.append(style);
                  }
               }
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
