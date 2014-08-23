package org.simpleframework.http.core;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;

import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.http.MockTrace;
import org.simpleframework.http.Part;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.ReactorTest.TestChannel;
import org.simpleframework.transport.Certificate;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.ByteWriter;
import org.simpleframework.transport.trace.Trace;

public class ReactorProcessorTest extends TestCase implements Container {
   
   private static final int ITERATIONS = 20000;
   
   private static final String MINIMAL =
   "HEAD /MINIMAL/%s HTTP/1.0\r\n" +   
   "Accept-Language: fr;q=0.1, en-us;q=0.4, en-gb; q=0.8, en;q=0.7\r\n"+
   "Host:   some.host.com    \r\n"+
   "\r\n";
      
   private static final String SIMPLE =
   "GET /SIMPLE/%s HTTP/1.0\r\n" +   
   "Accept: image/gif;q=1.0,\r\n image/jpeg;q=0.8,\r\n"+
   "   \t\t   image/png;\t\r\n\t"+
   "   q=1.0,*;q=0.1\r\n"+
   "Accept-Language: fr;q=0.1, en-us;q=0.4, en-gb; q=0.8, en;q=0.7\r\n"+
   "Host:   some.host.com    \r\n"+
   "Cookie: $Version=1; UID=1234-5678; $Path=/; $Domain=.host.com\r\n"+
   "Cookie: $Version=1; NAME=\"Niall Gallagher\"; $path=\"/\"\r\n"+
   "\r\n";
      
   private static final String UPLOAD =
   "POST /UPLOAD/%s HTTP/1.0\r\n" +
   "Content-Type: multipart/form-data; boundary=AaB03x\r\n"+
   "Accept: image/gif;q=1.0,\r\n image/jpeg;q=0.8,\r\n"+
   "   \t\t   image/png;\t\r\n\t"+
   "   q=1.0,*;q=0.1\r\n"+
   "Accept-Language: fr;q=0.1, en-us;q=0.4, en-gb; q=0.8, en;q=0.7\r\n"+
   "Host:   some.host.com    \r\n"+
   "Cookie: $Version=1; UID=1234-5678; $Path=/; $Domain=.host.com\r\n"+
   "Cookie: $Version=1; NAME=\"Niall Gallagher\"; $path=\"/\"\r\n"+
   "\r\n" +
   "--AaB03x\r\n"+
   "Content-Disposition: file; name=\"pics\"; filename=\"file1.txt\"; modification-date=\"Wed, 12 Feb 1997 16:29:51 -0500\"\r\n"+
   "Content-Type: text/plain\r\n\r\n"+
   "example contents of file1.txt\r\n"+   
   "--AaB03x\r\n"+   
   "Content-Type: multipart/mixed; boundary=BbC04y\r\n\r\n"+
   "--BbC04y\r\n"+
   "Content-Disposition: file; name=\"pics\"; filename=\"file2.txt\"\r\n"+
   "Content-Type: text/plain\r\n\r\n"+
   "example contents of file3.txt ...\r\n"+
   "--BbC04y\r\n"+
   "Content-Disposition: file; name=\"pics\"; filename=\"file3.txt\"\r\n"+
   "Content-Type: text/plain\r\n\r\n"+
   "example contents of file4.txt ...\r\n"+
   "--BbC04y\r\n"+
   "Content-Disposition: file; name=\"pics\"; filename=\"file4.txt\"\r\n"+
   "Content-Type: text/plain\r\n\r\n"+
   "example contents of file4.txt ...\r\n"+ 
   "--BbC04y--\r\n"+
   "--AaB03x--\r\n";
   
   private static class StopWatch {
      
      private long duration;
      
      private long start;
      
      public StopWatch() {
         this.start = System.currentTimeMillis();
      }
      
      public long time() {
         return duration;
      }
      
      public void stop() {
         duration = System.currentTimeMillis() - start;
      }
   }
   
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
         return null;
      }
      
      public Map getAttributes() {
         return null;
      }
      
      public void close() {}

      public SocketChannel getSocket() {
         return null;
      }
   }
   
   private ConcurrentHashMap<String, StopWatch> timers = new ConcurrentHashMap<String, StopWatch>();
   
   private LinkedBlockingQueue<StopWatch> finished = new LinkedBlockingQueue<StopWatch>();
   
   public void testMinimal() throws Exception {
      Controller handler = new ContainerController(this, new ArrayAllocator(), 10, 2);
            
      testRequest(handler, "/MINIMAL/%s", MINIMAL, "MINIMAL");
      testRequest(handler, "/SIMPLE/%s", SIMPLE, "SIMPLE");
      testRequest(handler, "/UPLOAD/%s", UPLOAD, "UPLOAD");
   }
   
   public void testRequest(Controller handler, String target, String payload, String name) throws Exception {
      long start = System.currentTimeMillis();
      
      for(int i = 0; i < ITERATIONS; i++) {
         String request = String.format(payload, i);
         StopWatch stopWatch = new StopWatch();
         
         timers.put(String.format(target, i), stopWatch);         
         testHandler(handler, request, 2048);
      }
      double sum = 0;
      
      for(int i = 0; i < ITERATIONS; i++) {
         StopWatch stopWatch = finished.take();
         sum += stopWatch.time();         
      }
      double total = (System.currentTimeMillis() - start);
      double count = ITERATIONS;
      
      System.err.println(String.format("%s total=[%s] for=[%s] average=[%s] time-per-request=[%s] request-per-millisecond=[%s] request-per-second=[%s]", 
            name, total, count, sum / count, total / count, count / total + 1, count / (total / 1000)));
   }
   
   public void testHandler(Controller handler, String payload, int dribble) throws Exception {      
      StreamCursor cursor = new StreamCursor(payload);
      Channel channel = new TestChannel(cursor, dribble);
      
      handler.start(channel);
   }
  
   
   public void handle(Request request, Response response) {
      try {
         process(request, response);
      }catch(Exception e) {
         e.printStackTrace();
         assertTrue(false);
      }
   }
   
   public void process(Request request, Response response) throws Exception {
      List<Part> list = request.getParts();
      String method = request.getMethod();
      
      if(method.equals("HEAD")) {
         assertEquals(request.getMajor(), 1);
         assertEquals(request.getMinor(), 0);     
         assertEquals(request.getValue("Host"), "some.host.com"); 
      } else if(method.equals("GET")) {      
         assertEquals(request.getMajor(), 1);
         assertEquals(request.getMinor(), 0);     
         assertEquals(request.getValue("Host"), "some.host.com");
         assertEquals(request.getValues("Accept").size(), 4);
         assertEquals(request.getValues("Accept").get(0), "image/gif");
         assertEquals(request.getValues("Accept").get(1), "image/png");
         assertEquals(request.getValues("Accept").get(2), "image/jpeg");
         assertEquals(request.getValues("Accept").get(3), "*"); 
      } else {
         assertEquals(request.getMajor(), 1);
         assertEquals(request.getMinor(), 0);
         assertEquals(request.getContentType().getPrimary(), "multipart");
         assertEquals(request.getContentType().getSecondary(), "form-data");     
         assertEquals(request.getValue("Host"), "some.host.com");
         assertEquals(request.getValues("Accept").size(), 4);
         assertEquals(request.getValues("Accept").get(0), "image/gif");
         assertEquals(request.getValues("Accept").get(1), "image/png");
         assertEquals(request.getValues("Accept").get(2), "image/jpeg");
         assertEquals(request.getValues("Accept").get(3), "*");     
         assertEquals(list.size(), 4);
         assertEquals(list.get(0).getContentType().getPrimary(), "text");
         assertEquals(list.get(0).getContentType().getSecondary(), "plain");
         assertEquals(list.get(0).getHeader("Content-Disposition"), "file; name=\"pics\"; filename=\"file1.txt\"; modification-date=\"Wed, 12 Feb 1997 16:29:51 -0500\"");
         assertEquals(list.get(0).getName(), "pics");
         assertEquals(list.get(0).getFileName(), "file1.txt");
         assertEquals(list.get(0).isFile(), true);
         assertEquals(list.get(1).getContentType().getPrimary(), "text");
         assertEquals(list.get(1).getContentType().getSecondary(), "plain");
         assertEquals(list.get(1).getHeader("Content-Disposition"), "file; name=\"pics\"; filename=\"file2.txt\"");
         assertEquals(list.get(1).getContentType().getPrimary(), "text");
         assertEquals(list.get(1).getName(), "pics");
         assertEquals(list.get(1).getFileName(), "file2.txt");
         assertEquals(list.get(1).isFile(), true);
         assertEquals(list.get(2).getContentType().getSecondary(), "plain");
         assertEquals(list.get(2).getHeader("Content-Disposition"), "file; name=\"pics\"; filename=\"file3.txt\"");
         assertEquals(list.get(2).getName(), "pics");
         assertEquals(list.get(2).getFileName(), "file3.txt");
         assertEquals(list.get(2).isFile(), true);
         assertEquals(list.get(3).getContentType().getPrimary(), "text");
         assertEquals(list.get(3).getContentType().getSecondary(), "plain");
         assertEquals(list.get(3).getHeader("Content-Disposition"), "file; name=\"pics\"; filename=\"file4.txt\"");
         assertEquals(list.get(3).getName(), "pics");
         assertEquals(list.get(3).getFileName(), "file4.txt");
         assertEquals(list.get(3).isFile(), true);
      }
      StopWatch stopWatch = timers.get(request.getTarget());
      stopWatch.stop();
      finished.offer(stopWatch);
   }
   
   public static void main(String[] list) throws Exception {
      new ReactorProcessorTest().testMinimal();
   }
}
