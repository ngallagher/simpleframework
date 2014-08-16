package org.simpleframework.demo.table;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;

import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.WebSocket;
import org.simpleframework.http.socket.service.Service;

public class TableUpdater extends Thread implements Service {
   
   private final Set<TableSubscription> subscriptions;
   private final TableListener listener;
   private final TableRowChanger changer;
   private final TableSweeper sweeper;   
   private final Table table;
   private final AtomicLong time;
   
   public TableUpdater(String key, TableSchema schema, TableRowAnnotator annotator) {
      this.subscriptions = new CopyOnWriteArraySet<TableSubscription>();
      this.table = new Table(key, schema, annotator);
      this.sweeper = new TableSweeper(table);
      this.changer = new TableRowChanger(table);
      this.listener = new TableListener(this);
      this.time = new AtomicLong();
   }
   
   public void refresh(Session session) {
      for(TableSubscription subscription : subscriptions) {
         WebSocket socket = subscription.getSocket();  
         WebSocket other = session.getSocket();
         
         if(socket == other) {
            AtomicLong timeStamp = subscription.getTimeStamp();
            timeStamp.set(0);
         }
      }
   }
   
   public void run() {     
      changer.start();
      
      while(true) {
         try {
            Thread.sleep(100);
            
            for(TableSubscription subscription : subscriptions) {
               WebSocket socket = subscription.getSocket();            
               AtomicLong timeStamp = subscription.getTimeStamp();
               AtomicLong sendCount = subscription.getSendCount();
               long before = System.currentTimeMillis();
               long time = timeStamp.get();                
               long count = sendCount.get();
           
               try {                   
                  Set<Integer> missedUpdates = subscription.getMissedUpdates();
                  Map<TableUpdateType, String> messages = sweeper.sweep(missedUpdates, time - 1000, count);
                  Set<TableUpdateType> updates = messages.keySet();
                  
                  for(TableUpdateType update : updates) {
                     String message = messages.get(update);

                     if(message != null) {
                        socket.send(update.code + message);
                     }
                  }
               } catch(Exception e) {
                  e.printStackTrace();
                  subscriptions.remove(subscription);
                  socket.close();
               } finally {
                  sendCount.getAndIncrement();
                  timeStamp.set(before);
               }
            }
         } catch(Exception e) {
            e.printStackTrace();
         }
      }
   }

   public void connect(Session connection) {
      WebSocket socket = connection.getSocket();   
      
      try {
         TableSubscription subscription = new TableSubscription(socket);
               
         socket.register(listener);
         subscriptions.add(subscription);
         time.set(0);
         Thread.sleep(1000); // crap
         time.set(0);
      } catch(Exception e) {
         e.printStackTrace();
      }
      
   }
}