package org.simpleframework.http.validate.test;

import java.io.OutputStream;

import org.simpleframework.common.KeyMap;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.StatusLine;

public class SoakTest extends RoundTripTest {
   
   private static final String PAGE =
   "<html>\n"+
   "<head>\n"+
   "<title>Soak Test Page</title>\n"+
   "<style type='text/css'>\n"+
   "<!--\n"+
   ".text1 {  font-family: Arial, Helvetica, sans-serif; font-size: 14pt; font-style: italic; color: #6666FF }\n"+
   ".text2 {  font-family: 'Courier New', Courier, mono; font-size: 10pt; font-style: normal; font-weight: bold }\n"+
   ".text3 {  font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 12pt; color: #66CCCC; font-weight: bolder }\n"+
   "-->\n"+
   "</style>\n"+
   "</head>\n"+
   "\n"+
   "<body bgcolor='#FFFFFF'>\n"+
   "<p class='text1'>This is the soak test page.</p>\n"+
   "<p class='text2'>It measures the servers stability.</p>\n"+
   "<p class='text3'>It also provides throughput data.</p>\n"+
   "</body>\n"+
   "</html>\n";

   private static final byte[] DATA = PAGE.getBytes();

   @Scenario(concurrency=30, requests=100, threadDump=true)
   public Analyser testRequest() throws Exception {
      return new SoakScenario();
   }
   
   @Scenario(concurrency=30, requests=100, threadDump=true)
   public Analyser testFilterRequest() throws Exception {
      return new FilterAnalyser(new SoakScenario());
   }
   
   @Scenario(concurrency=30, requests=100, threadDump=true, protocol=Protocol.HTTPS)
   public Analyser testSecureRequest() throws Exception {
      return new SoakScenario();
   }
   
   public class SoakScenario implements Analyser {
         
      public void compose(StringBuilder address, KeyMap<String> header, Buffer body) throws Exception {
         address.append("/index.html");   
         header.put("Accept", "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, */*");
         header.put("Referer", "http://www.google.co.uk/search?hl=en&q=example+http+request&meta=");
         header.put("Accept-Language", "en-gb");
         header.put("UA-CPU", "x86");
         header.put("Accept-Encoding", "gzip, deflate");
         header.put("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Sky Broadband; .NET CLR 1.1.4322; Sky Broadband; Sky Broadband)");
         header.put("Connection", "Keep-Alive");            
      }
      public void handle(Request req, Response resp) throws Exception {
         OutputStream out = resp.getOutputStream();

         assertTrue(req.isKeepAlive());
         assertEquals(req.getValue("Accept"),  "image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, */*");
         assertEquals(req.getValue("Referer"), "http://www.google.co.uk/search?hl=en&q=example+http+request&meta=");
         assertEquals(req.getValue("Accept-Language"), "en-gb");
         assertEquals(req.getValue("UA-CPU"), "x86");
         assertEquals(req.getValue("Accept-Encoding"), "gzip, deflate");
         assertEquals(req.getValue("User-Agent"), "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; Sky Broadband; .NET CLR 1.1.4322; Sky Broadband; Sky Broadband)");  
         assertEquals(req.getValue("Connection"), "Keep-Alive");

         resp.setCode(200);
         resp.setDescription("OK");
         resp.setValue("Content-Type", "text/html; charset=UTF-8");
         resp.setValue("Server", "Apache/1.3.27 (Unix) mod_perl/1.27");
         resp.setInteger("Content-Length", DATA.length);
         resp.setValue("Connection", "keep-alive");
         resp.setValue("Date", "Thu, 24 Jul 2008 11:20:54 GMT");
         resp.setValue("Last-Modified", "Thu, 24 Jul 2008 11:20:54 GMT");

         out.write(DATA);
         out.close();
      }
      public void analyse(StatusLine line, KeyMap<String> resp, Buffer body) throws Exception {
         assertEquals(line.getMajor(), 1);
         assertEquals(line.getMinor(), 1);
         assertEquals(line.getCode(), 200);
         assertEquals(line.getDescription(), "OK");
         assertEquals(body.encode(), PAGE);
         assertEquals(resp.get("Server"),"Apache/1.3.27 (Unix) mod_perl/1.27");                  
         assertEquals(resp.get("Content-Length"), String.valueOf(DATA.length));
         assertEquals(resp.get("Content-Type"), "text/html; charset=UTF-8");
         assertEquals(resp.get("Last-Modified"), "Thu, 24 Jul 2008 11:20:54 GMT");
         assertEquals(resp.get("Date"), "Thu, 24 Jul 2008 11:20:54 GMT");
         assertEquals(resp.get("Connection"), "keep-alive");
      }
   }
   
}
