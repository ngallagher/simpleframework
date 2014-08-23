package org.simpleframework.http.validate.test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;

import org.simpleframework.common.KeyMap;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.StatusLine;

public class ChunkedBodyTest extends RoundTripTest {
   
   @Scenario(requests=1, concurrency=1, debug=true, protocol=Protocol.HTTP, method=Method.POST)
   public Analyser testRequest() throws Exception {
      return new ChunkedAnalyser(1024);         
   }
   
   @Scenario(requests=10, concurrency=10, protocol=Protocol.HTTP, method=Method.POST, threadDump=true)
   public Analyser testLargeBufferRequest() throws Exception {
      return new ChunkedAnalyser(8192);         
   }
   
   
   @Scenario(requests=10, concurrency=10, protocol=Protocol.HTTPS, method=Method.POST, threadDump=true)
   public Analyser testSecureRequest() throws Exception {
      return new ChunkedAnalyser(2048);         
   }
   
   private class ChunkedAnalyser implements Analyser {
      
      private int buffer;
      
      public ChunkedAnalyser(int buffer)  {
         this.buffer = buffer;
      }
      
      public void compose(StringBuilder address, KeyMap<String> header, Buffer body) throws Exception {
         address.append("/index.html");  
         header.put("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; en-US; rv:1.9.0.5) Gecko/2008120121 Firefox/3.0.5");
         header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
         header.put("Accept-Language", "en-us,en;q=0.5");
         header.put("Accept-Encoding", "gzip,deflate");
         header.put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
         header.put("Keep-Alive", "300");
         header.put("Content-Length", "15");
         header.put("Content-Type", "application/x-www-form-urlencoded");
         header.put("Connection", "keep-alive");
         header.put("If-Modified-Since", "Sat, 31 Jan 2009 13:39:48 GMT");
         body.append("a=A&b=B&c=C&d=D".getBytes());
      }
      
      public void handle(Request req, Response resp) throws Exception {
         PrintStream out = resp.getPrintStream(buffer);
         long time = System.currentTimeMillis();
         
         assertEquals(req.getMethod(), "POST");
         assertEquals(req.getTarget(), "/index.html");
         assertEquals(req.getValue("Connection"), "keep-alive");
         assertEquals(req.getValue("Accept-Language"), "en-us,en;q=0.5");
         assertEquals(req.getValue("Accept-Encoding"), "gzip,deflate");
         assertEquals(req.getValue("Accept"), "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
         assertEquals(req.getParameter("a"), "A");
         assertEquals(req.getParameter("b"), "B");
         assertEquals(req.getParameter("c"), "C");
         assertEquals(req.getParameter("d"), "D");
         assertEquals(req.getParameter("a"), "A");
         assertEquals(req.getParameter("b"), "B");
         assertEquals(req.getParameter("c"), "C");
         assertEquals(req.getParameter("d"), "D");
         assertEquals(req.getContent(), "a=A&b=B&c=C&d=D");
         
         resp.setValue("Server", "Apache/1.2");
         resp.setDate("Date", time);
         resp.setDate("Last-Modified", time);
         
         for(int i = 0; i < 10000; i++) {
            out.printf("%s: xxxxx xxxxx xxxxx xxxxx%n", i);
         }
         resp.close(); 
      }
      
      public void analyse(StatusLine line, KeyMap<String> resp, Buffer body) throws Exception {
         InputStream stream = body.open();
         Reader reader = new InputStreamReader(stream);
         BufferedReader lineReader = new BufferedReader(reader);

         for(int i = 0; i < 10000; i++) {
            assertEquals(String.format("%s: xxxxx xxxxx xxxxx xxxxx", i), lineReader.readLine());
         }
         assertEquals(line.getMajor(), 1);
         assertEquals(line.getMinor(), 1);
         assertEquals(line.getCode(), 200);
         assertEquals(resp.get("Server"), "Apache/1.2");
         assertEquals(resp.get("Connection"), "keep-alive");  
         assertNotNull(resp.get("Date"));
         assertNotNull(resp.get("Last-Modified"));
      }      
   }
}
