package org.simpleframework.demo.table;

import java.util.LinkedHashMap;
import java.util.Map;

public class TableSweeper {

   private final Table table;

   public TableSweeper(Table table) {
      this.table = table;     
   }

   public Map<TableUpdateType, String> sweep(long time, long count) {
      Map<TableUpdateType, String> messages = new LinkedHashMap<TableUpdateType, String>();
      
      if(count <= 1) {
         TableSchema schema = table.getSchema();
         String schemaUpdate = schema.createStyle();
         messages.put(TableUpdateType.SCHEMA, schemaUpdate);
      }
      String highlightUpdate = table.calculateHighlight(time);
      String deltaUpdate = table.calculateChange(time);// really should only take small batches...
      
      highlightUpdate = count + "@" + System.currentTimeMillis() + ":" + highlightUpdate;
      deltaUpdate = count + "@" + System.currentTimeMillis() + ":" + deltaUpdate;
      
      messages.put(TableUpdateType.HIGHLIGHT, highlightUpdate);
      messages.put(TableUpdateType.DELTA, deltaUpdate);

      return messages;
   }
}
