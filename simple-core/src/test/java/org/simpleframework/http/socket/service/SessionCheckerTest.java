package org.simpleframework.http.socket.service;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.MockRequest;
import org.simpleframework.http.core.MockResponse;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameListener;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.WebSocket;

public class SessionCheckerTest extends TestCase {
   
   public void testSessionChecker() throws Exception {
      SessionChecker checker = new SessionChecker(1000, 2000);
      checker.start();
      
      MockSession session1 = new MockSession();
      MockSession session2 = new MockSession();
      
      checker.register(session1);
      checker.register(session2);
      
      Frame frame1 = session1.getSocket().next(2000L);
      Frame frame2 = session2.getSocket().next(2000L);
      
      assertNotNull(frame1);
      assertNotNull(frame2);
      
      assertEquals(session1.getSocket().count(), 0);
      assertEquals(session2.getSocket().count(), 0);

   }
   
   public class MockSession implements Session {
      
      private final MockWebSocket socket = new MockWebSocket();
      private final MockRequest request = new MockRequest();
      private final MockResponse response = new MockResponse();

      public MockWebSocket getSocket() {
         return socket;
      }

      public Request getRequest() {
         return request;
      }

      public Response getResponse() {
         return response;
      }

      public String getProtocol() {
         return "test-protocol";
      }

      public String getKey() {
         return "test-key";
      }      
   }
   
   private class MockWebSocket implements WebSocket {
      
      private final BlockingQueue<Frame> frames;
      
      public MockWebSocket() {
         this.frames = new LinkedBlockingQueue<Frame>();
      }
      public int count() throws Exception {
         return frames.size();
      }
      public Frame next(long duration) throws Exception {
         return frames.poll(duration, TimeUnit.MILLISECONDS);
      }
      public void send(byte[] data) throws IOException {}
      public void send(String text) throws IOException {}
      public void send(Frame frame) throws IOException {
         frames.offer(frame);
      }
      public void register(FrameListener listener) throws IOException {}
      public void remove(FrameListener listener) throws IOException {}
      public void close(Reason reason) throws IOException {}
      public void close() throws IOException {}
   }

}
