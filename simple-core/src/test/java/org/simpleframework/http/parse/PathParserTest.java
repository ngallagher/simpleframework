package org.simpleframework.http.parse;

import junit.framework.TestCase;

import org.simpleframework.http.parse.PathParser;

public class PathParserTest extends TestCase {

   private PathParser path;
        
   protected void setUp() {
      path = new PathParser();           
   } 

   public void testEmpty() {
      assertEquals(null, path.getPath());
      assertEquals(null, path.getExtension());
      assertEquals(null, path.getName());
   }

   public void testSegments() {
      path.parse("/a/b/c/d");
      
      String[] list = path.getSegments();

      assertEquals("a", list[0]);
      assertEquals("b", list[1]);
      assertEquals("c", list[2]);
      assertEquals("d", list[3]);
   }
   
   public void testSubPath() {
      path.parse("/0/1/2/3/4/5/6/index.html");
      
      testSubPath(1);
      testSubPath(2);
      testSubPath(3);
      testSubPath(4);
      testSubPath(5);
      testSubPath(6);
      testSubPath(7);
      
      testSubPath(0,4);
      testSubPath(1,2);
      testSubPath(2,3);
      testSubPath(3,4);
      testSubPath(1,3);
      testSubPath(1,4);
      testSubPath(1,5);
      
      path.parse("/a/b/c/d/e/index.html");
      
      testSubPath(1,2);
      testSubPath(2,3);
      testSubPath(3,1);
      testSubPath(1,3);
   }
   
   private void testSubPath(int from) {
      System.err.printf("[%s] %s: %s%n", path, from, path.getPath(from));
   }
   
   private void testSubPath(int from, int to) {
      System.err.printf("[%s] %s, %s: %s%n", path, from, to, path.getPath(from, to));
   }

   public void testDirectory() {
      path.parse("/some/directory/path/index.html"); 
      assertEquals("/some/directory/path/", path.getDirectory());      

      path.parse("/some/path/README");
      assertEquals("/some/path/", path.getDirectory());
   }

   public void testNormalization() {
      path.parse("/path/./../index.html");
      assertEquals("/", path.getDirectory());      
      
      path.parse("/path/hidden/./index.html");
      assertEquals("/path/hidden/", path.getDirectory());

      path.parse("/path/README");
      assertEquals("/path/", path.getDirectory());
   }

   public void testString() {
      path.parse("/some/path/../path/./to//a/file.txt"); 
      assertEquals("/some/path/to/a/file.txt", path.toString());
   }
   
   public void testAIOB(){
      path.parse("/admin/ws");
      String result = path.getRelative("/admin/ws/");
      String expResult = null;
      assertEquals(expResult, result);
   }
}        
