package org.simpleframework.http.socket.table;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.simpleframework.http.socket.FrameChannel;

public class WebSocketTableSubscription {

   private final Set<Integer> missedUpdates;
   private final AtomicLong timeStamp;
   private final FrameChannel socket;
   private final AtomicLong send;
   private final AtomicLong received;
      
   public WebSocketTableSubscription(FrameChannel socket) {
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
   
   public FrameChannel getSocket() {
      return socket;
   }
}
