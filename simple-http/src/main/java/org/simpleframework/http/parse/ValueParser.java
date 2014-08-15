/*
 * ValueParser.java September 2003
 *
 * Copyright (C) 2003, Niall Gallagher <niallg@users.sf.net>
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

/**
 * The <code>ValueParser</code> is used to extract a comma separated 
 * list of HTTP header values. This will extract values without 
 * any leading or trailing spaces, which enables the values to be
 * used. Listing the values that appear in the header also requires
 * that the values are ordered. This orders the values using the 
 * values that appear with any quality parameter associated with it.
 * The quality value is a special parameter that often found in a
 * comma separated value list to specify the client preference.
 * <pre>
 * 
 *    image/gif, image/jpeg, text/html
 *    image/gif;q=1.0, image/jpeg;q=0.8, image/png;  q=1.0,*;q=0.1
 *    gzip;q=1.0, identity; q=0.5, *;q=0
 *
 * </pre>
 * The above lists taken from RFC 2616 provides an example of the
 * common form comma separated values take. The first illustrates
 * a simple comma delimited list, here the ordering of values is
 * determined from left to right. The second and third list have
 * quality values associated with them, these are used to specify
 * a preference and thus order. 
 * <p> 
 * Each value within a list has an implicit quality value of 1.0.
 * If the value is explicitly set with a the "q" parameter, then
 * the values can range from 1.0 to 0.001. This parser ensures
 * that the order of values returned from the <code>list</code>
 * method adheres to the optional quality parameters and ensures
 * that the quality parameters a removed from the resulting text.
 * 
 * @author Niall Gallagher
 */
public class ValueParser extends ListParser<String> {
   
   /**
    * Constructor for the <code>ValueParser</code>. This creates 
    * a parser with no initial parse data, if there are headers to
    * be parsed then the <code>parse(String)</code> method or
    * <code>parse(List)</code> method can be used. This will
    * parse a delimited list according so RFC 2616 section 4.2.
    */
   public ValueParser(){
      super();
   }

   /**
    * Constructor for the <code>ValueParser</code>. This creates 
    * a parser with the text supplied. This will parse the comma
    * separated list according to RFC 2616 section 2.1 and 4.2.
    * The tokens can be extracted using the <code>list</code>
    * method, which will also sort and trim the tokens.
    * 
    * @param text this is the comma separated list to be parsed
    */
   public ValueParser(String text) {
      super(text);
   }
   
   /**
    * Constructor for the <code>ValueParser</code>. This creates 
    * a parser with the text supplied. This will parse the comma
    * separated list according to RFC 2616 section 2.1 and 4.2.
    * The tokens can be extracted using the <code>list</code>
    * method, which will also sort and trim the tokens.
    * 
    * @param list a list of comma separated lists to be parsed
    */
   public ValueParser(List<String> list) {
      super(list);
   }
   
   /**
    * This creates a string object using an offset and a length.
    * The string is created from the extracted token and the offset
    * and length ensure that no leading or trailing whitespace are
    * within the created string object. 
    *
    * @param text this is the text buffer to acquire the value from
    * @param start the offset within the buffer to take characters
    * @param len this is the number of characters within the token
    */
   @Override
   protected String create(char[] text, int start, int len){
      return new String(text, start, len);
   }
}