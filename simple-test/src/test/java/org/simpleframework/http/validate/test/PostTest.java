package org.simpleframework.http.validate.test;

import java.io.OutputStream;

import org.simpleframework.common.KeyMap;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.StatusLine;

public class PostTest extends RoundTripTest {
   
   private static final String RESPONSE =
   "<html>\n"+
   "<head>\n"+
   "<title>Post Test Page</title>\n"+
   "</head>\n"+
   "\n"+
   "<body bgcolor='#FFFFFF'>\n"+
   "<form action='/post.html?code=5678' method='post'>\n"+
   "<table>\n"+
   "<tr><td>Name</td><td><input type='text' name='name'></td></tr>\n"+
   "<tr><td>Address</td><td><input type='text' name='address'></td></tr>\n"+  
   "<tr><td>Age</td><td><input type='text' name='age'></td></tr>\n"+
   "<tr><td>Description</td><td><input type='text' name='description'></td></tr>\n"+
   "<tr><td><input type='submit'></td><td><input type='reset'></td></tr>\n"+
   "</table>\n"+
   "</form>\n"+
   "</body>\n"+
   "</html>\n";
   
   private static final String REQUEST = 
   "name=Homer+Simpson&address=Springfield&age=36&description=Fat";

   private static final byte[] RESPONSE_DATA = RESPONSE.getBytes();
   
   @Scenario(concurrency=10, requests=10, method=Method.POST, protocol=Protocol.HTTP)
   public Analyser testRequest() throws Exception {
      return new PostAnalyser(0);
   }
   
   @Scenario(concurrency=10, requests=10, debug=true, method=Method.POST, protocol=Protocol.HTTP)
   public Analyser testBufferRequest() throws Exception {
      return new PostAnalyser(256);
   }
   
   @Scenario(concurrency=10, requests=10, debug=true, method=Method.POST, protocol=Protocol.HTTP)
   public Analyser testLargeBufferRequest() throws Exception {
      return new PostAnalyser(10000);
   }
   
   @Scenario(concurrency=10, requests=10, method=Method.POST, protocol=Protocol.HTTPS)
   public Analyser testSecureRequest() throws Exception {
      return new PostAnalyser(0);
   }
   
   @Scenario(concurrency=10, requests=10, debug=true, method=Method.POST, protocol=Protocol.HTTPS)
   public Analyser testSecureBufferRequest() throws Exception {
      return new PostAnalyser(256);
   }
   
   private class PostAnalyser implements Analyser { 
      
      private int buffer;
      
      public PostAnalyser(int buffer) {
         this.buffer = buffer;
      }
      
      public void compose(StringBuilder address, KeyMap<String> header, Buffer body) throws Exception {
         address.append("/post.html?code=5678");   
         header.put("Host", "localhost:9999");
         header.put("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.9.0.1) Gecko/2008070208 Firefox/3.0.1");
         header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
         header.put("Accept-Language", "en-gb,en;q=0.5");
         header.put("Accept-Encoding", "gzip,deflate");
         header.put("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
         header.put("Keep-Alive", "300");
         header.put("Connection", "keep-alive");
         header.put("Referer", "http://localhost:9999/post.html?code=5678");
         header.put("Content-Type", "application/x-www-form-urlencoded");
         header.put("Cookie", "Account=Standard; Secure=false");
         body.append(REQUEST.getBytes());            
      }
      public void handle(Request req, Response resp) throws Exception {
         OutputStream out = resp.getOutputStream(buffer);

         assertEquals(req.getValue("User-Agent"), "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-GB; rv:1.9.0.1) Gecko/2008070208 Firefox/3.0.1");
         assertEquals(req.getValue("Accept"), "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
         assertEquals(req.getValue("Accept-Language"), "en-gb,en;q=0.5");
         assertEquals(req.getValue("Accept-Encoding"), "gzip,deflate");
         assertEquals(req.getValue("Accept-Charset"), "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
         assertEquals(req.getValue("Keep-Alive"), "300");
         assertEquals(req.getValue("Connection"), "keep-alive");
         assertEquals(req.getValue("Referer"), "http://localhost:9999/post.html?code=5678");
         assertEquals(req.getValue("Content-Type"), "application/x-www-form-urlencoded");
         assertEquals(req.getParameter("code"), "5678");
         assertEquals(req.getParameter("name"), "Homer Simpson");
         assertEquals(req.getParameter("address"), "Springfield");
         assertEquals(req.getParameter("age"), "36");
         assertEquals(req.getParameter("description"), "Fat");
         assertEquals(req.getParameter("code"), "5678");
         assertEquals(req.getParameter("name"), "Homer Simpson");
         assertEquals(req.getParameter("address"), "Springfield");
         assertEquals(req.getParameter("age"), "36");
         assertEquals(req.getParameter("description"), "Fat");
         assertEquals(req.getQuery().get("code"), "5678");
         assertNull(req.getAddress().getQuery().get("name"));
         assertNull(req.getAddress().getQuery().get("address"));
         assertNull(req.getAddress().getQuery().get("age"));
         assertNull(req.getAddress().getQuery().get("description"));
         assertEquals(req.getLocales().get(0).getLanguage(), "en");
         assertEquals(req.getLocales().get(0).getCountry(), "GB");
         assertEquals(req.getLocales().get(1).getLanguage(), "en");
         assertEquals(req.getLocales().get(1).getCountry(), "");
         assertEquals(req.getValues("Accept").get(0), "text/html");
         assertEquals(req.getValues("Accept").get(1), "application/xhtml+xml");
         assertEquals(req.getValues("Accept").get(2), "application/xml");
         assertEquals(req.getValues("Accept").get(3), "*/*");
         assertEquals(req.getValues("Accept-Charset").get(0), "ISO-8859-1");
         assertEquals(req.getValues("Accept-Charset").get(1), "utf-8"); 
         assertEquals(req.getValues("Accept-Charset").get(2), "*");
         assertEquals(req.getValues("Accept-Encoding").get(0), "gzip");
         assertEquals(req.getValues("Accept-Encoding").get(1), "deflate");
         assertEquals(req.getValues("Accept-Language").get(0), "en-gb");
         assertEquals(req.getValues("Accept-Language").get(1), "en");
         assertEquals(req.getContentType().getPrimary(), "application");
         assertEquals(req.getContentType().getSecondary(), "x-www-form-urlencoded");
         assertNull(req.getContentType().getCharset());
         assertEquals(req.getCookie("Account").getName(), "Account");
         assertEquals(req.getCookie("Account").getValue(), "Standard");            
         assertEquals(req.getCookie("Account").getPath(), "/");
         assertEquals(req.getCookie("Secure").getName(), "Secure");
         assertEquals(req.getCookie("Secure").getValue(), "false");
         assertEquals(req.getCookie("Secure").getPath(), "/");
         assertEquals(req.getPath().getPath(), "/post.html");
         assertEquals(req.getPath().getName(), "post.html");
         assertEquals(req.getPath().getExtension(), "html");
         assertTrue(req.isKeepAlive());

         resp.setCode(200);
         resp.setDescription("OK");
         resp.setValue("Content-Type", "text/html; charset=UTF-8");
         resp.setValue("Server", "Apache/1.3.27 (Unix) mod_perl/1.27");
         resp.setInteger("Content-Length", RESPONSE_DATA.length);
         resp.setValue("Connection", "keep-alive");
         resp.setValue("Date", "Thu, 24 Jul 2008 11:20:54 GMT");
         resp.setValue("Last-Modified", "Thu, 24 Jul 2008 11:20:54 GMT");

         out.write(RESPONSE_DATA);
         out.close();
      }
      public void analyse(StatusLine line, KeyMap<String> header, Buffer body) throws Exception {
         assertEquals(line.getMajor(), 1);
         assertEquals(line.getMinor(), 1);
         assertEquals(line.getCode(), 200);
         assertEquals(line.getDescription(), "OK");
         assertEquals(body.encode(), RESPONSE);
         assertEquals(header.get("Server"),"Apache/1.3.27 (Unix) mod_perl/1.27");                  
         assertEquals(header.get("Content-Length"), String.valueOf(RESPONSE_DATA.length));
         assertEquals(header.get("Content-Type"), "text/html; charset=UTF-8");
         assertEquals(header.get("Last-Modified"), "Thu, 24 Jul 2008 11:20:54 GMT");
         assertEquals(header.get("Date"), "Thu, 24 Jul 2008 11:20:54 GMT");
         assertEquals(header.get("Connection"), "keep-alive");
      }
   }
}
