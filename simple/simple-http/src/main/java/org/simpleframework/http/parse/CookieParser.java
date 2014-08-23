/*
 * CookieParser.java February 2001
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

import org.simpleframework.common.parse.Parser;
import org.simpleframework.http.Cookie;

import java.util.Iterator;

/**
 * CookieParser is used to parse the cookie header. The cookie header is
 * one of the headers that is used by the HTTP state management mechanism.
 * The Cookie header is the header that is sent from the client to the
 * server in response to a Set-Cookie header. The syntax of the Cookie
 * header as taken from RFC 2109, HTTP State Management Mechanism.
 * <pre>
 *
 *  cookie          =       "Cookie:" cookie-version
 *                          1*((";" | ",") cookie-value)
 *  cookie-value    =       NAME "=" VALUE [";" path] [";" domain]
 *  cookie-version  =       "$Version" "=" value
 *  NAME            =       attr
 *  VALUE           =       value
 *  path            =       "$Path" "=" value
 *  domain          =       "$Domain" "=" value
 *
 * </pre>
 * The cookie header may consist of several cookies. Each cookie can be
 * extracted from the header by examining the it syntax of the cookie
 * header. The syntax of the cookie header is defined in RFC 2109.
 * <p>
 * Each cookie has a <code>$Version</code> attribute followed by multiple
 * cookies. Each contains a name and a value, followed  by an optional
 * <code>$Path</code> and <code>$Domain</code> attribute. This will parse
 * a given cookie header and return each cookie extracted as a
 * <code>Cookie</code> object.
 *
 * @author Niall Gallagher
 */
public class CookieParser extends Parser implements Iterable<Cookie> {

   /**
    * Determines when the <code>Parser</code> has finished.
    */
   private boolean finished;

   /**
    * Used so the <code>Parser</code> does not parse twice.
    */
   private boolean parsed;

   /**
    * Version of the <code>Cookie</code> being parsed.
    */
   private int version;

   /**
    * Used to store the name of the <code>Cookie</code>.
    */
   private Token name;

   /**
    * Used to store the value of the <code>Cookie</code>.
    */
   private Token value;

   /**
    * Used to store the <code>$Path</code> values.
    */
   private Token path;

   /**
    * Used to store the <code>$Domain</code> values.
    */
   private Token domain;

   /**
    * Create a <code>CookieParser</code> that contains no cookies.
    * the instance will return <code>false</code> for the
    * <code>hasNext</code> method. cookies may be parsed using
    * this instance by using the <code>parse</code> method.
    */
   public CookieParser(){
      this.path = new Token();
      this.domain = new Token();
      this.name = new Token();
      this.value = new Token();
      this.finished = true;
   }

   /**
    * This is primarily a convineance constructor. This will parse the
    * <code>String</code> given to extract the cookies. This could be
    * achived by calling the default no-arg constructor and then using
    * the instance to invoke the <code>parse</code> method on that
    * <code>String</code>.
    *
    * @param header a <code>String</code> containing a cookie value
    */
   public CookieParser(String header){
      this();
      parse(header);
   }

   /**
    * Resets the cookie and the buffer variables for this
    * <code>CookieParser</code>. It is used to set the
    * state of the parser to start parsing a new cookie.
    */
   protected void init() {
      finished = false;
      parsed =false;
      version = 0;
      off = 0;
      version();
   }

   /**
    * This will extract the next <code>Cookie</code> from the
    * buffer. If all the characters in the buffer have already
    * been examined then this method will simply do nothing.
    * Otherwise this will parse the remainder of the buffer
    * and (if it follows RFC 2109) produce a <code>Cookie</code>.
    */
   protected void parse() {
      if(!finished){
         cookie();
         parsed=true;
      }
   }

   /**
    * This is used to skip an arbitrary <code>String</code> within the
    * <code>char</code> buf. It checks the length of the <code>String</code>
    * first to ensure that it will not go out of bounds. A comparison
    * is then made with the buffers contents and the <code>String</code>
    * if the reigon in the buffer matched the <code>String</code> then the
    * offset within the buffer is increased by the <code>String</code>'s
    * length so that it has effectively skipped it.
    * <p>
    * This <code>skip</code> method will ignore all of the whitespace text.
    * This will also skip trailing spaces within the the input text and
    * all spaces within the source text. For example if the input was
    * the string "s omete xt" and the source was "some text to skip" then
    * the result of a skip ignoring spaces would be "to skip" in the
    * source string, as the trailing spaces are also eaten by this.
    *
    * @param text this is the <code>String</code> value to be skipped
    *
    * @return true if the <code>String</code> was skipped
    */
   protected boolean skip(String text){      
      int size = text.length();
      int seek = off;
      int read = 0;

      if(off + size > count){
         return false;
      }
      while(read < size) {
         char a = text.charAt(read);
         char b = buf[seek];

         if(space(b)){
            if(++seek >= count){
               return false;
            }
         }else if(space(a)){
            if(++read >= size) {
               continue;
            }
         }else {
            if(toLower(a) != toLower(b)){
               return false;
            }
            read++;
            seek++;
         }
      }
      for(off = seek; off < count; off++){
         if(!space(buf[off]))
            break;
      }
      return true;
   }
   
   /**
    * This is used to acquire the cookie values from the provided 
    * the provided source text. This allows the cookie parser to be
    * used within a for each loop to parse out the values of a
    * cookie one by one so that they may be used or stored.
    * 
    * @return this returns an iterator for extracting cookie value
    */
   public Iterator<Cookie> iterator() {
      return new Sequence();
   }

   /**
    * This is used so that the collection of <code>Cookies</code>
    * can be reiterated. This allows the collection to be reused.
    * The <code>reset</code> method will invoke the super classes
    * <code>init</code> method. This will reinitialize this
    * <code>Parser</code> so the cookie will be reparsed.
    */
   public void reset() {
      init();
      parse();
   }

   /**
    * Creates the <code>Cookie</code> from the token objects. It is
    * assumed that the <code>Cookie</code> <code>String</code> has
    * been parsed when this is called. This should only be used after
    * the <code>parse</code> method has been called.
    * <p>
    * If there is no <code>$Domain</code> or <code>$Path</code>
    * within  the <code>Cookie</code> <code>String</code> then the
    * <code>getDomain</code> and <code>getPath</code> are null.
    *
    * @return the <code>Cookie</code> that was just parsed
    */
   private Cookie getCookie() {
      return getCookie(name.toString(),
         value.toString());
   }

   /**
    * Creates the <code>Cookie</code> from the token objects. It is
    * assumed that the <code>Cookie</code> <code>String</code> has
    * been parsed when this is called. This should only be used after
    * the <code>parse</code> method has been called.
    * <p>
    * If there is no <code>$Domain</code> or <code>$Path</code>
    * within  the <code>Cookie</code> <code>String</code> then the
    * <code>getDomain</code> and <code>getPath</code> are null.
    *
    * @param name the name that the <code>Cookie</code> contains
    * @param value the value that the <code>Cookie</code> contains
    *
    * @return the <code>Cookie</code> that was just parsed
    */
   private Cookie getCookie(String name, String value) {
      Cookie cookie = new Cookie(name, value, false);
      
      if(domain.len > 0) {
         cookie.setDomain(domain.toString());
      }
      if(path.len > 0) {
         cookie.setPath(path.toString());
      }
      cookie.setVersion(version);
      return cookie;
   }

   /**
    * This is used to parse a <code>Cookie</code> from the buffer
    * that contains the <code>Cookie</code> values. This will first
    * try to remove any trailing value after the version/prev
    * <code>Cookie</code> once this is removed it will extract the
    * name/value pair from the <code>Cookie</code>. The name and
    * value of the <code>Cookie</code> will be saved by the name
    * and value tokens.
    */
   private void cookie(){
      if(!skip(",")){ /* ,|; */
         skip(";");
      }
      name();
      skip("="); /* = */
      value();
   }

   /**
    * This initializes the name token and extracts the name of this
    * <code>Cookie</code>. The offset and length of the name will be
    * saved in the name token. This will read all <code>char</code>'s
    * upto but excluding the first '=' <code>char</code> encountered
    * from the <code>off</code> within the buffer.
    */
   private void name() {
      name.off = off;
      name.len = 0;
      while(off < count){
         if(buf[off] == '='){
            break;
         }
         name.len++;
         off++;
      }
   }

   /**
    * Used to extract everything found after the <code>NAME '='</code>
    * within a <code>Cookie</code>. This extracts the <code>Cookie</code>
    * value the <code>$Path</code> and <code>$Domain</code> attributes
    * if they exist (i.e. <code>$Path</code> and <code>$Domain</code>
    * are optional in a cookie see RFC 2109).
    * <p>
    * The path method reads the terminal found before it as does the
    * <code>domain</code> method that is ";$Path" is read as the first
    * part of the path method. This is because if there is no path the
    * parser should not read data it does not know belongs to a specific
    * part of the <code>Cookie</code>.
    */
   private void value() {
      data();
      path();
      domain();
   }

   /**
    * This initializes the value token and extracts the value of this
    * <code>Cookie</code>. The offset and length of the value will be
    * saved in the value token. This will read all <code>char</code>'s
    * upto but excluding the first terminal char encountered from the
    * off within the buffer, or if the value is a literal it will read
    * a literal from the buffer (literal is any data between quotes
    * except if the quote is prefixed with a backward slash character
    * that is '\').
    */
   private void data() {
      value.off = off;
      value.len = 0;
      if(off < count && buf[off] == '"'){
         value.len++;
         for(off++; off < count;){
            value.len++;
            if(buf[off++]=='"')
               if(buf[off-2]!='\\'){
                  break;
               }
         }
         value.len-=2;  /* remove " */
         value.off++; /* remove " */
      }else {
         while(off < count){
            if(terminal(buf[off]))
               break;
            value.len++;
            off++;
         }
      }
   }

   /**
    * This initializes the path token and extracts the <code>$Path</code>
    * of this <code>Cookie</code>. The offset and length of the path will
    * be saved in the path token. This will read all <code>char</code>'s
    * up to but excluding the first terminal <code>char</code> encountered
    * from the <code>off</code> within the buffer, or if the value is a
    * literal it will read a literal from the buffer (literal is any data
    * between quotes except if the quote is prefixed with a backward slash
    * character, that is '\').
    * <p>
    * This reads the terminal before the <code>$Path</code> so that if
    * there is no <code>$Path</code> for the <code>Cookie</code> then
    * the character before it will not be read needlessly.
    */
   private void path() {
      path.len = 0; /* reset */
      if(skip(";$Path=")){
         path.off = off;
         if(buf[off] == '"'){
            path.len++;
            for(off++; off < count;){
               path.len++;
               if(buf[off++]=='"')
                  if(buf[off-2]!='\\'){
                     break;
                  }
            }
            path.len-=2;  /* remove " */
            path.off++; /* remove " */
         }else{
            while(off < count){
               if(terminal(buf[off]))
                  break;
               path.len++;
               off++;
            }
         }
      }
   }

   /**
    * Initializes the domain token and extracts the <code>$Domain</code>
    * of this <code>Cookie</code>. The offset and length of the domain
    * will be saved in the path token. This will read all characters up
    * to but excluding the first terminal <code>char</code> encountered
    * from the off within the buffer, or  if the value is a literal it
    * will read a literal from the buffer (literal is any data between
    * quotes except if the quote is prefixed with a backward slash
    * character, that is '\').
    * <p>
    * This reads the terminal before the <code>$Domain</code> so that
    * if there is  no <code>$Domain</code> for the <code>Cookie</code>
    * then the character before it will not be read needlessly.
    */
   private void domain(){
      domain.len = 0;   /* reset */
      if(skip(";$Domain=")) {
         domain.off = off;
         if(buf[off] == '"'){
            domain.len++;
            for(off++; off < count;){
               domain.len++;
               if(buf[off++]=='"')
                  if(buf[off-2]!='\\'){
                     break;
                  }
            }
            domain.len-=2;  /* remove " */
            domain.off++; /* remove " */
         }else{
            while(off < count){
               if(terminal(buf[off]))
                  break;
               domain.len++;
               off++;
            }
         }
      }
   }

   /**
    * This extracts the <code>$Version</code> of this <code>Cookie</code>.
    * The version is parsed and converted into a decimal int from the digit
    * characters that make up a version.
    * <p>
    * This will read all digit <code>char</code>'s up to but excluding the
    * first non digit <code>char</code> that it encounters from the offset
    * within the buffer, or if the value is a literal it will read a literal
    * from the buffer (literal is any data between quotes except if the quote
    * is prefixed with a backward slash character i.e. '\').
    */
   private void version(){
      if(skip("$Version=")) {
         if(buf[off] == '"'){
            off++;
         }
         while(off < count){
            if(!digit(buf[off])){
               break;
            }
            version *= 10;
            version += buf[off];
            version -= '0';
            off++;
         }
         if(buf[off] == '"'){
            off++;
         }
      }else{
         version = 1;
      }
   }

   /**
    * This is used to determine if a given  iso8859-1 character is
    * a terminal character. That is either the ';' or ','
    * characters. Although the RFC 2109 says the terminal can be
    * either a comma, it is not used by any browsers.
    *
    * @param ch the character that is to be compared
    *
    * @return true if this is a semicolon character
    */
   private boolean terminal(char ch) {
      return ch == ';'; 
   }

   /**
    * This is used to represent an <code>Iterator</code> that will 
    * iterate over the available cookies within the provided source
    * text. This allows the cookie parser to be used as an iterable
    * with for each loops. Cookies can not be removed with this.
    */
   private class Sequence implements Iterator<Cookie> {
      
      /**
       * Extracts the next <code>Cookie</code> object from the string
       * given. This will return <code>null</code> when there are no
       * more cookies left in the <code>String</code> being parsed.
       * <p>
       * To find out when there are no more cookies left use the
       * <code>hasNext</code> method. This will only set the name,
       * value, path, domain name version of the <code>cookie</code>
       * because as of RFC 2109 these are the only attributes a
       * <code>Cookie</code> may have, the path and domain are
       * optional.
       *
       * @return an initialized <code>Cookie</code> object
       */
      public Cookie next(){
         if(!hasNext()) {
            return null;
         }
         parsed = false;
         return getCookie();
      }
      

      /**
       * Determine whether or not there are any <code>Cookie</code>s
       * left in the <code>String</code>. This will attempt to extract
       * another <code>Cookie</code> from the <code>String</code> and
       * cache the result so the <code>next</code> method will produce
       * this <code>Cookie</code>. If another <code>Cookie</code> cannot
       * be parsed from the remainder of the <code>String</code> then
       * this will return <code>false</code> otherwise it will return
       * <code>true</code>.
       *
       * @return true if there are more cookies false otherwise
       */
      public boolean hasNext(){
         if(finished) {
            return false;
         }
         if(parsed) {
            return true;
         }
         parse();
         
         if(name.len <=0){
            finished = true;
            return false;
         }
         return true;

      }
      
      /**
       * This method is used to remove items from the iterator. This
       * however performs no action as the act of parsing should not
       * modify the underlying source text value so that it can be 
       * reset with the <code>reset</code> method and used again.
       */
      public void remove() {
         return;
      }
   }
   
   /**
    * This is a token object that is used to store the offset and
    * length of a region of chars in the <code>CookieParser.buf</code>
    * array. The <code>toString</code> method of this token will
    * produce the <code>String</code> value of the region it
    * represents.
    */
   private class Token {

      /**
       * The numer of characters that were consumed by this token.
       */
      public int len;

      /**
       * The offset within the buffer that this token starts from.
       */
      public int off;

      /**
       * This converts region within the buffer to a <code>String</code>.
       * This converts the region only if there is a sufficient length.
       *
       * @return the <code>String</code> value of the region
       */
      public String toString(){
         return new String(buf,off,len);
      }
   }
}
