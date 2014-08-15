package org.simpleframework.http;

import junit.framework.TestCase;

public class CookieTest extends TestCase {
   
   public void testCookies() throws Exception {
      Cookie cookie = new Cookie("JSESSIONID", "XXX");
      
      cookie.setExpiry(10);
      cookie.setPath("/path");
      
      System.err.println(cookie);
      
      assertTrue(cookie.toString().contains("max-age=10"));
      assertTrue(cookie.toString().matches(".*expires=\\w\\w\\w, \\d\\d-\\w\\w\\w-\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d GMT;.*"));
   }

   public void testCookieWithoutExpiry() throws Exception {
      Cookie cookie = new Cookie("JSESSIONID", "XXX");
      
      cookie.setPath("/path");
      
      System.err.println(cookie);
      
      assertFalse(cookie.toString().contains("max-age=10"));
      assertFalse(cookie.toString().matches(".*expires=\\w\\w\\w, \\d\\d \\w\\w\\w \\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d GMT;.*"));
   }
   
   public void testSecureCookies() throws Exception {
      Cookie cookie = new Cookie("JSESSIONID", "XXX");
      
      cookie.setExpiry(10);
      cookie.setPath("/path");
      cookie.setSecure(true);
      
      System.err.println(cookie);
      
      assertTrue(cookie.toString().contains("max-age=10"));
      assertTrue(cookie.toString().matches(".*expires=\\w\\w\\w, \\d\\d-\\w\\w\\w-\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d GMT;.*"));
   
      cookie.setExpiry(10);
      cookie.setPath("/path");
      cookie.setSecure(false);
      cookie.setProtected(true);
      
      System.err.println(cookie);
      
      assertTrue(cookie.toString().contains("max-age=10"));
      assertTrue(cookie.toString().matches(".*expires=\\w\\w\\w, \\d\\d-\\w\\w\\w-\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d GMT;.*"));

      cookie.setExpiry(10);
      cookie.setPath("/path");
      cookie.setSecure(true);
      cookie.setProtected(true);
      
      System.err.println(cookie);
      
      assertTrue(cookie.toString().contains("max-age=10"));
      assertTrue(cookie.toString().matches(".*expires=\\w\\w\\w, \\d\\d-\\w\\w\\w-\\d\\d\\d\\d \\d\\d:\\d\\d:\\d\\d GMT;.*"));

   }
}
