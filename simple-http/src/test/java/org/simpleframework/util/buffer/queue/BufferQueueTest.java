package org.simpleframework.util.buffer.queue;

import java.io.InputStream;

import junit.framework.TestCase;

public class BufferQueueTest extends TestCase {
   
   public void testBufferQueue() throws Exception {
      final ByteQueue queue = new ArrayByteQueue(1024 * 1000);
      final BufferQueue buffer = new BufferQueue(queue);
      
      Thread reader = new Thread(new Runnable() {
         public void run() {
            try {
               InputStream source = buffer.open();
               for(int i = 0; i < 1000; i++) {
                  int octet = source.read();
                  System.err.write(octet);
                  System.err.flush();
               }
            }catch(Exception e) {
               e.printStackTrace();
            }
         }
      });
      Thread writer = new Thread(new Runnable() {
         public void run() {
            try {
               for(int i = 0; i < 1000; i++) {                 
                  buffer.append(("Test message: "+i+"\n").getBytes());                  
               }
            }catch(Exception e) {
               e.printStackTrace();
            }
         }
      });
      reader.start();
      writer.start();
      reader.join();
      writer.join();
   }

}
