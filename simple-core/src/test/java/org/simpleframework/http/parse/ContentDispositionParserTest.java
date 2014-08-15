package org.simpleframework.http.parse;

import org.simpleframework.http.parse.ContentDispositionParser;

import junit.framework.TestCase;

public class ContentDispositionParserTest extends TestCase {
	
   private ContentDispositionParser parser;
	
   public void setUp() {
      parser = new ContentDispositionParser();
   }

   public void testDisposition() {
      parser.parse("form-data; name=\"input_check\"");

      assertFalse(parser.isFile());
      assertEquals(parser.getName(), "input_check");

      parser.parse("form-data; name=\"input_password\"");

      assertFalse(parser.isFile());
      assertEquals(parser.getName(), "input_password");

      parser.parse("form-data; name=\"FileItem\"; filename=\"C:\\Inetpub\\wwwroot\\Upload\\file1.txt\"");

      assertTrue(parser.isFile());
      assertEquals(parser.getName(), "FileItem");
      assertEquals(parser.getFileName(), "C:\\Inetpub\\wwwroot\\Upload\\file1.txt");

   }
}
