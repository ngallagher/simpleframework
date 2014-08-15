package org.simpleframework.util.buffer;

import junit.framework.TestCase;

public class FileByteQueueTest extends TestCase {
   
   public void testQueue() throws Exception {
     /* Allocator allocator = new FileAllocator();
      FileByteQueue queue = new FileByteQueue(allocator);
      for(int i = 0; i < 26; i++) {
         queue.write(new byte[]{(byte)(i+'a')}, 0, 1);
         System.err.println("WRITE>>"+(char)(i+'a'));
      }
      for(int i = 0; i < 26; i++) {
         byte[] buffer = new byte[1];
         assertEquals(queue.read(buffer, 0, 1), 1);
         System.err.println("READ>>"+((char)buffer[0]));
         assertEquals(buffer[0], (byte)(i+'a'));
      }*/
   }

}
