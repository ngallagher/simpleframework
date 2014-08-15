package org.simpleframework.http.parse;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.simpleframework.http.Cookie;

public class CookieParserTest extends TestCase {
   
   public void testParse() throws Exception {
      CookieParser parser = new CookieParser("blackbird={\"pos\": 1, \"size\": 0, \"load\": null}; JSESSIONID=31865d30-e252-4729-ac6f-9abdd1fb9071");
      List<Cookie> cookies = new ArrayList<Cookie>();
      
      for(Cookie cookie : parser) {
         System.out.println(cookie.toClientString());
         cookies.add(cookie);
      }
   }

}
