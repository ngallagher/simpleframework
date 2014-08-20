package org.simpleframework.demo.table.service;

import java.util.concurrent.atomic.AtomicLong;

import org.simpleframework.http.socket.WebSocket;

public class TableSession {
   
   private final AtomicLong received;
   private final AtomicLong send;
   private final WebSocket socket;
   
   public TableSession(WebSocket socket) {
      this.received = new AtomicLong();
      this.send = new AtomicLong();
      this.socket = socket;
   }   
   
   public WebSocket getSocket() {
      return socket;
   }
   
   public AtomicLong getSendCount() {
      return send;
   }
   
   public AtomicLong getReceiveCount() {
      return received;
   }
}
