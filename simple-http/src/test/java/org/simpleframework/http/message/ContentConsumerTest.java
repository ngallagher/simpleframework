package org.simpleframework.http.message;

import java.io.IOException;

import junit.framework.TestCase;

import org.simpleframework.http.core.DribbleCursor;
import org.simpleframework.http.core.StreamCursor;
import org.simpleframework.http.message.ContentConsumer;
import org.simpleframework.http.message.PartData;
import org.simpleframework.util.buffer.Allocator;
import org.simpleframework.util.buffer.ArrayAllocator;
import org.simpleframework.util.buffer.Buffer;

public class ContentConsumerTest extends TestCase implements Allocator {
   
   private static final byte[] BOUNDARY = { 'A', 'a', 'B', '0', '3', 'x' };
   
   private Buffer buffer;
   
   public Buffer allocate() {
      return buffer;
   }
   
   public Buffer allocate(long size) {
      return buffer;
   }

   public void testContent() throws Exception {
      testContent(1, 1);
      
      for(int i = 1; i < 1000; i++) {
         testContent(i, i);
      }
      for(int i = 20; i < 1000; i++) {
         for(int j = 1; j < 19; j++) {
            testContent(i, j);
         }
      }
      testContent(10, 10);
      testContent(100, 2);
   }
   
   public void testContent(int entitySize, int dribble) throws Exception {
      MockSegment segment = new MockSegment();
      PartData list = new PartData();
      ContentConsumer consumer = new ContentConsumer(this, segment, list, BOUNDARY); 
      StringBuffer buf = new StringBuffer();
      
      segment.add("Content-Disposition", "form-data; name='photo'; filename='photo.jpg'");
      segment.add("Content-Type", "text/plain");
      segment.add("Content-ID", "<IDENTITY>");
      
      for(int i = 0, line = 0; buf.length() < entitySize; i++) {
         String text = String.valueOf(i);        
        
         line += text.length();
         buf.append(text);
         
         if(line >= 48) {
            buf.append("\n");           
            line = 0;
         }
      }
      // Get request body without boundary
      String requestBody = buf.toString();
      
      // Add the boundary to the request body
      buf.append("\r\n--");
      buf.append(new String(BOUNDARY, 0, BOUNDARY.length, "UTF-8"));
      buffer = new ArrayAllocator().allocate();
      
      DribbleCursor cursor = new DribbleCursor(new StreamCursor(buf.toString()), dribble);
      
      while(!consumer.isFinished()) {
         consumer.consume(cursor);
      }
      byte[] consumedBytes = buffer.encode("UTF-8").getBytes("UTF-8");
      String consumedBody = new String(consumedBytes, 0, consumedBytes.length, "UTF-8");
      
      assertEquals(String.format("Failed for entitySize=%s and dribble=%s", entitySize, dribble), consumedBody, requestBody);
      assertEquals(cursor.read(), '\r');
      assertEquals(cursor.read(), '\n');
      assertEquals(cursor.read(), '-');
      assertEquals(cursor.read(), '-'); 
      assertEquals(cursor.read(), BOUNDARY[0]);
      assertEquals(cursor.read(), BOUNDARY[1]);
      assertEquals(consumer.getPart().getContentType().getPrimary(), "text");
      assertEquals(consumer.getPart().getContentType().getSecondary(), "plain");
   }

   public void close() throws IOException {
      // TODO Auto-generated method stub
      
   }
   


}
