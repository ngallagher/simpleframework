package org.simpleframework.http.core;

import org.simpleframework.http.core.Conversation;

import junit.framework.TestCase;

public class ConversationTest extends TestCase {
   
   private MockRequest request;   
   private MockResponse response;   
   private Conversation support;
   
   public void setUp() {
      request = new MockRequest();
      response = new MockResponse();
      support = new Conversation(request, response);
   }
   
   public void testWebSocket() {
      request.setMajor(1);
      request.setMinor(1);
      response.setValue("Connection", "upgrade");     
      
      assertFalse(support.isWebSocket());
      assertFalse(support.isTunnel());
      assertTrue(support.isKeepAlive());
      
      request.setValue("Upgrade", "WebSocket");
      
      assertFalse(support.isWebSocket());
      assertFalse(support.isTunnel());
      assertTrue(support.isKeepAlive());
      
      response.setCode(101);
      response.setValue("Upgrade", "websocket");
      
      assertTrue(support.isWebSocket());
      assertTrue(support.isTunnel());
      assertTrue(support.isKeepAlive());
   }
   
   public void testConnectTunnel() {
      request.setMajor(1);
      request.setMinor(1);
      response.setCode(404);
      request.setMethod("CONNECT");   
      
      assertFalse(support.isWebSocket());
      assertFalse(support.isTunnel());
      assertTrue(support.isKeepAlive());

      response.setCode(200);
      
      assertFalse(support.isWebSocket());
      assertTrue(support.isTunnel());
      assertTrue(support.isKeepAlive());
   }
   
   public void testResponse() {
      request.setMajor(1);
      request.setMinor(1);
      response.setValue("Content-Length", "10");
      response.setValue("Connection", "close");
      
      assertFalse(support.isKeepAlive());
      assertTrue(support.isPersistent());
      assertEquals(support.getContentLength(), 10);
      assertEquals(support.isChunkedEncoded(), false);
      
      request.setMinor(0);
      
      assertFalse(support.isKeepAlive());
      assertFalse(support.isPersistent());
      
      response.setValue("Connection", "keep-alive");
      
      assertTrue(support.isKeepAlive());
      assertFalse(support.isPersistent());
      
      response.setValue("Transfer-Encoding", "chunked");
      
      assertTrue(support.isChunkedEncoded());
      assertTrue(support.isKeepAlive());
   }
   
   public void testConversation() {
      request.setMajor(1);
      request.setMinor(1);
      support.setChunkedEncoded();
      
      assertEquals(response.getValue("Transfer-Encoding"), "chunked");
      assertEquals(response.getValue("Connection"), "keep-alive");
      assertTrue(support.isKeepAlive());
      assertTrue(support.isPersistent());
      
      request.setMinor(0);      
      support.setChunkedEncoded();
      
      assertEquals(response.getValue("Connection"), "close");
      assertFalse(support.isKeepAlive());      
      
      request.setMajor(1);
      request.setMinor(1);
      response.setValue("Content-Length", "10");
      response.setValue("Connection", "close");
      
      assertFalse(support.isKeepAlive());
      assertTrue(support.isPersistent());
      assertEquals(support.getContentLength(), 10);
      
      request.setMinor(0);
      
      assertFalse(support.isKeepAlive());
      assertFalse(support.isPersistent());
      
      response.setValue("Connection", "keep-alive");
      
      assertTrue(support.isKeepAlive());
      assertFalse(support.isPersistent());
      
      response.setValue("Transfer-Encoding", "chunked");
      
      assertTrue(support.isChunkedEncoded());
      assertTrue(support.isKeepAlive());
   }
}
