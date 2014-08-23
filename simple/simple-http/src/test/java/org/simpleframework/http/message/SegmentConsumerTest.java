package org.simpleframework.http.message;

import java.io.IOException;

import org.simpleframework.http.core.DribbleCursor;
import org.simpleframework.http.core.StreamCursor;
import org.simpleframework.http.message.SegmentConsumer;
import org.simpleframework.transport.Cursor;

import junit.framework.TestCase;

public class SegmentConsumerTest extends TestCase {
   
   private static final String SOURCE =
   "Content-Type: application/x-www-form-urlencoded\r\n"+
   "User-Agent:\r\n" +
   "Content-Length: 42\r\n"+
   "Transfer-Encoding: chunked\r\n"+
   "Accept: image/gif;q=1.0,\r\n image/jpeg;q=0.8,\r\n"+
   "   \t\t   image/png;\t\r\n\t"+
   "   q=1.0,*;q=0.1\r\n"+
   "Accept-Language: fr;q=0.1, en-us;q=0.4, en-gb; q=0.8, en;q=0.7\r\n"+
   "Host:   some.host.com    \r\n"+
   "Cookie: $Version=1; UID=1234-5678; $Path=/; $Domain=.host.com\r\n"+
   "Cookie: $Version=1; NAME=\"Niall Gallagher\"; $path=\"/\"\r\n"+
   "\r\n";
   
   private static final String EMPTY =
   "Accept-Language:\r\n"+
   "Content-Length:\r\n"+
   "Content-Type:\r\n"+
   "Content-Disposition:\r\n"+
   "Transfer-Encoding:\r\n"+
   "Expect:\r\n"+
   "Cookie:\r\n"+
   "\r\n";
   
   protected SegmentConsumer header;
   
   public void setUp() throws IOException {
      header = new SegmentConsumer();
   }
   
   public void testHeader() throws Exception {  
      Cursor cursor = new StreamCursor(SOURCE);
      
      while(!header.isFinished()) {
         header.consume(cursor);
      }      
      assertEquals(cursor.ready(), -1);
      assertEquals(header.getValue("Pragma"), null);
      assertEquals(header.getValue("User-Agent"), "");
      assertEquals(header.getValue("Content-Length"), "42");
      assertEquals(header.getValue("Content-Type"), "application/x-www-form-urlencoded");
      assertEquals(header.getValue("Host"), "some.host.com");
      assertEquals(header.getValues("Accept").size(), 4);
      assertEquals(header.getValues("Accept").get(0), "image/gif");
      assertEquals(header.getValues("Accept").get(1), "image/png");
      assertEquals(header.getValues("Accept").get(2), "image/jpeg");
      assertEquals(header.getValues("Accept").get(3), "*");
      assertEquals(header.getContentType().getPrimary(), "application");
      assertEquals(header.getContentType().getSecondary(), "x-www-form-urlencoded");
      assertEquals(header.getTransferEncoding(), "chunked");      
   }
   
   public void testEmptyHeader() throws Exception {  
      Cursor cursor = new StreamCursor(EMPTY);
      
      while(!header.isFinished()) {
         header.consume(cursor);
      }      
      assertEquals(cursor.ready(), -1);
      assertEquals(header.getValue("Accept-Language"), "");
      assertEquals(header.getValue("Content-Length"), "");
      assertEquals(header.getValue("Content-Type"), "");
      assertEquals(header.getValue("Content-Disposition"), "");
      assertEquals(header.getValue("Transfer-Encoding"), "");
      assertEquals(header.getValue("Expect"), "");
      assertEquals(header.getValue("Cookie"), "");
      assertEquals(header.getContentType().getPrimary(), null);
      assertEquals(header.getContentType().getSecondary(), null);   
   }
   
   public void testDribble() throws Exception {  
      Cursor cursor = new DribbleCursor(new StreamCursor(SOURCE), 1);
      
      while(!header.isFinished()) {
         header.consume(cursor);
      }      
      assertEquals(cursor.ready(), -1);
      assertEquals(header.getValue("Content-Length"), "42");
      assertEquals(header.getValue("Content-Type"), "application/x-www-form-urlencoded");
      assertEquals(header.getValue("Host"), "some.host.com");
      assertEquals(header.getValues("Accept").size(), 4);
      assertEquals(header.getValues("Accept").get(0), "image/gif");
      assertEquals(header.getValues("Accept").get(1), "image/png");
      assertEquals(header.getValues("Accept").get(2), "image/jpeg");
      assertEquals(header.getValues("Accept").get(3), "*");
      assertEquals(header.getContentType().getPrimary(), "application");
      assertEquals(header.getContentType().getSecondary(), "x-www-form-urlencoded");
      assertEquals(header.getTransferEncoding(), "chunked");      
   }
}
