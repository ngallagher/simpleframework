package org.simpleframework.http.socket.table;

import java.util.LinkedHashMap;
import java.util.Map;

public class WebSocketTableSweeper {

   private final WebSocketTable table;

   public WebSocketTableSweeper(WebSocketTable table) {
      this.table = table;     
   }

   public Map<WebSocketTableUpdateType, String> sweep(long time, long count) {
      Map<WebSocketTableUpdateType, String> messages = new LinkedHashMap<WebSocketTableUpdateType, String>();
      
      if(count <= 1) {
         WebSocketTableSchema schema = table.getSchema();
         String schemaUpdate = schema.createStyle();
         messages.put(WebSocketTableUpdateType.SCHEMA, schemaUpdate);
      }
      String highlightUpdate = table.calculateHighlight(time);
      String deltaUpdate = table.calculateChange(time);// really should only take small batches...
      
      highlightUpdate = count + "@" + System.currentTimeMillis() + ":" + highlightUpdate;
      deltaUpdate = count + "@" + System.currentTimeMillis() + ":" + deltaUpdate;
      
      messages.put(WebSocketTableUpdateType.HIGHLIGHT, highlightUpdate);
      messages.put(WebSocketTableUpdateType.DELTA, deltaUpdate);

      return messages;
   }
}
