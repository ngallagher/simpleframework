package org.simpleframework.http.parse;

import junit.framework.TestCase;

import org.simpleframework.http.Query;

public class AddressParserTest extends TestCase {

   private AddressParser link;
        
   protected void setUp() {
      link = new AddressParser();           
   } 

   public void testEmptyPath() {
      assertEquals("/", link.getPath().toString());
   }

   public void testEmptyQuery() {
      Query query = link.getQuery();
      assertEquals(0, query.size());
   }

   public void testPath() {
      link.parse("/this/./is//some/relative/./hidden/../URI.txt"); 
      assertEquals("/this/is/some/relative/URI.txt", link.getPath().toString());      

      link.parse("/this//is/a/simple/path.html?query");
      assertEquals("/this/is/a/simple/path.html", link.getPath().toString());
   }

   public void testQuery() {
      link.parse("/?name=value&attribute=string");
   
      Query query = link.getQuery();               
                    
      assertEquals(2, query.size());      
      assertEquals("value", query.get("name"));
      assertTrue(query.containsKey("attribute"));

      query.clear();
      query.put("name", "change");

      assertEquals("change", query.get("name"));
   }

   public void testPathParameters() {
      link.parse("/index.html;jsessionid=1234567890?jsessionid=query"); 
      assertEquals("1234567890", link.getParameters().get("jsessionid"));
      
      link.parse("/path/index.jsp");
      link.getParameters().put("jsessionid", "value");
      
      assertEquals("/path/index.jsp;jsessionid=value", link.toString());
      
      link.parse("/path");
      link.getParameters().put("a", "1");
      link.getParameters().put("b", "2");
      link.getParameters().put("c", "3");
      
      link.parse(link.toString());

      assertEquals("1", link.getParameters().get("a"));
      assertEquals("2", link.getParameters().get("b"));
      assertEquals("3", link.getParameters().get("c"));
      
      
   }

   public void testAbsolute() {
      link.parse("http://domain:9090/index.html?query=value");
      assertEquals("domain", link.getDomain());
   
      link.setDomain("some.domain");
      assertEquals("some.domain", link.getDomain());
      assertEquals("http://some.domain:9090/index.html?query=value", link.toString());
      assertEquals(9090, link.getPort());

      link.parse("domain.com:80/index.html?a=b&c=d");
      assertEquals("domain.com", link.getDomain());
      assertEquals(80, link.getPort());
      
      link.parse("https://secure.com/index.html");
      assertEquals("https", link.getScheme());
      assertEquals("secure.com", link.getDomain());
   }
}        
