package org.simpleframework.util.buffer;

import junit.framework.TestCase;

public class BufferAllocatorTest extends TestCase {
   
   public void testBuffer() throws Exception {
      Allocator allocator = new ArrayAllocator(1, 2);
      Buffer buffer = new BufferAllocator(allocator, 1, 2);
      
      buffer.append(new byte[]{'a'}).append(new byte[]{'b'});
      
      assertEquals(buffer.encode(), "ab");
      assertEquals(buffer.encode("ISO-8859-1"), "ab"); 
      
      boolean overflow = false;
      
      try {
         buffer.append(new byte[]{'c'});
      } catch(Exception e) {
         overflow = true;
      }
      assertTrue(overflow);
      
      buffer.clear();
      
      assertEquals(buffer.encode(), "");
      assertEquals(buffer.encode("UTF-8"), "");
      
      allocator = new ArrayAllocator(1024, 2048);
      buffer = new BufferAllocator(allocator, 1024, 2048);    
      buffer.append("abcdefghijklmnopqrstuvwxyz".getBytes());
      
      Buffer alphabet = buffer.allocate();      
      alphabet.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
      
      Buffer digits = buffer.allocate();
      digits.append("0123456789".getBytes());
      
      assertEquals(alphabet.encode(), "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
      assertEquals(digits.encode(), "0123456789");
      assertEquals(buffer.encode(), "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789");
      
      Buffer extra = digits.allocate();
      extra.append("#@?".getBytes());
      
      assertEquals(extra.encode(), "#@?");
      assertEquals(extra.length(), 3);
      assertEquals(digits.encode(), "0123456789#@?");
      assertEquals(digits.length(), 13);
      assertEquals(buffer.encode(), "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789#@?");   
      assertEquals(buffer.length(), 65);
   }
   
   public void testCascadingBufferAllocator() throws Exception {
      Allocator allocator = new ArrayAllocator(1024, 2048);
      allocator = new BufferAllocator(allocator, 1024, 2048);
      allocator = new BufferAllocator(allocator, 1024, 2048);
      allocator = new BufferAllocator(allocator, 1024, 2048);
      allocator = new BufferAllocator(allocator, 1024, 2048);
      
      Buffer buffer = allocator.allocate(1024);
      
      buffer.append("abcdefghijklmnopqrstuvwxyz".getBytes());
      
      assertEquals(buffer.encode(), "abcdefghijklmnopqrstuvwxyz");
      
      buffer.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
      
      assertEquals(buffer.encode(), "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"); 
      assertEquals(buffer.length(), 52);  
   }

}
