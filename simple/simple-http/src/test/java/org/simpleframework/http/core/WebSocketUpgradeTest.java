package org.simpleframework.http.core;

import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.http.MockTrace;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.transport.Certificate;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.ByteWriter;
import org.simpleframework.transport.trace.Trace;

public class WebSocketUpgradeTest extends TestCase implements Container {
   
   private static final String OPEN_HANDSHAKE =         
   "GET /chat HTTP/1.1\r\n"+
   "Host: server.example.com\r\n"+
   "Upgrade: websocket\r\n"+
   "Connection: Upgrade\r\n"+
   "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==\r\n"+
   "Origin: http://example.com\r\n"+
   "Sec-WebSocket-Protocol: chat, superchat\r\n"+
   "Sec-WebSocket-Version: 14\r\n" +
   "\r\n";
   
   public static class MockChannel implements Channel {
      
      private ByteCursor cursor;
      
      public MockChannel(StreamCursor cursor, int dribble) {
         this.cursor = new DribbleCursor(cursor, dribble);
      }
      public boolean isSecure() {
         return false;
      }
      
      public Trace getTrace() {
         return new MockTrace();
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
         return null;
      }
      
      public void close() {}

      public SocketChannel getSocket() {
         return null;
      }
   }
   
   private final BlockingQueue<Response> responses = new LinkedBlockingQueue<Response>();

   public void testWebSocketUpgrade() throws Exception {      
      Allocator allocator = new ArrayAllocator();
      Controller handler = new ContainerController(this, allocator, 10, 2);
      StreamCursor cursor = new StreamCursor(OPEN_HANDSHAKE);
      Channel channel = new MockChannel(cursor, 10);
      
      handler.start(channel);
      
      Response response = responses.poll(5000, TimeUnit.MILLISECONDS);
      
      assertEquals(response.getValue("Connection"), "Upgrade");
      assertEquals(response.getValue("Upgrade"), "websocket");
      assertTrue(response.isCommitted());
      assertTrue(response.isKeepAlive());
   }
   
   public void handle(Request request, Response response) {
      try {
         process(request, response);
         responses.offer(response);
      }catch(Exception e) {
         e.printStackTrace();
         assertTrue(false);
      }
   }
   
   public void process(Request request, Response response) throws Exception {
      String method = request.getMethod();
      
      assertEquals(method, "GET");
      assertEquals(request.getValue("Upgrade"), "websocket");
      assertEquals(request.getValue("Connection"), "Upgrade");      		
      assertEquals(request.getValue("Sec-WebSocket-Key"), "dGhlIHNhbXBsZSBub25jZQ==");
      
      response.setCode(101);
      response.setValue("Connection", "close");
      response.setValue("Upgrade", "websocket");
      
      OutputStream out = response.getOutputStream();
      
      out.write(10); // force commit
      
      assertTrue(response.isCommitted());
      assertTrue(response.isKeepAlive());
   }
   
   public static void main(String[] list) throws Exception {
      new ReactorProcessorTest().testMinimal();
   }

}
