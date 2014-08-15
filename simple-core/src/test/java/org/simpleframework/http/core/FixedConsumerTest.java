package org.simpleframework.http.core;

import java.io.IOException;

import junit.framework.TestCase;

import org.simpleframework.http.message.FixedConsumer;
import org.simpleframework.transport.Cursor;
import org.simpleframework.util.buffer.Allocator;
import org.simpleframework.util.buffer.ArrayAllocator;
import org.simpleframework.util.buffer.Buffer;

public class FixedConsumerTest extends TestCase implements Allocator {
   
   private Buffer buffer;
   
   public Buffer allocate() {
      return buffer;
   }
   
   public Buffer allocate(long size) {
      return buffer;
   }
   
   public void testConsumer() throws Exception {
      testConsumer(10, 10, 10);
      testConsumer(1024, 10, 1024);
      testConsumer(1024, 1024, 1024);
      testConsumer(1024, 1024, 1023);
      testConsumer(1024, 1, 1024);
      testConsumer(1, 1, 1);
      testConsumer(2, 2, 2);
      testConsumer(3, 1, 2);
   }
   
   public void testConsumer(int entitySize, int dribble, int limitSize) throws Exception {
      StringBuffer buf = new StringBuffer();
      
      // Ensure that we dont try read forever
      limitSize = Math.min(entitySize, limitSize);
      
      for(int i = 0, line = 0; i < entitySize; i++) {
         String text = "["+String.valueOf(i)+"]";        
        
         line += text.length();
         buf.append(text);
         
         if(line >= 48) {
            buf.append("\n");           
            line = 0;
         }

      }
      buffer = new ArrayAllocator().allocate();
      
      String requestBody = buf.toString();
      FixedConsumer consumer = new FixedConsumer(this, limitSize);
      Cursor cursor = new DribbleCursor(new StreamCursor(requestBody), dribble);
      byte[] requestBytes = requestBody.getBytes("UTF-8");
      
      while(!consumer.isFinished()) {
         consumer.consume(cursor);
      }
      byte[] consumedBytes = buffer.encode("UTF-8").getBytes("UTF-8");
      
      assertEquals(buffer.encode("UTF-8").length(), limitSize);
      
      for(int i = 0; i < limitSize; i++) {
         if(consumedBytes[i] != requestBytes[i]) {
            throw new IOException("Fixed consumer modified the request!");
         }
      }
   }

   public void close() throws IOException {
      // TODO Auto-generated method stub
      
   }

}
