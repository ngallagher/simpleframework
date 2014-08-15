package org.simpleframework.transport;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

public class BufferWrapperTest extends TestCase {
   
   public void testOrder() throws Exception {
      String text = "this is a test string";
      Packet buffer = new BufferWrapper(ByteBuffer.wrap(text.getBytes()), 1);
      
      assertEquals(buffer.length(), text.length());
      
      Packet copy = buffer.extract();
      
      assertEquals(copy.length(), text.length());
      assertEquals(copy.encode("ISO-8859-1"), text);
   }
   
   public void testZeroLength() throws Exception {
      Packet buffer = new BufferWrapper(ByteBuffer.wrap(new byte[0]), 1);
      
      assertEquals(buffer.length(), 0);
      
      Packet copy = buffer.extract();
      
      assertEquals(copy.length(), 0);
      
      Packet copyOfCopy = buffer.extract();
      
      assertEquals(copyOfCopy.length(), 0);
      assertEquals(copyOfCopy.encode("ISO-8859-1"), "");  
   }

}
