package org.simpleframework.http.validate.test;

import java.io.PrintStream;

import org.simpleframework.common.KeyMap;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.StatusLine;

public class HeadTest extends RoundTripTest {

   @Scenario(requests=5, concurrency=10, debug=true, method=Method.HEAD)
   public Analyser testQuery() throws Exception {
      return new HeadAnalyser(false);
   }
   
   @Scenario(requests=5, concurrency=10, debug=true, method=Method.HEAD, protocol=Protocol.HTTPS)
   public Analyser testSecureQuery() throws Exception {
      return new HeadAnalyser(true);
   }

   private class HeadAnalyser implements Analyser {
      
      private final boolean secure;
      
      public HeadAnalyser(boolean secure) {
         this.secure = secure;
      }
      
      public void compose(StringBuilder address, KeyMap<String> header, Buffer body) throws Exception {
         address.append("/index.html");  
         header.put("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; en-US; rv:1.9.0.5) Gecko/2008120121 Firefox/3.0.5");
         header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
         header.put("Accept-Language", "en-us,en;q=0.5");
         header.put("Accept-Encoding", "gzip,deflate");
         header.put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
         header.put("Keep-Alive", "300");
         header.put("Content-Type", "application/x-www-form-urlencoded");
         header.put("Connection", "keep-alive");
         header.put("If-Modified-Since", "Sat, 31 Jan 2009 13:39:48 GMT");
      }
      
      public void handle(Request req, Response resp) throws Exception {
         PrintStream out = resp.getPrintStream();
         long time = System.currentTimeMillis();
         
         assertEquals(req.getMethod(), "HEAD");
         assertEquals(req.getTarget(), "/index.html");
         assertEquals(req.getValue("Connection"), "keep-alive");
         assertEquals(req.getValue("Accept-Language"), "en-us,en;q=0.5");
         assertEquals(req.getValue("Accept-Encoding"), "gzip,deflate");
         assertEquals(req.getValue("Accept"), "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
         assertEquals(req.isSecure(), secure);
         
         resp.setCookie("A", "b");
         resp.setCookie("C", "d");
         resp.setValue("Server", "Apache/1.2");   
         resp.setDate("Date", time);
         resp.setDate("Last-Modified", time);
         resp.setContentLength(10000);
         
         for(int i = 0; i < 10000; i++) {
            out.write(i);
         }
         out.close();
      }
      
      public void analyse(StatusLine line, KeyMap<String> resp, Buffer body) throws Exception {
         assertEquals(line.getMajor(), 1);
         assertEquals(line.getMinor(), 1);
         assertEquals(line.getCode(), 200);
         assertEquals(body.encode(), "");
         assertEquals(resp.get("Server"), "Apache/1.2");  
         assertNotNull(resp.get("Set-Cookie"));
         assertNotNull(resp.get("Date"));
         assertNotNull(resp.get("Last-Modified"));
      }
      
   }

}
