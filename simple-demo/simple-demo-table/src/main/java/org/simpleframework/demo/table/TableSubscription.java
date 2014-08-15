package org.simpleframework.demo.table;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.simpleframework.http.socket.WebSocket;

public class TableSubscription {

   private final Set<Integer> missedUpdates;
   private final AtomicLong timeStamp;
   private final WebSocket socket;
   private final AtomicLong send;
   private final AtomicLong received;
      
   public TableSubscription(WebSocket socket) {
      this.timeStamp = new AtomicLong();
      this.received = new AtomicLong();
      this.send = new AtomicLong();
      this.missedUpdates = new HashSet<Integer>();
      this.socket = socket;
   }
   
   public Set<Integer> getMissedUpdates() {
      return missedUpdates;
   }
   
   public AtomicLong getSendCount() {
      return send;
   }
   
   public AtomicLong getReceiveCount() {
      return received;
   }

   public AtomicLong getTimeStamp() {
      return timeStamp;
   }
   
   public WebSocket getSocket() {
      return socket;
   }
}
