package org.simpleframework.demo.table.service;

import java.util.concurrent.atomic.AtomicLong;

import org.simpleframework.http.socket.FrameChannel;

public class TableSession {
   
   private final AtomicLong received;
   private final AtomicLong send;
   private final FrameChannel socket;
   
   public TableSession(FrameChannel socket) {
      this.received = new AtomicLong();
      this.send = new AtomicLong();
      this.socket = socket;
   }   
   
   public FrameChannel getSocket() {
      return socket;
   }
   
   public AtomicLong getSendCount() {
      return send;
   }
   
   public AtomicLong getReceiveCount() {
      return received;
   }
}
