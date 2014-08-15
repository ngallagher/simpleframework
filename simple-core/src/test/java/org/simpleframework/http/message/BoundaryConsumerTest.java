package org.simpleframework.http.message;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.simpleframework.http.core.DribbleCursor;
import org.simpleframework.http.core.StreamCursor;
import org.simpleframework.http.message.BoundaryConsumer;
import org.simpleframework.util.buffer.ArrayAllocator;

public class BoundaryConsumerTest extends TestCase {
   
   private static final byte[] TERMINAL = { '-', '-', 'A', 'a', 'B', '0', '3', 'x', '-', '-', '\r', '\n', 'X', 'Y' };
   
   private static final byte[] NORMAL = { '-', '-', 'A', 'a', 'B', '0', '3', 'x', '\r', '\n', 'X', 'Y' };
   
   private static final byte[] BOUNDARY = { 'A', 'a', 'B', '0', '3', 'x' };
   
   private BoundaryConsumer boundary;
   
   public void setUp() {
      boundary = new BoundaryConsumer(new ArrayAllocator(), BOUNDARY);
   }
   
   public void testBoundary() throws Exception {
      StreamCursor cursor = new StreamCursor(new ByteArrayInputStream(NORMAL));
      
      while(!boundary.isFinished()) {
         boundary.consume(cursor);
      }
      assertEquals(cursor.read(), 'X');
      assertEquals(cursor.read(), 'Y');
      assertTrue(boundary.isFinished());
      assertFalse(boundary.isEnd());
      assertFalse(cursor.isReady());
   }
   
   public void testTerminal() throws Exception {
      StreamCursor cursor = new StreamCursor(new ByteArrayInputStream(TERMINAL));
      
      while(!boundary.isFinished()) {
         boundary.consume(cursor);
      }
      assertEquals(cursor.read(), 'X');
      assertEquals(cursor.read(), 'Y');
      assertTrue(boundary.isFinished());
      assertTrue(boundary.isEnd());
      assertFalse(cursor.isReady());
   }
   
   public void testDribble() throws Exception {
      DribbleCursor cursor = new DribbleCursor(new StreamCursor(new ByteArrayInputStream(TERMINAL)), 3);
      
      while(!boundary.isFinished()) {
         boundary.consume(cursor);
      }   
      assertEquals(cursor.read(), 'X');
      assertEquals(cursor.read(), 'Y');
      assertTrue(boundary.isFinished());
      assertTrue(boundary.isEnd());
      assertFalse(cursor.isReady());
      
      boundary.clear();
      
      cursor = new DribbleCursor(new StreamCursor(new ByteArrayInputStream(TERMINAL)), 1);
      
      while(!boundary.isFinished()) {
         boundary.consume(cursor);
      }   
      assertEquals(cursor.read(), 'X');
      assertEquals(cursor.read(), 'Y');
      assertTrue(boundary.isFinished());
      assertTrue(boundary.isEnd());
      assertFalse(cursor.isReady());
   }
}
