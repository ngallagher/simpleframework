package org.simpleframework.transport;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

public class BufferCompacterTest extends TestCase {
   
   public void testOrder() throws Exception {
      Packet first = new BufferWrapper(ByteBuffer.wrap("First".getBytes()), 1);
      Packet second = new BufferWrapper(ByteBuffer.wrap("Second".getBytes()), 2);
      Packet third = new BufferWrapper(ByteBuffer.wrap("Third".getBytes()), 3);
      BufferCompacter builder = new BufferCompacter();
      
      assertEquals(builder.build(first).encode(), "First");
      assertEquals(builder.build(second).encode(), "First");
      assertEquals(builder.build(third).encode(), "First");
      
      BufferSegment firstSegment = builder.build();
      
      assertEquals(firstSegment.length(), first.length());
      assertEquals(firstSegment.encode(), "First");
      assertEquals(firstSegment.encode(), builder.build().encode());
      
      firstSegment.close(); // removew it from the builder
      
      BufferSegment secondSegment = builder.build();
      
      assertEquals(secondSegment.length(), second.length());
      assertEquals(secondSegment.encode(), "Second");
      assertEquals(secondSegment.encode(), builder.build().encode());
      
      Packet newFirst = new BufferWrapper(ByteBuffer.wrap("First".getBytes()), 1);

      assertEquals(newFirst.sequence(), 1);
      assertEquals(newFirst.length(), "First".length());
      assertEquals(builder.build(newFirst).encode(), "First");
      assertEquals(builder.build().encode(), "First");
      
      BufferSegment newFirstSegment = builder.build();
      
      assertEquals(newFirstSegment.sequence(), 1);
      assertEquals(newFirstSegment.encode(), "First");
      
      newFirstSegment.close();
      secondSegment.close();
      
      assertEquals(builder.build().sequence(), 3);
      assertEquals(builder.build().encode(), "Third");
      assertEquals(builder.length(), builder.build().length()); // because only one packet left  
   }
   
   public void testSegmentClose() throws Exception {
      Packet first = new BufferWrapper(ByteBuffer.wrap("First".getBytes()), 1);
      Packet second = new BufferWrapper(ByteBuffer.wrap("Second".getBytes()), 2);
      BufferCompacter builder = new BufferCompacter();
      
      builder.build(first); 
      builder.build(second);
      builder.build(first);
      builder.build(second);
      builder.build(first);
        
      assertEquals(builder.build().sequence(), 1);
      assertEquals(builder.build().encode(), "First");
      
      builder.build().close();
      
      assertEquals(builder.build().sequence(), 2); // don't extract closed packet
      assertEquals(builder.build().encode(), "Second");  
      
      builder.build().close();
      
      assertEquals(builder.build(), null); // all segments drained
      assertEquals(builder.length(), 0);      
   }
   
   public void testCompact() throws Exception {
      Packet first = new BufferWrapper(ByteBuffer.wrap("First".getBytes()), 1);
      Packet second = new BufferWrapper(ByteBuffer.wrap("Second".getBytes()), 2);
      BufferCompacter builder = new BufferCompacter();
      
      builder.build(first);
      builder.build(second);
      
      assertTrue(first.isReference());
      assertTrue(second.isReference());
      assertEquals(first.length(), "First".length());
      assertEquals(second.length(), "Second".length());
      assertEquals(builder.length(), "First".length() + "Second".length());
      assertEquals(builder.build().encode(), "First");
      
      builder.compact();
      
      assertTrue(first.isReference());
      assertTrue(second.isReference());
      assertEquals(first.length(), 0);
      assertEquals(second.length(), 0);
      assertEquals(builder.length(), "First".length() + "Second".length());
      assertEquals(builder.build().encode(), "First");  
      assertFalse(builder.build().isReference());
      
      first = new BufferWrapper(ByteBuffer.wrap("First".getBytes()), 1);
      second = new BufferWrapper(ByteBuffer.wrap("Second".getBytes()), 2);
      builder = new BufferCompacter(0);
      
      builder.build(first);
      builder.build(second);
      
      assertTrue(first.isReference());
      assertTrue(second.isReference());
      assertEquals(first.length(), "First".length());
      assertEquals(second.length(), "Second".length());
      assertEquals(builder.length(), "First".length() + "Second".length());
      assertEquals(builder.build().encode(), "First");
      
      builder.compact();
      
      assertTrue(first.isReference());
      assertTrue(second.isReference());
      assertEquals(first.length(), "First".length());
      assertEquals(second.length(), "Second".length());
      assertEquals(builder.length(), "First".length() + "Second".length());
      assertEquals(builder.build().encode(), "First");
   }
   
   public void testNulls() throws Exception {
      BufferCompacter builder = new BufferCompacter();
      Packet packet = new BufferWrapper(ByteBuffer.allocate(0), 1);
      BufferSegment segment = builder.build(packet);
      
      assertNull(segment);
   }

}
