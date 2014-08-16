package org.simpleframework.demo.table.message;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.simpleframework.demo.table.extract.RowChange;
import org.simpleframework.demo.table.extract.TableCursor;
import org.simpleframework.demo.table.extract.TableSchema;
import org.simpleframework.http.socket.WebSocket;

public class TableConnection {
   
   private final TableChangeFormatter tableFormatter;
   private final SchemaFormatter schemaFormatter;  
   private final TableSession session;
   private final TableCursor cursor;
   
   public TableConnection(TableCursor cursor, TableSession session, TableSchema schema) {
      this.tableFormatter = new TableChangeFormatter(schema);
      this.schemaFormatter = new SchemaFormatter(schema);
      this.session = session;
      this.cursor = cursor;
   }   
   
   public void update() throws IOException {
      updateSchema();
      updateTable();
   }
   
   public void updateSchema() throws IOException {
      WebSocket socket = session.getSocket();
      AtomicLong counter = session.getSendCount();                         
      long count = counter.get();
      
      if(count == 0) {
         String schema = schemaFormatter.formatSchema();
         
         if(schema != null) {
            socket.send(ChangeType.SCHEMA.code + schema);
         }
      }
   }
   
   public void updateTable() throws IOException {
      WebSocket socket = session.getSocket();
      AtomicLong counter = session.getSendCount();      
      List<RowChange> changes = cursor.update();         
      String message = tableFormatter.formatChanges(changes);
      
      if(message != null) {
         long time = System.currentTimeMillis();            
         long count = counter.get();
      
         socket.send(ChangeType.TABLE.code + count + "@" + time + ":" + message);
         counter.getAndIncrement();
      }
   }   
   
   public void refresh() throws IOException {
      cursor.clear();
   }
}
