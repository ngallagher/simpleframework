package org.simpleframework.http.validate.test;

import java.io.InputStream;

import org.simpleframework.common.KeyMap;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.StatusLine;

public class QueryTest extends RoundTripTest {
   
   @Scenario(requests=5, concurrency=5, debug=true)
   public Analyser testQuery() throws Exception {
      return new QueryScenario(false);
   }
   
   @Scenario(requests=5, concurrency=5, debug=true, protocol=Protocol.HTTPS)
   public Analyser testSecureQuery() throws Exception {
      return new QueryScenario(true);
   }

   private class QueryScenario implements Analyser {
      
      private final boolean secure;
      
      public QueryScenario(boolean secure) {
         this.secure = secure;
      }
      
      public void compose(StringBuilder address, KeyMap<String> header, Buffer body) throws Exception {
         address.append("/index.html?a=A&b=B&c=C&d=D");
         header.put("User-Agent", "IE/5.0");
      }
      
      public void handle(Request req, Response resp) throws Exception {
         InputStream body = req.getInputStream();
         Query query = req.getQuery();
         
         assertEquals(body.read(), -1);
         assertEquals(req.isSecure(), secure);
         assertEquals(req.getMethod(), "GET");
         assertEquals(query.size(), 4);
         assertEquals(query.get("a"), "A");
         assertEquals(query.get("b"), "B");
         assertEquals(query.get("c"), "C");
         assertEquals(query.get("d"), "D");
         assertEquals(req.getValue("User-Agent"), "IE/5.0");
         
         resp.setCookie("A", "b");
         resp.setCookie("C", "d");
         resp.setValue("Server", "Apache/1.2");              
      }
      
      public void analyse(StatusLine line, KeyMap<String> resp, Buffer body) throws Exception {
         assertEquals(line.getMajor(), 1);
         assertEquals(line.getMinor(), 1);
         assertEquals(line.getCode(), 200);
         assertEquals(body.encode(), "");
         assertEquals(resp.get("Server"), "Apache/1.2");
         assertEquals(resp.get("Connection"), "keep-alive");   
      }
      
   }

}
