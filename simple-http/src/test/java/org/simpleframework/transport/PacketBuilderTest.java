package org.simpleframework.transport;

import java.io.IOException;
import java.nio.ByteBuffer;

import junit.framework.TestCase;

public class PacketBuilderTest extends TestCase {
   
   public void testBuilder() throws IOException {
      PacketBuilder builder = new PacketBuilder(3, 4096);
      byte[] chunk = new byte[1024];
      
      for(int i = 0; i < chunk.length; i++) {
         chunk[i] = (byte) 255;
      }
      ByteBuffer buffer = ByteBuffer.wrap(chunk);
      
      assertNull(builder.build(buffer.duplicate()));
      assertNull(builder.build(buffer.duplicate()));
      assertNull(builder.build(buffer.duplicate()));
      
      Packet packet = builder.build(buffer.duplicate());

      assertNotNull(packet);
      assertEquals(packet.sequence(), 0L);
      assertEquals(packet.length(), 4096);
      assertFalse(packet.isReference());
      
      chunk = new byte[8192];
      
      for(int i = 0; i < chunk.length; i++) {
         chunk[i] = (byte) 255;
      }
      buffer = ByteBuffer.wrap(chunk);
      packet = builder.build(buffer.duplicate());
      
      assertNotNull(packet);
      assertEquals(packet.sequence(), 1L);
      assertEquals(packet.length(), 8192);
      assertTrue(packet.isReference());
   }
   
   public void testPacketSize() throws IOException {
      PacketBuilder builder = new PacketBuilder(3, 4096);
      byte[] chunk = new byte[4096];
      
      for(int i = 0; i < chunk.length; i++) {
         chunk[i] = (byte) 255;
      }
      ByteBuffer buffer = ByteBuffer.wrap(chunk);
      Packet packet = builder.build(buffer.duplicate());
      
      assertNotNull(packet);
      assertEquals(packet.length(), 4096);
      assertFalse(packet.isReference());
      
      chunk = new byte[8192];
      
      for(int i = 0; i < chunk.length; i++) {
         chunk[i] = (byte) 255;
      }
      buffer = ByteBuffer.wrap(chunk);
      packet = builder.build(buffer.duplicate());
      
      assertNotNull(packet);
      assertEquals(packet.length(), 8192);
      assertTrue(packet.isReference());
   }

}
