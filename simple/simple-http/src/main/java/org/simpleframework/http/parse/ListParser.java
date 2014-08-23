/*
 * ListParser.java September 2003
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

import static java.lang.Long.MAX_VALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.simpleframework.common.parse.Parser;

/**
 * The <code>ListParser</code> is used to extract a comma separated 
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
public abstract class ListParser<T> extends Parser {
   
   /**
    * Provides a quick means of sorting the values extracted.
    */
   private PriorityQueue<Entry> order;
   
   /**
    * Contains all the values extracted from the header(s).
    */
   private List<T> list;

   /**
    * This is used as a working space to parse the value. 
    */
   private char[] text;

   /**
    * The quality associated with an individual value.
    */ 
   private long qvalue;

   /**
    * Used to index into the write offset for the value. 
    */
   private int pos;
   
   /**
    * This is used to determine whether to gather tokens.
    */
   private boolean build;

   /**
    * Constructor for the <code>ListParser</code>. This creates a
    * parser with no initial parse data, if there are headers to
    * be parsed then the <code>parse(String)</code> method or
    * <code>parse(List)</code> method can be used. This will
    * parse a delimited list according so RFC 2616 section 4.2.
    */
   public ListParser(){
      this.order = new PriorityQueue<Entry>(); 
      this.list = new ArrayList<T>();
      this.text = new char[0];
   }

   /**
    * Constructor for the <code>ListParser</code>. This creates a
    * parser with the text supplied. This will parse the comma
    * separated list according to RFC 2616 section 2.1 and 4.2.
    * The tokens can be extracted using the <code>list</code>
    * method, which will also sort and trim the tokens.
    * 
    * @param text this is the comma separated list to be parsed
    */
   public ListParser(String text) {
      this();
      parse(text);
   }
   
   /**
    * Constructor for the <code>ListParser</code>. This creates a
    * parser with the text supplied. This will parse the comma
    * separated list according to RFC 2616 section 2.1 and 4.2.
    * The tokens can be extracted using the <code>list</code>
    * method, which will also sort and trim the tokens.
    * 
    * @param list a list of comma separated lists to be parsed
    */
   public ListParser(List<String> list) {
      this();
      parse(list);
   }
   
   /**
    * This allows multiple header values to be represented as one
    * single comma separated list. RFC 2616 states that multiple 
    * message header fields with the same field name may be present
    * in a message if and only if the entire field value for that
    * header field is defined as a comma separated list. This means
    * that if there are multiple header values with the same name
    * they can be combined into a single comma separated list.
    *
    * @param list this is a list of header values to be combined
    */
   public void parse(List<String> list) {     
      for(String value : list) {
         parse(value);
         build = true;
      }
      build = false;
   }

   /**
    * This will build an ordered list of values extracted from the
    * comma separated header value. This enables the most preferred
    * token, to be taken from the first index of the array and the
    * least preferred token to be taken from the last index.
    * 
    * @return tokens parsed from the list ordered by preference
    */
   public List<T> list() {
      return list;
   }
   
   /** 
    * This is used to remove the <code>String</code> tokens from 
    * the priority queue and place those tokens in an array. The
    * The <code>String</code> tokens are placed into the array 
    * in an ordered manner so that the most preferred token is
    * inserted into the start of the list.
    */
   private void build() {
      while(!order.isEmpty()) {
         Entry entry = order.remove();
         T value = entry.getValue();
         
         list.add(value);
      }
   }
   
   /**
    * This ensures that tokens are taken from the comma separated
    * list as long as there bytes left to be examined within the
    * source text. This also makes sure that the implicit qvalue
    * is decreased each time a token is extracted from the list.
    */
   protected void parse() {
      while(off < count) {
         clear();
         value();
         save();
      }
      build();
   }

   /**
    * Initializes the parser so that tokens can be extracted from 
    * the list. This creates a write buffer so that a if there is 
    * only one token as long as the source text, then that token
    * can be accommodated, also this starts of the initial qvalue
    * implicit to tokens within the list as the maximum long value.
    * <p>
    * One thing that should be noted is that this will not empty
    * the priority queue on each string parsed. This ensures that
    * if there are multiple strings they can be parsed quickly 
    * and also contribute to the final result.
    */
   protected void init(){
      if(text.length < count){
         text = new char[count];
      }
      if(!build) {
         list.clear();
      }
      pos = off = 0;
      order.clear();
   }

   /**
    * This is used to return the parser to a semi-initialized state.
    * After extracting a token from the list the buffer will have
    * accumulated bytes, this ensures that bytes previously written
    * to the buffer do not interfere with the next token extracted.
    * <p>
    * This also ensures the implicit qvalue is reset to the maximum
    * long value, so that the next token parsed without a qvalue
    * will have the highest priority and be placed at the top of 
    * the list. This ensures order is always maintained.
    */
   private void clear() {
      qvalue = MAX_VALUE;
      pos = 0;
   }

   /**
    * This method will extract a token from a comma separated list
    * and write it to a buffer. This performs the extraction in such
    * a way that it can tolerate literals, parameters, and quality
    * value parameters. The only alterations made to the token by
    * this method is the removal of quality values, that is, qvalue
    * parameters which have the name "q". Below is an example of 
    * some of the lists that this can parse.
    * <pre>
    *
    *    token; quantity=1;q=0.001, token; text="a, b, c, d";q=0
    *    image/gif, , image/jpeg, image/png;q=0.8, *
    *    token="\"a, b, c, d\", a, b, c, d", token="a";q=0.9,,
    *    
    * </pre>
    * This will only interpret a comma delimiter outside quotes of
    * a literal. So if there are comma separated tokens that have
    * quoted strings, then commas within those quoted strings will
    * not upset the extraction of the token. Also escaped strings
    * are tolerated according to RFC 2616 section 2.
    */
   private void value() {
      parse: while(off < count) {
         if(buf[off++] == '"'){ /* "[t]ext" */
            text[pos++] = buf[off-1]; /* ["]text"*/
            while(++off < count){   /* "text"[] */ 
               if(buf[off -1] =='"'){ /* "text["] */
                  if(buf[off -2] !='\\')
                     break;
               }
               text[pos++] = buf[off-1]; /* "tex[t]"*/
            }
         } else if(buf[off -1] == ';'){ /* [;] q=0.1 */
            for(int seek = off; seek+1 < count;){/* ;[ ]q=0.1 */
               if(!space(buf[seek])){  /* ;[ ]q=0.1*/
                  if(buf[seek] =='q'){ /* ; [q]=0.1*/
                     if(buf[seek+1] =='='){ /* ; q[=]0.1*/
                        off = seek;
                        qvalue();
                        continue parse;
                     }
                  }
                  break;
               }
               seek++;
            }
         } 
         if(buf[off-1] ==','){
            break;
         }
         text[pos++] = buf[off-1];
      }
   }
   
   /**
    * This method will trim whitespace from the extracted token and
    * store that token within the <code>PriorityQueue</code>. This
    * ensures that the tokens parsed from the comma separated list
    * can be used. Trimming the whitespace is something that will be
    * done to the tokens so that they can be examined, so this 
    * ensures that the overhead of the <code>String.trim</code> 
    * method is not required to remove trailing or leading spaces.
    * This also ensures that empty tokens are not saved.
    */
   private void save() {
      int size = pos;
      int start = 0;

      while(size > 0){
         if(!space(text[size-1])){
            break;
         }
         size--;
      }
      while(start < pos){
         if(space(text[start])){
            start++;
            size--;
         }else {
            break;
         }
      }
      if(size > 0) {
         T value = create(text, start, size);
         
         if(value != null) {
            save(value);
         }
      }
   }
      
   /**
    * This stores the string in the <code>PriorityQueue</code>. If
    * the qvalue extracted from the header value is less that 0.001
    * then this will not store the token. This ensures that client
    * applications can specify tokens that are unacceptable to it.
    *
    * @param value this is the token to be enqueued into the queue
    */
   private void save(T value) {
      int size = order.size();
      
      if(qvalue > 0) {
         order.offer(new Entry(value, qvalue, size));
      }
   }
   
   /** 
    * This is used to extract the qvalue parameter from the header. 
    * The qvalue parameter is identified by a parameter with the 
    * name "q" and a numeric floating point number. The number can 
    * be in the range of 0.000 to 1.000. The <code>qvalue</code> 
    * is parsed byte bit shifting a byte in to a value in to a
    * long, this may cause problems with varying accuracy.
    */
   private void qvalue() {
      if(skip("q=")){         
         char digit = 0;

         for(qvalue = 0; off < count;){
            if(buf[off] == '.'){
               off++;
               continue;
            }
            if(!digit(buf[off])){
               break;
            }
            digit = buf[off]; 
            digit -= '0';  
            qvalue |= digit;  
            qvalue <<= 4;
            off++;   
         }
      }
   }
   
   /**
    * This creates an value object using the range of characters 
    * that have been parsed as an item within the list of values. It
    * is up to the implementation to create a value to insert in to
    * the list. A null value will be ignored if returned.
    * 
    * @param text this is the text buffer to acquire the value from
    * @param start the offset within the array to take characters
    * @param len this is the number of characters within the token
    */
   protected abstract T create(char[] text, int start, int len);
   
   /**
    * The <code>Entry</code> object provides a comparable object to
    * insert in to a priority queue. This will sort the value using
    * the quality value parameter parsed from the list. If there 
    * are values with the same quality value this this will sort
    * the values by a secondary order parameter.
    */
   private class Entry implements Comparable<Entry> {
      
      /**
       * This is the value that is represented by this entry.
       */
      private final T value;
      
      /**
       * This is the priority value that is used to sort entries.
       */
      private final long priority;
      
      /**
       * This is the secondary order value used to sort entries. 
       */
      private final int order;
      
      /**
       * Constructor for the <code>Entry</code> object. This is used
       * to create a comparable value that can be inserted in to a
       * priority queue and extracted in order of the priority value.
       * 
       * @param value this is the value that is represented by this
       * @param priority this is the priority value for sorting
       * @param order this is the secondary priority value used
       */
      public Entry(T value, long priority, int order) {
         this.priority = priority;
         this.order = order;
         this.value = value; 
      }
      
      /**
       * This acquires the value represented by this entry. This is
       * can be used to place the value within a list as it is taken
       * from the priority queue. Acquiring the values in this way
       * facilitates a priority ordered list of values.
       * 
       * @return this returns the value represented by this
       */
      public T getValue() {
         return value;
      }
      
      /**
       * This is used to sort the entries within the priority queue
       * using the provided priority of specified. If the entries 
       * have the same priority value then they are sorted using a
       * secondary order value, which is the insertion index.
       * 
       * @param entry this is the entry to be compared to
       * 
       * @return this returns the result of the entry comparison
       */
      public int compareTo(Entry entry) {
         long value = entry.priority - priority;
         
         if(value > 0) {
            return 1;
         } 
         if(value < 0) {
            return -1;
         }
         return order - entry.order;
      }
   }
}