package org.simpleframework.http.parse;

import org.simpleframework.http.parse.QueryParser;

import junit.framework.TestCase;

public class ParameterTest extends TestCase {

   private QueryParser data;
        
   protected void setUp() {
      data = new QueryParser();           
   } 

   public void testEmptyPath() {
      assertEquals(0, data.size());
   }

   public void testValue() {
      data.parse("a=");
      
      assertEquals(1, data.size());
      assertEquals("", data.get("a"));

      data.parse("a=&b=c");

      assertEquals(2, data.size());
      assertEquals("", data.get("a"));
      assertEquals("c", data.get("b"));

      data.parse("a=b&c=d&e=f&");

      assertEquals(3, data.size());
      assertEquals("b", data.get("a"));
      assertEquals("d", data.get("c"));
      assertEquals("f", data.get("e"));

      data.clear();
      data.put("a", "A");
      data.put("c", "C");
      data.put("x", "y");

      assertEquals(3, data.size());
      assertEquals("A", data.get("a"));
      assertEquals("C", data.get("c"));
      assertEquals("y", data.get("x"));
   }
   
   public void testValueList() {
      data.parse("a=1&a=2&a=3");
      
      assertEquals(data.size(), 1);
      assertEquals(data.getAll("a").size(), 3);
      assertEquals(data.getAll("a").get(0), "1");
      assertEquals(data.getAll("a").get(1), "2");
      assertEquals(data.getAll("a").get(2), "3");
      
      data.parse("a=b&c=d&c=d&a=1");
    
      assertEquals(data.size(), 2);
      assertEquals(data.getAll("a").size(), 2);
      assertEquals(data.getAll("a").get(0), "b");
      assertEquals(data.getAll("a").get(1), "1");
      assertEquals(data.getAll("c").size(), 2);
      assertEquals(data.getAll("c").get(0), "d");
      assertEquals(data.getAll("c").get(1), "d");
      
   }
}        
