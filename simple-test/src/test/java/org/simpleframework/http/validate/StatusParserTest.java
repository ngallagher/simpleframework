package org.simpleframework.http.validate;

import junit.framework.TestCase;

import org.simpleframework.common.buffer.ArrayBuffer;
import org.simpleframework.common.buffer.Buffer;

public class StatusParserTest extends TestCase {
   
   public void testStatusParser() throws Exception {
      Buffer buffer = new ArrayBuffer(200);
      byte[] status = "HTTP/1.1 200 OK\r\n".getBytes();
      buffer.append(status);
      StatusParser parser = new StatusParser(buffer);
      
      assertEquals(1, parser.getMajor());
      assertEquals(1, parser.getMinor());
      assertEquals(200, parser.getCode());
      assertEquals("OK", parser.getDescription());
      
      buffer = new ArrayBuffer(200);
      status = "HTTP/1.0 200 OK".getBytes();
      buffer.append(status);
      parser = new StatusParser(buffer);
      
      assertEquals(1, parser.getMajor());
      assertEquals(0, parser.getMinor());
      assertEquals(200, parser.getCode());
      assertEquals("OK", parser.getDescription());
      
      buffer = new ArrayBuffer(200);
      status = "HTTP/1.0 200 The request succeeded".getBytes();
      buffer.append(status);
      parser = new StatusParser(buffer);
      
      assertEquals(1, parser.getMajor());
      assertEquals(0, parser.getMinor());
      assertEquals(200, parser.getCode());
      assertEquals("The request succeeded", parser.getDescription());
   }

}
