package org.simpleframework.http.message;

import junit.framework.TestCase;

import org.simpleframework.http.Part;
import org.simpleframework.http.core.StreamCursor;
import org.simpleframework.http.message.PartConsumer;
import org.simpleframework.http.message.PartData;
import org.simpleframework.transport.Cursor;
import org.simpleframework.util.buffer.ArrayAllocator;

public class PartConsumerTest extends TestCase {
   
   private static final String SOURCE =
   "Content-Disposition: form-data; name='pics'; filename='file1.txt'\r\n"+
   "Content-Type: text/plain\r\n\r\n"+
   "... contents of file1.txt ...\r\n"+
   "--AaB03x\r\n";
   
   public void testHeader() throws Exception {
      PartData list = new PartData();
      PartConsumer consumer = new PartConsumer(new ArrayAllocator(), list, "AaB03x".getBytes("UTF-8"), 8192);
      Cursor cursor = new StreamCursor(SOURCE);
      
      while(!consumer.isFinished()) {
         consumer.consume(cursor);
      }   
      assertEquals(list.getParts().size(), 1);
      assertEquals(list.getParts().get(0).getContentType().getPrimary(), "text");
      assertEquals(list.getParts().get(0).getContentType().getSecondary(), "plain");
      assertEquals(((Part)list.getParts().get(0)).getHeader("Content-Disposition"), "form-data; name='pics'; filename='file1.txt'");         
   }
}
