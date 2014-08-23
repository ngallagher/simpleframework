package org.simpleframework.http.validate;

import java.io.InputStream;
import java.io.PushbackInputStream;

import junit.framework.TestCase;

import org.simpleframework.common.buffer.ArrayBuffer;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.xml.core.Persister;

public class ReportTest extends TestCase {
   
   private static final String HEADER = 
   "HTTP/1.1 200 OK\r\n"+
   "Content-Type: text/plain\r\n"+
   "Server: Apache/2.1\r\n"+
   "Content-Length: 10\r\n"+
   "Connection: keep-alive\r\n"+
   "\r\n"+
   "0123456789";
   
   public void testReport() throws Exception {
      Persister persister = new Persister();
      Extractor extractor = new Extractor(true);
      Buffer buffer = new ArrayBuffer(1024);      
      buffer.append(HEADER.getBytes("ISO-8859-1"));
      InputStream stream = buffer.open();
      PushbackInputStream pushback = new PushbackInputStream(stream, 2048);
      Result response = extractor.extractResponse(pushback);
      Report report = new Report(response, "HTTP/1.1 404 Not Found");
      
      assertEquals(report.getStatusLine(), "HTTP/1.1 200 OK");
      assertEquals(report.isError(), false);
      
      persister.write(report, System.out);      
   }

}
