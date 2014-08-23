package org.simpleframework.http.core;

import junit.framework.TestCase;

import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.http.message.ChunkedConsumer;
import org.simpleframework.transport.ByteCursor;

public class ChunkedProducerTest extends TestCase {

   public void testChunk() throws Exception {
      testChunk(1024, 1);
      testChunk(1024, 2);
      testChunk(512, 20);
      testChunk(64, 64);
   }
   
   public void testChunk(int chunkSize, int count) throws Exception {
      MockSender sender = new MockSender((chunkSize * count) + 1024);
      MockObserver monitor = new MockObserver();
      ChunkedConsumer validator = new ChunkedConsumer(new ArrayAllocator());
      ChunkedEncoder producer = new ChunkedEncoder(monitor, sender);
      byte[] chunk = new byte[chunkSize];
      
      for(int i = 0; i < chunk.length; i++) {
         chunk[i] = (byte)String.valueOf(i).charAt(0);
      }
      for(int i = 0; i < count; i++) {
         producer.encode(chunk, 0, chunkSize);
      }
      producer.close();
      
      System.err.println(sender.getBuffer().encode("UTF-8"));
      
      ByteCursor cursor = sender.getCursor();
      
      while(!validator.isFinished()) {
         validator.consume(cursor);
      }
      assertEquals(cursor.ready(), -1);
      assertTrue(monitor.isReady());
   }
}
