/*
 * RequestConsumer.java February 2001
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
 
package org.simpleframework.http.message;

import java.util.List;

import org.simpleframework.http.Address;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;
import org.simpleframework.http.parse.AddressParser;

/**
 * The <code>RequestConsumer</code> object is used to parse the HTTP
 * request line followed by the HTTP message headers. This parses the
 * request URI such that the query parameters and path are extracted
 * and normalized. It performs this using external parsers, which
 * will remove and escaped characters and normalize the path segments.
 * Finally this exposes the HTTP version used using the major and
 * minor numbers sent with the HTTP request.
 *
 * @author Niall Gallagher
 */ 
public class RequestConsumer extends HeaderConsumer {
   
   /**
    * This is the address parser used to parse the request URI.
    */ 
   protected AddressParser parser;
   
   /**
    * This is the method token send with the HTTP request header.
    */ 
   protected String method;
   
   /**
    * This represents the raw request URI in an unparsed form.
    */ 
   protected String target;
   
   /**
    * This is the major version number of the HTTP request header.
    */ 
   protected int major;
   
   /**
    * This is the minor version number of the HTTP request header.
    */ 
   protected int minor;
  
   /**
    * Constructor for the <code>RequestConsumer</code> object. This 
    * is used to create a consumer which can consume a HTTP request
    * header and provide the consumed contents via a known interface.    
    * This also further breaks down the request URI for convenience.
    */ 
   public RequestConsumer() {
      super();
   }  
   
   /**
    * This can be used to get the target specified for this HTTP
    * request. This corresponds to the URI sent in the request 
    * line. Typically this will be the path part of the URI, but
    * can be the full URI if the request is a proxy request.
    *
    * @return the target URI that this HTTP request specifies
    */   
   public String getTarget() {
      return target;
   } 

   /**
    * This is used to acquire the address from the request line.
    * An address is the full URI including the scheme, domain,
    * port and the query parts. This allows various parameters
    * to be acquired without having to parse the target.
    * 
    * @return this returns the address of the request line
    */
   public Address getAddress() { 
      if(parser == null) {
         parser = new AddressParser(target);
      }
      return parser;
   }
   
   /**
    * This method is used to acquire the query part from the
    * HTTP request URI target. This will return only the values
    * that have been extracted from the request URI target.
    * 
    * @return the query associated with the HTTP target URI
    */   
   public Query getQuery() {
      return getAddress().getQuery();
   }

   /**
    * This is used to acquire the path as extracted from the
    * the HTTP request URI. The <code>Path</code> object that is
    * provided by this method is immutable, it represents the
    * normalized path only part from the request URI.
    * 
    * @return this returns the normalized path for the request
    */      
   public Path getPath() {
      return getAddress().getPath();
   }
   
   /**
    * This can be used to get the HTTP method for this request. The
    * HTTP specification RFC 2616 specifies the HTTP request methods
    * in section 9, Method Definitions. Typically this will be a 
    * GET or POST method, but can be any valid alphabetic token.
    *
    * @return the HTTP method that this request has specified
    */      
   public String getMethod() {
      return method;
   }
   
   /**
    * This can be used to get the major number from a HTTP version.
    * The major version corrosponds to the major protocol type, that
    * is the 1 of a HTTP/1.0 version string. Typically the major 
    * type is 1, by can be 0 for HTTP/0.9 clients.
    *
    * @return the major version number for the HTTP message
    */   
   public int getMajor() {
      return major;
   }
   
   /**
    * This can be used to get the minor number from a HTTP version. 
    * The minor version corrosponds to the minor protocol type, that
    * is the 0 of a HTTP/1.0 version string. This number is typically
    * used to determine whether persistent connections are supported.
    *
    * @return the minor version number for the HTTP message
    */   
   public int getMinor() {
      return minor;
   }    

   /**
    * This can be used to get the date of the first message header
    * that has the specified name. This is a convenience method that 
    * avoids having to deal with parsing the value of the requested
    * HTTP message header. This returns -1 if theres no HTTP header
    * value for the specified name.
    *
    * @param name the HTTP message header to get the value from
    *
    * @return this returns the date as a long from the header value 
    */      
   public long getDate(String name) {
      return header.getDate(name);
   }

   /**
    * This can be used to get the integer of the first message header
    * that has the specified name. This is a convenience method that 
    * avoids having to deal with parsing the value of the requested
    * HTTP message header. This returns -1 if theres no HTTP header
    * value for the specified name.
    *
    * @param name the HTTP message header to get the value from
    *
    * @return this returns the date as a long from the header value 
    */    
   public int getInteger(String name) {
      return header.getInteger(name);
   }

   /**
    * This method is used to get a <code>List</code> of the names
    * for the headers. This will provide the original names for the
    * HTTP headers for the message. Modifications to the provided
    * list will not affect the header, the list is a simple copy.
    *
    * @return this returns a list of the names within the header
    */   
   public List<String> getNames() {
      return header.getNames();
   }
  
   /**
    * This method is invoked after the terminal token has been read.
    * It is used to process the consumed data and is typically used to
    * parse the input such that it can be used by the subclass for
    * some useful puropse. This is called only once by the consumer.
    */    
   @Override
   protected void process() {
      method();
      target();
      version();
      end();
      headers();
   }  

   /**
    * This will parse URI target from the first line of the header
    * and store the parsed string internally. The target token is 
    * used to create an <code>Address</code> object which provides
    * all the details of the target including the query part.
    */ 
   private void target() {
      Token token = new Token(array, pos, 0);
      
      while(pos < count){
         if(white(array[pos])){
            pos++;
            break;
         }
         token.size++;
         pos++;
      }  
      target = token.toString();      
   }
   
   /**
    * This will parse HTTP method from the first line of the header
    * and store the parsed string internally. The method is used to
    * determine what action to take with the request, it also acts
    * as a means to determine the semantics of the request.
    */ 
   private void method() {
      Token token = new Token(array, pos, 0);

      while(pos < count){
         if(white(array[pos])){
            pos++;
            break;
         }
         token.size++;
         pos++;
      }       
      method = token.toString();
   }
   
   /**
    * This will parse HTTP version from the first line of the header
    * and store the parsed string internally. The method is used to
    * determine what version of HTTP is being used. Typically this
    * will be HTTP/1.1 however HTTP/1.0 must be supported and this
    * has different connection semantics with regards to pipelines.
    */ 
   protected void version() {
      pos += 5;   /* "HTTP/" */      
      major();  /* "1" */
      pos++;    /* "." */     
      minor();   /* "1" */
   }
    
   /**
    * This will parse the header from the current offset and convert
    * the bytes found into an int as it parses the digits it comes 
    * accross. This will cease to parse bytes when it encounters a 
    * non digit byte or the end of the readable bytes.
    */    
   private void major() {
      while(pos < count){
         if(!digit(array[pos])){            
            break;
         }
         major *= 10;
         major += array[pos];
         major -= '0';
         pos++;
      }        
   }

   /**
    * This will parse the header from the current offset and convert
    * the bytes found into an int as it parses the digits it comes 
    * accross. This will cease to parse bytes when it encounters a 
    * non digit byte or the end of the readable bytes.
    */     
   private void minor() {
      while(pos < count){
         if(!digit(array[pos])){            
            break;
         }
         minor *= 10;
         minor += array[pos];                  
         minor -= '0';
         pos++;
      }           
   } 

   /**
    * This is used to determine if a given ISO-8859-1 byte is a digit
    * character, between an ISO-8859-1 0 and 9. If it is, this will
    * return true otherwise it returns false.
    *
    * @param octet this is to be checked to see if it is a digit
    *
    * @return true if the byte is a digit character, false otherwise
    */ 
   protected boolean digit(byte octet) {
      return octet >= '0' && octet <= '9';
   }   
   
   /**
    * This method returns a <code>CharSequence</code> holding the data
    * consumed for the request. A character sequence is returned as it
    * can provide a much more efficient means of representing the header 
    * data by just wrapping the consumed byte array.
    * 
    * @return this returns the characters consumed for the header
    */
   public CharSequence getHeader() {
      return new Token(array, 0, count);
   } 
   
   /**
    * This is used to convert the byte range to a string. This 
    * will use UTF-8 encoding for the string which is compatible
    * with the HTTP default header encoding of ISO-8859-1.
    * 
    * @return the encoded string representing the token 
    */
   public String toString() {
      return getHeader().toString();
   }
   
   /**
    * This is a sequence of characters representing the header data
    * consumed. Here the internal byte buffer is simply wrapped so
    * that it can be a represented as a <code>CharSequence</code>. 
    * Wrapping the consumed array in this manner ensures that no
    * further memory allocation is required.
    */
   private static class Token implements CharSequence {
      
      /**
       * This is the array that contains the header bytes.
       */
      public byte[] array;
      
      /**
       * This is the number of bytes to use from the array.
       */
      public int size;
      
      /**
       * This is the offset in the array the token begins at.
       */
      public int off;
      
      /**
       * Constructor for the <code>ByteSequence</code> object. This
       * is used to represent the data that has been consumed by
       * the header. It acts as a light weight wrapper for the data
       * and avoids having to create new strings for each event.
       * 
       * @param array this is the array representing the header
       * @param off the starting offset for the token range
       * @param size the number of bytes used for the token
       */
      private Token(byte[] array, int off, int size) {
         this.array = array;
         this.size = size;
         this.off = off;
      }
      
      /**
       * This returns the length of the header in bytes. The length
       * includes the request line and all of the control characters
       * including the carriage return and line feed at the end of
       * the request header.
       * 
       * @return this returns the number of bytes for the header
       */
      public int length() {
         return size;
      }

      /**
       * This is used to acquire the character at the specified index.
       * Characters returned from this method are simply the bytes
       * casted to a character. This may not convert the character
       * correctly and a more sensible method should be used.
       * 
       * @param index the index to extract the character from
       * 
       * @return this returns the character found at the index
       */
      public char charAt(int index) {
         return (char) array[index];
      }

      /**
       * This returns a section of characters within the specified
       * range. Acquiring a section in this manner is simply done by
       * setting a start and end offset within the internal array.
       * 
       * @param start this is the start index to be used
       * @param end this is the end index to be used
       * 
       * @return this returns a new sequence within the original
       */
      public CharSequence subSequence(int start, int end) {
         return new Token(array, start, end - start);
      }
      
      /**
       * This is used to create a string from the header bytes. This
       * converts the header bytes to a string using a compatible
       * encoding. This may produce different results depending on
       * the time it is invoked, as the header consumes more data.
       * 
       * @return this returns an encoded version of the header
       */
      public String toString() {
         return toString("UTF-8");
      }
      
      /**
       * This is used to create a string from the header bytes. This
       * converts the header bytes to a string using a compatible
       * encoding. This may produce different results depending on
       * the time it is invoked, as the header consumes more data.
       * 
       * @param charset this is the encoding to use for the header
       * 
       * @return this returns an encoded version of the header
       */
      public String toString(String charset) {
         try {
            return new String(array, off, size, charset);
         } catch(Exception e) {
            return null;
         }
      }
   }
}


