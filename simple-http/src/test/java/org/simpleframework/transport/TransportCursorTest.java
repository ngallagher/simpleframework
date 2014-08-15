package org.simpleframework.transport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

public class TransportCursorTest extends TestCase {
   
   private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
   private static final String SOURCE = ALPHABET + "\r\n";
   
   public void testCursor() throws IOException {
      byte[] data = SOURCE.getBytes("ISO-8859-1");
      InputStream source = new ByteArrayInputStream(data);
      Transport transport = new StreamTransport(source, System.out);
      Cursor cursor = new TransportCursor(transport);
      byte[] buffer = new byte[1024];
      
      assertEquals(cursor.ready(), data.length);
      assertEquals(26, cursor.read(buffer, 0, 26));
      assertEquals(26, cursor.reset(26));
      assertEquals(new String(buffer, 0, 26), ALPHABET);
      
      assertEquals(cursor.ready(), data.length);
      assertEquals(26, cursor.read(buffer, 0, 26));
      assertEquals(26, cursor.reset(26));
      assertEquals(new String(buffer, 0, 26), ALPHABET);
      
      assertEquals(cursor.ready(), data.length);
      assertEquals(4, cursor.read(buffer, 0, 4));
      assertEquals(4, cursor.reset(26));
      assertEquals(new String(buffer, 0, 4), "abcd");
      
      assertEquals(cursor.ready(), data.length);
      assertEquals(4, cursor.read(buffer, 0, 4));
      assertEquals(4, cursor.reset(26));
      assertEquals(new String(buffer, 0, 4), "abcd");
      
      assertEquals(cursor.ready(), data.length);
      assertEquals(4, cursor.read(buffer, 0, 4));
      assertEquals(new String(buffer, 0, 4), "abcd");
      
      assertEquals(cursor.ready(), data.length - 4);
      assertEquals(4, cursor.read(buffer, 0, 4));
      assertEquals(new String(buffer, 0, 4), "efgh");
      
      assertEquals(cursor.ready(), data.length - 8);
      assertEquals(4, cursor.read(buffer, 0, 4));
      assertEquals(new String(buffer, 0, 4), "ijkl");
      
      assertEquals(cursor.ready(), data.length - 12);
      assertEquals(12, cursor.reset(12));
      assertEquals(10, cursor.read(buffer, 0, 10));
      assertEquals(new String(buffer, 0, 10), "abcdefghij");
      
      cursor.push("1234".getBytes("ISO-8859-1"));
      cursor.push("5678".getBytes("ISO-8859-1"));
      cursor.push("90".getBytes("ISO-8859-1"));
      
      assertEquals(cursor.ready(), 10);
      assertEquals(2, cursor.read(buffer, 0, 2));
      assertEquals(new String(buffer, 0, 2), "90");
      
      assertEquals(cursor.ready(), 8);
      assertEquals(4, cursor.read(buffer, 0, 4));
      assertEquals(new String(buffer, 0, 4), "5678");
      
      assertEquals(cursor.ready(), 4);
      assertEquals(4, cursor.read(buffer, 0, 4));
      assertEquals(new String(buffer, 0, 4), "1234");
      
      assertEquals(4, cursor.reset(4));
      assertEquals(cursor.ready(), 4);
      assertEquals(4, cursor.read(buffer, 0, 4));
      assertEquals(new String(buffer, 0, 4), "1234");
      
      assertEquals(8, cursor.read(buffer, 0, 8));
      assertEquals(new String(buffer, 0, 8), "klmnopqr");
   }

}
