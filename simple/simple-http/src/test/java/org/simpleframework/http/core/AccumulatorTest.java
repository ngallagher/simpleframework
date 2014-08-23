package org.simpleframework.http.core;

import java.io.IOException;

import junit.framework.TestCase;

public class AccumulatorTest extends TestCase {
   
   public void testAccumulator() throws IOException {
      MockChannel channel = new MockChannel(null);
      MockObserver monitor = new MockObserver();
      MockRequest request = new MockRequest();
      MockResponse response = new MockResponse();
      Conversation support = new Conversation(request, response);
      ResponseBuffer buffer = new ResponseBuffer(monitor, response, support, channel);
      
      byte[] content = { 'T', 'E', 'S', 'T' }; 
      
      // Start a HTTP/1.1 conversation
      request.setMajor(1);
      request.setMinor(1);
      
      // Write to a zero capacity buffer
      buffer.expand(0);
      buffer.write(content, 0, content.length);
      
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
      buffer = new ResponseBuffer(monitor, response, support, channel);
      
      // Start a HTTP/1.0 conversation
      request.setMajor(1);
      request.setMinor(0);
      
      // Write to a zero capacity buffer
      buffer.expand(0);
      buffer.write(content, 0, content.length);
      
      assertEquals(response.getValue("Connection"), "close");
      assertEquals(response.getValue("Transfer-Encoding"), null);
      assertEquals(response.getValue("Content-Length"), null);
      assertEquals(response.getContentLength(), -1);
      assertTrue(response.isCommitted());
      
      channel = new MockChannel(null);
      monitor = new MockObserver();
      request = new MockRequest();
      response = new MockResponse();
      support = new Conversation(request, response);
      buffer = new ResponseBuffer(monitor, response, support, channel);
      
      // Start a HTTP/1.1 conversation
      request.setMajor(1);
      request.setMinor(1);
      
      // Write to a large capacity buffer
      buffer.expand(1024);
      buffer.write(content, 0, content.length);
      
      assertEquals(response.getValue("Connection"), null);
      assertEquals(response.getValue("Transfer-Encoding"), null);
      assertEquals(response.getValue("Content-Length"), null);
      assertEquals(response.getContentLength(), -1);
      assertFalse(response.isCommitted());
      assertFalse(monitor.isReady());
      assertFalse(monitor.isClose());
      assertFalse(monitor.isError());
      
      // Flush the buffer
      buffer.close();
      
      assertEquals(response.getValue("Connection"), "keep-alive");
      assertEquals(response.getValue("Transfer-Encoding"), null);
      assertEquals(response.getValue("Content-Length"), "4");
      assertEquals(response.getContentLength(), 4);
      assertTrue(response.isCommitted());
      assertTrue(monitor.isReady());
      assertFalse(monitor.isClose());
      assertFalse(monitor.isError());

      boolean catchOverflow = false;
      
      try {
         buffer.write(content, 0, content.length);
      } catch(Exception e) {
         catchOverflow = true;
      }
      assertTrue(catchOverflow);
   }
}
