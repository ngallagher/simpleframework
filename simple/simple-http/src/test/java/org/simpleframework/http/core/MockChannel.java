package org.simpleframework.http.core;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import org.simpleframework.common.lease.Lease;
import org.simpleframework.http.MockTrace;
import org.simpleframework.transport.Certificate;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.Cursor;
import org.simpleframework.transport.Sender;
import org.simpleframework.transport.trace.Trace;


public class MockChannel implements Channel {
   
   private Cursor cursor;
   
   public MockChannel(Cursor cursor) {
      this.cursor = cursor;
   }
   
   public boolean isSecure() {
      return false;
   }
   
   public Trace getTrace(){
      return new MockTrace();
   }
   
   public Lease getLease() {
      return null;               
   }   

   public Certificate getCertificate() {
      return null;
   }
   
   public Cursor getCursor() {
      return cursor;
   }
   
   public Sender getSender() {
      return new MockSender();
   }
   
   public Map getAttributes() {
      return new HashMap();
   }
   
   public void close() {}

   public SocketChannel getSocket() {
      return null;
   }
}