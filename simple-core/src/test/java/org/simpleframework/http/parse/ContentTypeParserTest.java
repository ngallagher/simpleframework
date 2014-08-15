package org.simpleframework.http.parse;

import junit.framework.TestCase;

import org.simpleframework.http.parse.ContentTypeParser;

public class ContentTypeParserTest extends TestCase {

   private ContentTypeParser type;
        
   protected void setUp() {
      type = new ContentTypeParser();           
   } 

   public void testEmpty() {
      assertEquals(null, type.getPrimary());
      assertEquals(null, type.getSecondary());
      assertEquals(null, type.getCharset());
   }

   public void testPlain() {
      type.parse("text/html");
      assertEquals("text", type.getPrimary());
      assertEquals("html", type.getSecondary());

      type.setSecondary("plain");
      assertEquals("text", type.getPrimary());
      assertEquals("plain", type.getSecondary());
   }

   public void testCharset() {
      type.parse("text/html; charset=UTF-8");
      assertEquals("text", type.getPrimary());
      assertEquals("UTF-8", type.getCharset());
      assertEquals("text/html", type.getType());
      
      type.setCharset("ISO-8859-1"); 
      assertEquals("ISO-8859-1", type.getCharset());
   }

   public void testIgnore() {
      type.parse("text/html; name=value; charset=UTF-8; property=value");           
      assertEquals("UTF-8", type.getCharset());
      assertEquals("html", type.getSecondary());
   }

   public void testFlexibility() {
      type.parse("    text/html  ;charset=   UTF-8 ;  name =     value" ); 
      assertEquals("text", type.getPrimary());
      assertEquals("html", type.getSecondary());
      assertEquals("text/html", type.getType());
      assertEquals("UTF-8", type.getCharset());      
   }

   public void testString() {
      type.parse("  image/gif; name=value");
      assertEquals("image/gif; name=value", type.toString());

      type.parse(" text/html; charset =ISO-8859-1");
      assertEquals("text/html; charset=ISO-8859-1", type.toString());      
      assertEquals("text/html", type.getType());
      
      type.setSecondary("css");
      assertEquals("text", type.getPrimary());
      assertEquals("css", type.getSecondary());
      assertEquals("text/css", type.getType());
      assertEquals("text/css; charset=ISO-8859-1", type.toString());
      
      type.setPrimary("image");
      assertEquals("image", type.getPrimary());
      assertEquals("css", type.getSecondary());
      assertEquals("image/css", type.getType());
   }
}        
