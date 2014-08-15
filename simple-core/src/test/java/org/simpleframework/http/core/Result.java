package org.simpleframework.http.core;

import java.util.List;
import java.util.Map;

import org.simpleframework.http.Cookie;

class Result {

   private List<Cookie> cookies;
   private String response;
   private byte[] body;
   private Map map;

   public Result(String response, byte[] body, Map map, List<Cookie> cookies) {
      this.cookies = cookies;
      this.response = response;
      this.body = body;
      this.map = map;
   }

   public List<Cookie> getCookies() {
      return cookies;
   }

   public byte[] getBody() {
      return body;
   }

   public String getResponse() throws Exception {
      return response;
   }

   public Map getMap() {
      return map;
   }
}