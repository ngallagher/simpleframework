package org.simpleframework.util.buffer.queue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import junit.framework.TestCase;

public class ArrayByteQueueTest extends TestCase {
   
   public void testArrayByteQueue() throws Exception {
      ArrayByteQueue queue = new ArrayByteQueue(10);
      
      for(int i = 0; i < 9; i++) {        
         queue.write(new byte[]{(byte)('A'+i)});
      }
      for(int i = 0; i < 9; i++) {
         byte[] b = new byte[1];
         queue.read(b);
         System.err.write(b);
         System.err.println();
      }
      for(int i = 9; i < 19; i++) {        
         queue.write(new byte[]{(byte)('A'+i)});
      }
      for(int i = 0; i < 9; i++) {
         byte[] b = new byte[1];
         queue.read(b);
         System.err.write(b);
         System.err.println();
      }
   }
   
   public void testRandomReadWrite() throws Exception {
      ArrayByteQueue queue = new ArrayByteQueue(1024 * 10);
      
      for(int i = 0; i < 100; i++) {
         String text = "Test: "+i;
         queue.write(text.getBytes());
      }
      for(int i = 0; i < 100; i++) {
         String text = "Test: "+i;
         byte[] buffer = new byte[256];
         int size = queue.read(buffer, 0, text.length());
         String result = new String(buffer, 0, size);
         System.err.println(result);
         assertEquals(result, text);         
      }
   }
   /*
   public void testStream() throws Exception {
      final ByteArrayOutputStream output = new ByteArrayOutputStream();
      final ArrayByteQueue queue = new ArrayByteQueue(1024 * 10);
      final Thread reader = new Thread(new Runnable() {
         public void run() {
            try {
               for(int i = 0; i < 100; i++) {
                  byte[] chunk = new byte[(int)Math.round((Math.random() * 100))];
                  int size = queue.read(chunk);                  
                  output.write(chunk, 0, size);  
               }
            } catch(Exception e) {
               e.printStackTrace();
            }
         }
      }); 
      final Thread writer = new Thread(new Runnable() {
         public void run() {
            try {
               ByteArrayOutputStream buffer = new ByteArrayOutputStream();
               ObjectOutputStream objectOutput = new ObjectOutputStream(buffer);  
               
               for(int i = 0; i < 100; i++) {
                  try {
                     TestMessage message = new TestMessage(i, "Test Message: " +i);                
                     objectOutput.writeObject(message);
                     objectOutput.flush();
                     byte[] messageBytes = buffer.toByteArray();                     
                     queue.write(messageBytes);
                     buffer.reset(); // clear out the buffer so toByteArray picks up changes only
                  } catch(Exception e) {
                     e.printStackTrace();
                  }
               }
            }catch(Exception e){
               e.printStackTrace();
            }
         }
      });
      writer.start();
      reader.start();
      writer.join();
      Thread.sleep(5000);
      reader.interrupt();
      reader.join();
      
      ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
      ObjectInputStream objectInput = new ObjectInputStream(input);
      
      for(int i = 0; i < 100; i++) {
         TestMessage message = (TestMessage)objectInput.readObject();
         assertEquals(message.count, i);
         assertEquals(message.text, "Test Message: "+i);
      }      
   }
*/
   private static class TestMessage implements Serializable {
      
      public final int count;
      public final String text;
      
      public TestMessage(int count, String text) {
         this.count = count;
         this.text = text;
      }      
   }
}
