package org.simpleframework.http.core;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import org.simpleframework.common.lease.Lease;
import org.simpleframework.http.MockTrace;
import org.simpleframework.transport.Certificate;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.ByteWriter;
import org.simpleframework.transport.trace.Trace;


public class MockChannel implements Channel {
   
   private ByteCursor cursor;
   
   public MockChannel(ByteCursor cursor) {
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
   
   public ByteCursor getCursor() {
      return cursor;
   }
   
   public ByteWriter getWriter() {
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