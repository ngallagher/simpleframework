/*
 * LanguageParser.java February 2001
 *
 * Copyright (C) 2001, Niall Gallagher <niallg@users.sf.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
 
package org.simpleframework.http.parse;

import java.util.List;
import java.util.Locale;

/** 
 * LanguageParser is used to parse the HTTP <code>Accept-Language</code> 
 * header. This takes in an <code>Accept-Language</code> header and parses 
 * it according the RFC 2616 BNF for the <code>Accept-Language</code> header. 
 * This also has the ability to sequence the language tokens in terms of 
 * the most preferred and the least preferred. 
 * <p>
 * This uses the qvalues outlined by RFC 2616 to order the language tokens 
 * by preference. Typically the language tokens will not have qvalues with 
 * the language. However when a language tag has the qvalue parameter then 
 * this tag will be ordered based on the value of that parameter. A language 
 * tag without the qvalue parameter is considered to have a qvalue of 1 and 
 * is ordered accordingly.
 *
 * @author Niall Gallagher
 */
public class LanguageParser extends ListParser<Locale> {

   /**
    * This is used to create a <code>LanguageParser</code> for the
    * <code>Accept-Language</code> HTTP header value. This will
    * parse a set of language tokens and there parameters. The
    * languages will be ordered on preference. This constructor
    * will parse the value given using <code>parse(String)</code>.
    */
   public LanguageParser() {
      super();
   }
   
   /**
    * This is used to create a <code>LanguageParser</code> for the
    * <code>Accept-Language</code> HTTP header value. This will
    * parse a set of language tokens and there parameters. The
    * languages will be ordered on preference. This constructor
    * will parse the value given using <code>parse(String)</code>.
    *
    * @param text value of a <code>Accept-Language</code> header
    */   
   public LanguageParser(String text) {
      super(text);
   }
   
   /**
    * This is used to create a <code>LanguageParser</code> for the
    * <code>Accept-Language</code> HTTP header value. This will
    * parse a set of language tokens and there parameters. The
    * languages will be ordered on preference. This constructor
    * will parse the value given using <code>parse(String)</code>.
    *
    * @param list value of a <code>Accept-Language</code> header
    */   
   public LanguageParser(List<String> list) {
      super(list);
   }
   
   /**
    * This creates a locale object using an offset and a length.
    * The locale is created from the extracted token and the offset
    * and length ensure that no leading or trailing whitespace are
    * within the created locale object. 
    * 
    * @param text this is the text buffer to acquire the value from
    * @param start the offset within the array to take characters
    * @param len this is the number of characters within the token
    */
   @Override
   protected Locale create(char[] text, int start, int len){
      String language = language(text, start, len);
      String country = country(text, start, len);
      
      return new Locale(language, country);
   }
   
   /**
    * This will extract the primary language tag from the header.
    * This token is used to represent the language that will be
    * available in the <code>Locale</code> object created.  
    * 
    * @param text this is the text buffer to acquire the value from
    * @param start the offset within the array to take characters
    * @param len this is the number of characters within the token
    */
   private String language(char[] text, int start, int len) {
      int mark = start;
      int size = 0;
      
      while(start < len) {
         char next = text[start];
         
         if(terminal(next)) {
            return new String(text, mark, size);
         }
         size++;
         start++;
      }
      return new String(text, mark, len);    
   }
   
   /**
    * This will extract the primary country tag from the header.
    * This token is used to represent the country that will be
    * available in the <code>Locale</code> object created.  
    * 
    * @param text this is the text buffer to acquire the value from
    * @param start the offset within the array to take characters
    * @param len this is the number of characters within the token
    */
   private String country(char[] text, int start, int len) {
      int size = len;
      
      while(start < len) {
         if(text[start++] == '-') {
            return new String(text, start, --size);
         }
         size--;
      }
      return "";
   }
   
   /**
    * This is used to determine whether the character provided is 
    * a terminal character. The terminal token is the value that is
    * used to separate the country from the language and also any
    * character the marks the end of the language token.
    *  
    * @param ch this is the character that is to be evaluated
    * 
    * @return true if the character represents a terminal token
    */
   private boolean terminal(char ch) {
      return ch ==' ' || ch == '-' || ch == ';';
   }
}
