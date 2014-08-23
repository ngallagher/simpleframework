package org.simpleframework.http.validate;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Map;

import junit.framework.TestCase;

import org.simpleframework.common.buffer.ArrayBuffer;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.http.StatusLine;

public class ExtractorTest extends TestCase {
   
   private static final String HEADER = 
   "HTTP/1.1 200 OK\r\n"+
   "Content-Type: text/plain\r\n"+
   "Server: Apache/2.1\r\n"+
   "Content-Length: 10\r\n"+
   "Connection: keep-alive\r\n"+
   "\r\n"+
   "0123456789";
   
   public void testExtractor() throws Exception {
      Extractor extractor = new Extractor(true);
      Buffer buffer = new ArrayBuffer(1024);      
      buffer.append(HEADER.getBytes("ISO-8859-1"));
      InputStream stream = buffer.open();
      PushbackInputStream pushback = new PushbackInputStream(stream, 2048);
      StatusLine status = extractor.extractStatus(pushback);
      Map<String, String> header = extractor.extractHeader(pushback);
      Buffer body = extractor.extractBody(header, pushback);
      
      assertEquals(status.getMajor(), 1);
      assertEquals(status.getMinor(), 1);
      assertEquals(status.getCode(), 200);
      assertEquals(status.getDescription(), "OK");
      assertEquals(header.get("Content-Type"), "text/plain");
      assertEquals(header.get("Server"), "Apache/2.1");
      assertEquals(header.get("Content-Length"), "10");
      assertEquals(header.get("Connection"), "keep-alive");      
      assertEquals(body.encode(), "0123456789");
   }
   
   public void testResponse() throws Exception {
      Extractor extractor = new Extractor(true);
      Buffer buffer = new ArrayBuffer(1024);      
      buffer.append(HEADER.getBytes("ISO-8859-1"));
      InputStream stream = buffer.open();
      PushbackInputStream pushback = new PushbackInputStream(stream, 2048);
      Result response = extractor.extractResponse(pushback);
      
      assertEquals(response.getStatusLine().getMajor(), 1);
      assertEquals(response.getStatusLine().getMinor(), 1);
      assertEquals(response.getStatusLine().getCode(), 200);
      assertEquals(response.getStatusLine().getDescription(), "OK");
      assertEquals(response.getValue("Content-Type"), "text/plain");
      assertEquals(response.getValue("Server"), "Apache/2.1");
      assertEquals(response.getInteger("Content-Length"), 10);
      assertEquals(response.getValue("Connection"), "keep-alive");      
      assertEquals(response.getBody().encode(), "0123456789");
   }

}
