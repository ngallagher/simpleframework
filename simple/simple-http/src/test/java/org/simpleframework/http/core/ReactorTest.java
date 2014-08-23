package org.simpleframework.http.core;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.common.lease.Lease;
import org.simpleframework.http.MockTrace;
import org.simpleframework.http.Part;
import org.simpleframework.http.message.Body;
import org.simpleframework.http.message.Entity;
import org.simpleframework.http.message.Header;
import org.simpleframework.transport.Certificate;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.ByteWriter;
import org.simpleframework.transport.trace.Trace;

public class ReactorTest extends TestCase implements Controller {
   
   private static final String SOURCE =
   "POST /index.html HTTP/1.0\r\n"+
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
   
   public static class TestChannel implements Channel {
      
      private ByteCursor cursor;
      
      public TestChannel(StreamCursor cursor, int dribble) {
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

      public Lease getLease() {
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
   
   public void testHandler() throws Exception {
      testHandler(1024);
      
      for(int i = 10; i < 2048; i++) {
         testHandler(i);
      }
   }
   
   public void testHandler(int dribble) throws Exception {
      StreamCursor cursor = new StreamCursor(SOURCE);
      Channel channel = new TestChannel(cursor, dribble);
      
      start(channel);
      
      assertEquals(cursor.ready(), -1);
   }
   
   public void start(Channel channel) throws IOException {   
      start(new RequestCollector(new ArrayAllocator(), channel));
   }
   
   public void start(Collector collector) throws IOException {
      collector.collect(this);
   }
   
   public void select(Collector collector) throws IOException {
      collector.collect(this);
   }
   
   public void ready(Collector collector) throws IOException {
      Entity entity = collector;
      Channel channel = entity.getChannel();
      ByteCursor cursor = channel.getCursor();
      Header header = entity.getHeader();
      Body body = entity.getBody();
      List<Part> list = body.getParts();
      
      assertEquals(header.getTarget(), "/index.html");
      assertEquals(header.getMethod(), "POST");
      assertEquals(header.getMajor(), 1);
      assertEquals(header.getMinor(), 0);
      assertEquals(header.getContentType().getPrimary(), "multipart");
      assertEquals(header.getContentType().getSecondary(), "form-data");     
      assertEquals(header.getValue("Host"), "some.host.com");
      assertEquals(header.getValues("Accept").size(), 4);
      assertEquals(header.getValues("Accept").get(0), "image/gif");
      assertEquals(header.getValues("Accept").get(1), "image/png");
      assertEquals(header.getValues("Accept").get(2), "image/jpeg");
      assertEquals(header.getValues("Accept").get(3), "*");     
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
      assertEquals(cursor.ready(), -1); 
   }

   public void stop() throws IOException {}    
}
