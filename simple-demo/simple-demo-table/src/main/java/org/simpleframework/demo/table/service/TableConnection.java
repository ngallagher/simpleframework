package org.simpleframework.demo.table.service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.simpleframework.demo.table.TableCursor;
import org.simpleframework.demo.table.extract.RowChange;
import org.simpleframework.demo.table.schema.TableSchema;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.WebSocket;

public class TableConnection {
   
   private final TableChangeFormatter tableFormatter;
   private final SchemaFormatter schemaFormatter;  
   private final TableSession session;
   private final TableCursor cursor;
   private final String table;
   
   public TableConnection(TableCursor cursor, TableSession session, TableSchema schema) {
      this.tableFormatter = new TableChangeFormatter(schema);
      this.schemaFormatter = new SchemaFormatter(schema);
      this.table = schema.getTable();
      this.session = session;
      this.cursor = cursor;
   }
   
   public void acknowledge(Session session, String table, long number) {
      WebSocket socket = session.getSocket();
      WebSocket expect = this.session.getSocket();
      
      if(socket == expect) {
         if(this.table.equals(table)) {
            AtomicLong counter = this.session.getReceiveCount();     
            counter.set(number);
         }
      }
   }
   
   public void update() throws IOException {
      AtomicLong sendCount = session.getSendCount();
      AtomicLong receiveCount = session.getReceiveCount();  
      long unacknowledged = sendCount.get() - receiveCount.get();      
      
      //if(unacknowledged < 3) {
         try {
            updateSchema();
            updateTable();         
         } finally {
            sendCount.getAndIncrement();
         }
     // }
   }
   
   public void updateSchema() throws IOException {
      WebSocket socket = session.getSocket();
      AtomicLong counter = session.getSendCount();                         
      long count = counter.get();
      
      if(count == 0) {
         String schema = schemaFormatter.formatSchema();
         
         if(schema != null) {
            socket.send(schema);
         }
      }
   }
   
   public void updateTable() throws IOException {
      WebSocket socket = session.getSocket();
      AtomicLong counter = session.getSendCount();      
      List<RowChange> changes = cursor.update();
      long count = counter.get();
      
      if(!changes.isEmpty()) {
         String message = tableFormatter.formatChanges(changes, count);
      
         if(message != null) {
            socket.send(message);
         }
      }
   }   
   
   public void refresh(Session session) throws IOException {
      WebSocket socket = session.getSocket();
      WebSocket expect = this.session.getSocket();
      
      if(socket == expect) {
         cursor.clear();
      }
   }
}
