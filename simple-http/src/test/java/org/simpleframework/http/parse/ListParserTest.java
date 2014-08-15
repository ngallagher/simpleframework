package org.simpleframework.http.parse;

import junit.framework.TestCase;

public class ListParserTest extends TestCase {

   private ValueParser list;
        
   protected void setUp() {
      list = new ValueParser();           
   } 

   public void testEmpty() {
      assertEquals(0, list.list().size());
   }

   public void testQvalue() {
      list.parse("ISO-8859-1,utf-8;q=0.7,*;q=0.7");
      assertEquals(list.list().get(0), "ISO-8859-1");
      assertEquals(list.list().get(1), "utf-8");
      assertEquals(list.list().get(2), "*");
   }
   
   public void testPlain() {
      list.parse("en-gb");
      assertEquals("en-gb", list.list().get(0));

      list.parse("en");
      assertEquals("en", list.list().get(0));
   }

   public void testList() {
      list.parse("en-gb, en-us");
      assertEquals(2, list.list().size());    
      assertEquals("en-gb", list.list().get(0));
      assertEquals("en-us", list.list().get(1));
   }

   public void testOrder() {
      list.parse("en-gb, en-us");
      assertEquals(2, list.list().size());  
      assertEquals("en-gb", list.list().get(0));
      assertEquals("en-us", list.list().get(1));
           
      list.parse("da, en-gb;q=0.8, en;q=0.7");
      assertEquals("da", list.list().get(0));
      assertEquals("en-gb", list.list().get(1));
      assertEquals("en", list.list().get(2));

      list.parse("fr;q=0.1, en-us;q=0.4, en-gb; q=0.8, en;q=0.7");
      assertEquals("en-gb", list.list().get(0));
      assertEquals("en", list.list().get(1));
      assertEquals("en-us", list.list().get(2));
      assertEquals("fr", list.list().get(3));

      list.parse("en;q=0.2, en-us;q=1.0, en-gb");
      assertEquals("en-gb", list.list().get(0));
      assertEquals("en-us", list.list().get(1));
      assertEquals("en", list.list().get(2));
   }

   public void testRange() {
      list.parse("image/gif, image/jpeg, text/html");
      assertEquals(3, list.list().size());
      assertEquals("image/gif", list.list().get(0));
      assertEquals("text/html", list.list().get(2));

      list.parse("image/gif;q=1.0, image/jpeg;q=0.8, image/png;  q=1.0,*;q=0.1");
      assertEquals("image/gif", list.list().get(0));
      assertEquals("image/png", list.list().get(1));
      assertEquals("image/jpeg", list.list().get(2));
     
      list.parse("gzip;q=1.0, identity; q=0.5, *;q=0");
      assertEquals("gzip", list.list().get(0));
      assertEquals("identity", list.list().get(1));
   }

   public void testFlexibility() {
      list.parse("last; quantity=1;q=0.001, first; text=\"a, b, c, d\";q=0.4");
      assertEquals(2, list.list().size());
      assertEquals("first; text=\"a, b, c, d\"", list.list().get(0));
      assertEquals("last; quantity=1", list.list().get(1));

      list.parse("image/gif, , image/jpeg, image/png;q=0.8, *");
      assertEquals(4, list.list().size());
      assertEquals("image/gif", list.list().get(0));
      assertEquals("image/jpeg", list.list().get(1));
      assertEquals("*", list.list().get(2));
      assertEquals("image/png", list.list().get(3));
      
      list.parse("first=\"\\\"a, b, c, d\\\", a, b, c, d\", third=\"a\";q=0.9,,second=2");
      assertEquals(3, list.list().size());
      assertEquals("first=\"\\\"a, b, c, d\\\", a, b, c, d\"", list.list().get(0));
      assertEquals("second=2", list.list().get(1));     
      assertEquals("third=\"a\"", list.list().get(2));
   }
}        
