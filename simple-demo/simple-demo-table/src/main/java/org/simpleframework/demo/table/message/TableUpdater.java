package org.simpleframework.demo.table.message;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.simpleframework.demo.table.extract.Client;
import org.simpleframework.demo.table.extract.RowExtractor;
import org.simpleframework.demo.table.extract.TableCursor;
import org.simpleframework.demo.table.extract.TableModel;
import org.simpleframework.demo.table.extract.TableSchema;
import org.simpleframework.demo.table.extract.TableSubscription;
import org.simpleframework.http.socket.WebSocket;

public class TableUpdater extends Thread {   

   private final Set<TableConnection> connections;
   private final RowExtractor extractor;
   private final TableSchema schema;
   private final TableModel model;
   
   public TableUpdater(TableModel model, TableSchema schema, RowExtractor extractor) {
      this.connections = new CopyOnWriteArraySet<TableConnection>();
      this.extractor = extractor;      
      this.schema = schema;
      this.model = model;
   }
   
   public void refresh() {
      for(TableConnection connection : connections) {
         try {
            connection.refresh();
         } catch(Exception e) {
            e.printStackTrace();
         }
      }
   }
  

   public void subscribe(WebSocket socket, Client client) {
      TableSubscription subscription = model.subscribe(client);
      
      if(subscription != null) {
         TableSession session = new TableSession(socket);
         TableCursor cursor = new TableCursor(subscription, schema, extractor);
         TableConnection connection = new TableConnection(cursor, session, schema);
         
         connections.add(connection);
      }
   }
   
   public void run() {
      while(true) {
         try {
            Thread.sleep(500);
         
            for(TableConnection connection : connections) {
               try {
                  connection.update();
               }catch(Exception e){
                  e.printStackTrace();
                  connections.remove(connection);
               }
            }
         } catch(Exception e) {
            e.printStackTrace();
         }
      }
   }
      
      
   
}
