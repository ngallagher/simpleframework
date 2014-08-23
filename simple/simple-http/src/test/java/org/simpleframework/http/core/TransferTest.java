package org.simpleframework.http.core;

import java.io.IOException;

import org.simpleframework.http.core.Conversation;
import org.simpleframework.http.core.ResponseEncoder;

import junit.framework.TestCase;

public class TransferTest extends TestCase {

   public void testTransferEncoding() throws IOException {
      MockChannel channel = new MockChannel(null);
      MockObserver monitor = new MockObserver();
      MockRequest request = new MockRequest();
      MockResponse response = new MockResponse();
      Conversation support = new Conversation(request, response);
      ResponseEncoder transfer = new ResponseEncoder(monitor, response, support, channel);
      
      // Start a HTTP/1.1 conversation
      request.setMajor(1);
      request.setMinor(1);
      transfer.start();
      
      assertEquals(response.getValue("Connection"), "keep-alive");
      assertEquals(response.getValue("Transfer-Encoding"), "chunked");
      assertEquals(response.getValue("Content-Length"), null);
      assertEquals(response.getContentLength(), -1);
      assertTrue(response.isCommitted());
      
      channel = new MockChannel(null);
      monitor = new MockObserver();
      request = new MockRequest();
      response = new MockResponse();
      support = new Conversation(request, response);
      transfer = new ResponseEncoder(monitor, response, support, channel);
      
      // Start a HTTP/1.0 conversation
      request.setMajor(1);
      request.setMinor(0);
      transfer.start();
      
      assertEquals(response.getValue("Connection"), "close");
      assertEquals(response.getValue("Transfer-Encoding"), null);
      assertEquals(response.getValue("Content-Length"), null);
      assertEquals(response.getContentLength(), -1);
      assertTrue(response.isCommitted());
   }
   
   public void testContentLength() throws IOException {
      MockChannel channel = new MockChannel(null);
      MockObserver monitor = new MockObserver();
      MockRequest request = new MockRequest();
      MockResponse response = new MockResponse();
      Conversation support = new Conversation(request, response);
      ResponseEncoder transfer = new ResponseEncoder(monitor, response, support, channel);
      
      // Start a HTTP/1.1 conversation
      request.setMajor(1);
      request.setMinor(1);
      transfer.start(1024);
      
      assertEquals(response.getValue("Connection"), "keep-alive");
      assertEquals(response.getValue("Content-Length"), "1024");
      assertEquals(response.getValue("Transfer-Encoding"), null);
      assertEquals(response.getContentLength(), 1024);
      assertTrue(response.isCommitted());
      
      channel = new MockChannel(null);
      monitor = new MockObserver();
      request = new MockRequest();
      response = new MockResponse();
      support = new Conversation(request, response);
      transfer = new ResponseEncoder(monitor, response, support, channel);
      
      // Start a HTTP/1.0 conversation
      request.setMajor(1);
      request.setMinor(0);
      transfer.start(1024);
      
      assertEquals(response.getValue("Connection"), "close");
      assertEquals(response.getValue("Content-Length"), "1024");
      assertEquals(response.getValue("Transfer-Encoding"), null);
      assertEquals(response.getContentLength(), 1024);
      assertTrue(response.isCommitted());
      
      channel = new MockChannel(null);
      monitor = new MockObserver();
      request = new MockRequest();
      response = new MockResponse();
      support = new Conversation(request, response);
      transfer = new ResponseEncoder(monitor, response, support, channel);
      
      // Start a HTTP/1.0 conversation
      request.setMajor(1);
      request.setMinor(1);
      response.setValue("Content-Length", "2048");
      response.setValue("Connection", "close");
      response.setValue("Transfer-Encoding", "chunked");
      transfer.start(1024);      
      
      assertEquals(response.getValue("Connection"), "close");
      assertEquals(response.getValue("Content-Length"), "1024"); // should be 1024
      assertEquals(response.getValue("Transfer-Encoding"), null);
      assertEquals(response.getContentLength(), 1024);
      assertTrue(response.isCommitted());
   }
   
   public void  testHeadMethodWithConnectionClose() throws IOException {
      MockChannel channel = new MockChannel(null);
      MockObserver monitor = new MockObserver();
      MockRequest request = new MockRequest();
      MockResponse response = new MockResponse();
      Conversation support = new Conversation(request, response);
      ResponseEncoder transfer = new ResponseEncoder(monitor, response, support, channel);

      request.setMajor(1);
      request.setMinor(0);
      request.setMethod("HEAD");
      request.setValue("Connection", "keep-alive");     
      response.setContentLength(1024);
      response.setValue("Connection", "close");
      
      transfer.start();
      
      assertEquals(response.getValue("Connection"), "close");
      assertEquals(response.getValue("Content-Length"), "1024"); // should be 1024
      assertEquals(response.getValue("Transfer-Encoding"), null);
      assertEquals(response.getContentLength(), 1024);
   }
   
   public void  testHeadMethodWithSomethingWritten() throws IOException {
      MockChannel channel = new MockChannel(null);
      MockObserver monitor = new MockObserver();
      MockRequest request = new MockRequest();
      MockResponse response = new MockResponse();
      Conversation support = new Conversation(request, response);
      ResponseEncoder transfer = new ResponseEncoder(monitor, response, support, channel);

      request.setMajor(1);
      request.setMinor(1);
      request.setMethod("HEAD");
      request.setValue("Connection", "keep-alive");     
      response.setContentLength(1024);
      
      transfer.start(512);
      
      assertEquals(response.getValue("Connection"), "keep-alive");
      assertEquals(response.getValue("Content-Length"), "512"); // should be 512
      assertEquals(response.getValue("Transfer-Encoding"), null);
      assertEquals(response.getContentLength(), 512);
   }
   
   public void testHeadMethodWithNoContentLength() throws IOException {
      MockChannel channel = new MockChannel(null);
      MockObserver monitor = new MockObserver();
      MockRequest request = new MockRequest();
      MockResponse response = new MockResponse();
      Conversation support = new Conversation(request, response);
      ResponseEncoder transfer = new ResponseEncoder(monitor, response, support, channel);

      request.setMajor(1);
      request.setMinor(1);
      request.setMethod("HEAD");
      request.setValue("Connection", "keep-alive");     
      
      transfer.start();
      
      assertEquals(response.getValue("Connection"), "keep-alive");
      assertEquals(response.getValue("Content-Length"), null); 
      assertEquals(response.getValue("Transfer-Encoding"), "chunked");
      assertEquals(response.getContentLength(), -1);
   }
   
   public void testHeadMethodWithNoContentLengthAndSomethingWritten() throws IOException {
      MockChannel channel = new MockChannel(null);
      MockObserver monitor = new MockObserver();
      MockRequest request = new MockRequest();
      MockResponse response = new MockResponse();
      Conversation support = new Conversation(request, response);
      ResponseEncoder transfer = new ResponseEncoder(monitor, response, support, channel);

      request.setMajor(1);
      request.setMinor(1);
      request.setMethod("HEAD");
      request.setValue("Connection", "keep-alive");     
      
      transfer.start(32);
      
      assertEquals(response.getValue("Connection"), "keep-alive");
      assertEquals(response.getValue("Content-Length"), "32"); 
      assertEquals(response.getValue("Transfer-Encoding"), null);
      assertEquals(response.getContentLength(), 32);
   }
}
