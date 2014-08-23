package org.simpleframework.http.validate.test;

import java.io.PrintStream;

import org.simpleframework.common.KeyMap;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.StatusLine;

public class EchoTest extends RoundTripTest {
   
   @Scenario(requests=1, concurrency=1, protocol=Protocol.HTTP, method=Method.POST)
   public Analyser testRequestByte() throws Exception {
      return testRequest(1);
   }
   
   @Scenario(requests=20, concurrency=10, protocol=Protocol.HTTP, method=Method.POST)
   public Analyser testRequestTwoBytes() throws Exception {
      return testRequest(2);
   }
   
   @Scenario(requests=20, concurrency=10, protocol=Protocol.HTTP, method=Method.POST)
   public Analyser testRequestThreeByte() throws Exception {
      return testRequest(3);
   }
   
   @Scenario(requests=20, concurrency=10, protocol=Protocol.HTTP, method=Method.POST)
   public Analyser testSmallRequest() throws Exception {
      return testRequest(512);
   }
   
   @Scenario(requests=20, concurrency=10, protocol=Protocol.HTTP, method=Method.POST)
   public Analyser testMediumRequest() throws Exception {
      return testRequest(2048);
   }
   
   @Scenario(requests=20, concurrency=10, protocol=Protocol.HTTP, method=Method.POST)
   public Analyser testLargeRequest() throws Exception {
      return testRequest(10000);
   }
   
   public Analyser testRequest(int size) throws Exception {
      return new FilterAnalyser(new EchoScenario(size));         
   }
   
   private class EchoScenario implements Analyser {
      
      private String expect;
      
      public EchoScenario(int size) throws Exception {
         this.expect = getBody(size);
      }
      public void compose(StringBuilder address, KeyMap<String> header, Buffer body) throws Exception {
         address.append("/index.html");  
         header.put("User-Agent", "IE/5.0");
         header.put("Content-Length", String.valueOf(expect.length()));
         body.append(expect.getBytes());
      }
      public void handle(Request req, Response resp) throws Exception {
         PrintStream out = resp.getPrintStream();
         String content = req.getContent();
         
         resp.setValue("Server", "Apache/1.2");
         out.print(content); 
      }
      public void analyse(StatusLine line, KeyMap<String> resp, Buffer body) throws Exception {
         assertEquals(line.getMajor(), 1);
         assertEquals(line.getMinor(), 1);
         assertEquals(line.getCode(), 200);
         assertEquals(body.encode(), expect);
         assertEquals(resp.get("Server"), "Apache/1.2");
         assertEquals(resp.get("Connection"), "keep-alive");     
      }      
   }
   
   private static String getBody(int size) throws Exception {
      String values = "abcdefghijklmnopqrstuvwxyz";
      StringBuilder builder = new StringBuilder();
      
      for(int i = 0; i < size; i++) {
         char ch = values.charAt(i % values.length());
         
         if(i % 40 == 0 && i > 0) {
            builder.append('\n');
         }
         builder.append(ch);
      }
      builder.append('\n');
      return builder.toString();
   }
}
