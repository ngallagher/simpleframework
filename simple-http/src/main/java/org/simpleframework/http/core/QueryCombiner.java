/*
 * QueryCombiner.java May 2003
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
  
package org.simpleframework.http.core;

import java.util.List;
import java.util.Set;

import org.simpleframework.http.Query;
import org.simpleframework.http.parse.QueryParser;

/**
 * The <code>QueryCombimer</code> is used to parse several strings
 * as a complete URL encoded parameter string. This will do the
 * following concatenations.
 *
 * <pre>
 * null + "a=b&amp;c=d&amp;e=f" = "a=b&amp;c=d&amp;e=f"
 * "a=b" + "e=f&amp;g=h" = "a=b&amp;e=f&amp;g=h";
 * "a=b&amp;c=d&amp;e=f" + "" = "a=b&amp;c=d&amp;e=f"
 * </pre>
 *
 * This ensures that the <code>QueryForm</code> can parse the list
 * of strings as a single URL encoded parameter string. This can
 * parse any number of parameter strings.
 *
 * @author Niall Gallagher
 */
class QueryCombiner extends QueryParser {
   
   /**
    * Constructor that allows a list of string objects to be
    * parsed as a single parameter string. This will check
    * each string to see if it is empty, that is, is either
    * null or the zero length string.
    * 
    * @param list this is a list of query values to be used
    */
   public QueryCombiner(String... list) {
      this.parse(list);
   }

   /**
    * Constructor that allows an array of string objects to 
    * be parsed as a single parameter string. This will check
    * each string to see if it is empty, that is, is either
    * null or the zero length string.
    * 
    * @param query this is the query from the HTTP header     
    * @param list this is the list of strings to be parsed
    */
   public QueryCombiner(Query query, String... list) {
      this.add(query);
      this.parse(list);      
   }
   
   /**
    * Constructor that allows an array of string objects to 
    * be parsed as a single parameter string. This will check
    * each string to see if it is empty, that is, is either
    * null or the zero length string.
    * 
    * @param query this is the query from the HTTP header
    * @param post this is the query from the HTTP post body
    */
   public QueryCombiner(Query query, Query post) {
      this.add(query);
      this.add(post);
   }

   /**
    * This will concatenate the list of parameter strings as a 
    * single parameter string, before handing it to be parsed
    * by the <code>parse(String)</code> method. This method 
    * will ignore any null or zero length strings in the array.    
    *
    * @param list this is the list of strings to be parsed
    */
   public void parse(String[] list) {
      StringBuilder text = new StringBuilder();
      
      for(int i = 0; i < list.length; i++) {
         if(list[i] == null) {
            continue;
         } else if(list[i].length()==0){
            continue;
         } else if(text.length() > 0){
            text.append("&");
         }
         text.append(list[i]);        
      }      
      parse(text);
   }
   
   /**
    * This is used to perform a parse of the form data that is in
    * the provided string builder. This will simply convert the
    * data in to a string and parse it in the normal fashion.
    * 
    * @param text this is the buffer to be converted to a string
    */
   private void parse(StringBuilder text) {
      if(text != null){ 
         ensureCapacity(text.length());
         count = text.length();
         text.getChars(0, count, buf,0);
         parse();
      }
   }   
   
   /**
    * This method is used to insert a collection of tokens into 
    * the parsers map. This is used when another source of tokens
    * is required to populate the connection currently maintained
    * within this parsers internal map. Any tokens that currently
    * exist with similar names will be overwritten by this.
    * 
    * @param query this is the collection of tokens to be added
    */
   private void add(Query query) {
      Set<String> keySet = query.keySet();
      
      for(String key : keySet) {
         List<String> list = query.getAll(key);
         String first = query.get(key);
         
         if(first != null) {
            all.put(key, list);
            map.put(key, first);
         }
      }                 
   }
}
