package org.simpleframework.http.message;

import java.io.IOException;

import junit.framework.TestCase;

import org.simpleframework.http.core.DribbleCursor;
import org.simpleframework.http.core.StreamCursor;
import org.simpleframework.http.message.TokenConsumer;
import org.simpleframework.transport.Cursor;
import org.simpleframework.util.buffer.Allocator;
import org.simpleframework.util.buffer.ArrayAllocator;

public class TokenConsumerTest extends TestCase {
   
   public void testTokenConsumer() throws IOException {
      Allocator allocator = new ArrayAllocator();
      TokenConsumer consumer = new TokenConsumer(allocator, "\r\n".getBytes());
      Cursor cursor = new StreamCursor("\r\n");
      
      consumer.consume(cursor);
      
      assertEquals(cursor.ready(), -1);
      assertTrue(consumer.isFinished());  
   }
   
   public void testTokenConsumerException() throws IOException {
      Allocator allocator = new ArrayAllocator();
      TokenConsumer consumer = new TokenConsumer(allocator, "\r\n".getBytes());
      Cursor cursor = new StreamCursor("--\r\n");
      boolean exception = false;
      
      try {
         consumer.consume(cursor);
      } catch(Exception e) {
         exception = true;
      }
      assertTrue("Exception not thrown for invalid token", exception); 
   }
   
   public void testTokenConsumerDribble() throws IOException {
      Allocator allocator = new ArrayAllocator();
      TokenConsumer consumer = new TokenConsumer(allocator, "This is a large token to be consumed\r\n".getBytes());
      DribbleCursor cursor = new DribbleCursor(new StreamCursor("This is a large token to be consumed\r\n0123456789"), 1);

      consumer.consume(cursor);
      
      assertEquals(cursor.ready(), 1);
      assertTrue(consumer.isFinished()); 
      assertEquals(cursor.read(), '0');
      assertEquals(cursor.read(), '1');
      assertEquals(cursor.read(), '2');
   }

}
