package org.simpleframework.http.parse;

import junit.framework.TestCase;

public class LanguageParserTest extends TestCase {
   
   public void testLanguages() throws Exception {
      LanguageParser parser = new LanguageParser();
      
      parser.parse("en-gb,en;q=0.5");
      
      assertEquals(parser.list().get(0).getLanguage(), "en");
      assertEquals(parser.list().get(0).getCountry(), "GB");
      assertEquals(parser.list().get(1).getLanguage(), "en");
      assertEquals(parser.list().get(1).getCountry(), "");
      
      parser.parse("en-gb,en;q=0.5,*;q=0.9");
      
      assertEquals(parser.list().get(0).getLanguage(), "en");
      assertEquals(parser.list().get(0).getCountry(), "GB");
      assertEquals(parser.list().get(1).getLanguage(), "*");
      assertEquals(parser.list().get(2).getLanguage(), "en");
      assertEquals(parser.list().get(2).getCountry(), "");
   }

}
