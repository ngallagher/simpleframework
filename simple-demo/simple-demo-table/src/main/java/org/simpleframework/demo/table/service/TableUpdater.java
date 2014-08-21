package org.simpleframework.demo.table.service;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;

import org.simpleframework.demo.table.Query;
import org.simpleframework.demo.table.TableCursor;
import org.simpleframework.demo.table.TableModel;
import org.simpleframework.demo.table.TableSubscription;
import org.simpleframework.demo.table.extract.RowExtractor;
import org.simpleframework.demo.table.format.RowFormatter;
import org.simpleframework.demo.table.schema.TableSchema;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.FrameChannel;
import org.simpleframework.util.thread.Daemon;

public class TableUpdater extends Daemon {   
  
   private final Set<TableConnection> ready;   
   private final RowExtractor extractor;
   private final RowFormatter formatter;
   private final Executor executor;
   private final TableSchema schema;
   private final TableModel model;
   
   public TableUpdater(TableModel model, TableSchema schema, RowExtractor extractor, RowFormatter formatter, Executor executor) {    
      this.ready = new CopyOnWriteArraySet<TableConnection>();
      this.executor = executor;
      this.formatter = formatter;
      this.extractor = extractor;      
      this.schema = schema;
      this.model = model;      
   }
   
   public void acknowledge(Session session, String table, long number) {
      for(TableConnection connection : ready) {
         try {
            connection.acknowledge(session, table, number);
         } catch(Exception e) {
            e.printStackTrace();
         }
      }
   }
   
   public void refresh(Session session) {
      for(TableConnection connection : ready) {
         try {
            connection.refresh(session);
         } catch(Exception e) {
            e.printStackTrace();
         }
      }
   }
  

   public void subscribe(FrameChannel socket, Query client) {
      TableSubscription subscription = model.subscribe(client);
      
      if(subscription != null) {
         TableSession session = new TableSession(socket);
         TableCursor cursor = new TableCursor(subscription, schema, extractor, formatter);
         TableConnection connection = new TableConnection(cursor, session, schema);
         
         ready.add(connection);
      }
   }
   
   public void run() {
      while(true) {
         try {
            Thread.sleep(200);
         
            for(TableConnection connection : ready) {
               long time = System.currentTimeMillis();
               try {
                  ready.remove(connection);
                  executor.execute(new TableRefresher(connection));
               }catch(Exception e){
                  System.err.println("ERROR AFTER " +(System.currentTimeMillis() - time) + " " + e);
                  //e.printStackTrace();
                  ready.remove(connection);
               }
            }
         } catch(Exception e) {
            e.printStackTrace();
         }
      }
   }
      
   public class TableRefresher implements Runnable {
      
      private final TableConnection connection;
      
      public TableRefresher(TableConnection connection) {
         this.connection = connection;
      }
      
      public void run() {
         try {            
            connection.update();
            ready.add(connection);
         } catch(Exception e) {
           e.printStackTrace();
         }
      }
      
   }
   
}
