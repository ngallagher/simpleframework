package org.simpleframework.http.socket.table;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketTableChanger {

   private final Map<String, Integer> currentRows;
   private final WebSocketValueEncoder encoder;
   private final WebSocketTable table;

   public WebSocketTableChanger(WebSocketTable table) {
      this.currentRows = new ConcurrentHashMap<String, Integer>();
      this.encoder = new WebSocketValueEncoder();
      this.table = table;
   }

   public void onChange(Map<String, Object> values) {
      Map<String, String> row = new HashMap<String, String>();
      Map<String, String> header = new HashMap<String, String>();
      Set<String> columns = values.keySet();

      for (String column : columns) {
         Object value = values.get(column);
         String encoded = encoder.encode(value);

         row.put(column, encoded);
         header.put(column, column);
      }
      WebSocketTableRow headerRow = table.getRow(0);

      if (headerRow == null) {
         table.addRow(header);
      } else {
         for (String column : columns) {
            String name = header.get(column);
            headerRow.setValue(column, name);
         }
      }
      String key = table.getKey();
      Object keyAttribute = values.get(key);

      if (keyAttribute != null) {
         String tableKey = String.valueOf(keyAttribute);
         Integer index = currentRows.get(tableKey);

         if (index == null) {
            WebSocketTableRow newRow = table.addRow(row);
            index = newRow.getIndex();
            currentRows.put(tableKey, index);
         } else {
            table.updateRow(index, row);
         }
      }
   }
}
