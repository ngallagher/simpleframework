package org.simpleframework.util.buffer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

public class FileBufferTest extends TestCase {
   
   public void  testFileBuffer() throws Exception {
      File tempFile = File.createTempFile(FileBufferTest.class.getSimpleName(), null);
      Buffer buffer = new FileBuffer(tempFile);
      buffer.append("abcdefghijklmnopqrstuvwxyz".getBytes());
      
      Buffer alphabet = buffer.allocate();      
      alphabet.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
      
      Buffer digits = buffer.allocate();
      digits.append("0123456789".getBytes());
      
      expect(buffer, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".getBytes());
      expect(alphabet, "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
      expect(digits, "0123456789".getBytes());
   }
   
   private void expect(Buffer buffer, byte[] expect) throws IOException {
      InputStream result = buffer.open();
      
      for(int i  =0; i < expect.length; i++) {
         byte octet = expect[i];
         int value = result.read();
         
         if(value < 0) {
            throw new IOException("Buffer exhausted too early");
         }
         assertEquals(octet, (byte)value);
      }
      assertEquals(-1, result.read());
   }

}
