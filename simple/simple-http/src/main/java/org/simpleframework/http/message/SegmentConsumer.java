/*
 * SegmentConsumer.java February 2007
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

import static org.simpleframework.http.Protocol.ACCEPT_LANGUAGE;
import static org.simpleframework.http.Protocol.CONTENT_DISPOSITION;
import static org.simpleframework.http.Protocol.CONTENT_LENGTH;
import static org.simpleframework.http.Protocol.CONTENT_TYPE;
import static org.simpleframework.http.Protocol.COOKIE;
import static org.simpleframework.http.Protocol.EXPECT;
import static org.simpleframework.http.Protocol.TRANSFER_ENCODING;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.simpleframework.http.ContentDisposition;
import org.simpleframework.http.ContentType;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.parse.ContentDispositionParser;
import org.simpleframework.http.parse.ContentTypeParser;
import org.simpleframework.http.parse.CookieParser;
import org.simpleframework.http.parse.LanguageParser;

/**
 * The <code>SegmentConsumer</code> object provides a consumer that is
 * used to consume a HTTP header. This will read all headers within a
 * HTTP header message until the carriage return line feed empty line
 * is encountered. Once all headers are consumed they are available 
 * using the case insensitive header name. This will remove leading
 * and trailing whitespace from the names and values parsed.
 * 
 * @author Niall Gallagher
 */
public class SegmentConsumer extends ArrayConsumer implements Segment {
     
   /**
    * This is the terminal carriage return and line feed end line.
    */
   private static final byte[]  TERMINAL = { 13, 10, 13, 10 };
   
   /**
    * This is used to represent the content disposition header.
    */
   protected ContentDisposition disposition;
   
   /**
    * This is used to parse the languages accepted in the request.
    */
   protected LanguageParser language;
   
   /**
    * This is used to parse the cookie headers that are consumed.
    */
   protected CookieParser cookies;
   
   /**
    * This is used to store all consumed headers by the header name.
    */
   protected MessageHeader header;
   
   /**
    * This is used to parse the content type header consumed.
    */
   protected ContentType type;
   
   /**
    * This represents the transfer encoding value of the body.
    */
   protected String encoding;
   
   /**
    * During parsing this is used to store the parsed header name,
    */
   protected String name;   
   
   /**
    * During parsing this is used to store the parsed header value.
    */
   protected String value;
   
   /**
    * This is used to determine if there is a continue expected.
    */
   protected boolean expect;
   
   /**
    * Represents the length of the body from the content length.
    */
   protected long length;
   
   /**
    * This represents the length limit of the HTTP header cosumed.
    */
   protected long limit;
   
   /**
    * This is used to track the read offset within the header.
    */
   protected int pos;
   
   /**
    * This is used to track how much of the terminal is read.
    */
   protected int scan;
   
   /**
    * Constructor for the <code>SegmentConsumer</code> object. This
    * is used to create a segment consumer used to consume and parse
    * a HTTP message header. This delegates parsing of headers if
    * they represent special headers, like content type or cookies.
    */
   public SegmentConsumer() {
      this(1048576);
   }   
   
   /**
    * Constructor for the <code>SegmentConsumer</code> object. This
    * is used to create a segment consumer used to consume and parse
    * a HTTP message header. This delegates parsing of headers if
    * they represent special headers, like content type or cookies.
    * 
    * @param limit this is the length limit for a HTTP header
    */
   public SegmentConsumer(int limit) {
      this.language = new LanguageParser();
      this.cookies = new CookieParser();
      this.header = new MessageHeader();
      this.limit = limit;
      this.length = -1;
   }   
   
   /**
    * This method is used to determine the type of a part. Typically
    * a part is either a text parameter or a file. If this is true
    * then the content represented by the associated part is a file.
    *
    * @return this returns true if the associated part is a file
    */   
   public boolean isFile() {
      if(disposition == null) {
         return false;
      }
      return disposition.isFile();
   }
   
   /**
    * This method is used to acquire the name of the part. Typically
    * this is used when the part represents a text parameter rather
    * than a file. However, this can also be used with a file part.
    * 
    * @return this returns the name of the associated part
    */   
   public String getName() {
      if(disposition == null) {
         return null;
      }
      return disposition.getName();      
   }
   
   /**
    * This method is used to acquire the file name of the part. This
    * is used when the part represents a text parameter rather than 
    * a file. However, this can also be used with a file part.
    *
    * @return this returns the file name of the associated part
    */   
   public String getFileName() {
      if(disposition == null) {
         return null;
      }
      return disposition.getFileName();
   }
   
   /**
    * This is a convenience method that can be used to determine the 
    * content type of the message body. This will determine whether
    * there is a <code>Content-Type</code> header, if there is then
    * this will parse that header and represent it as a typed object
    * which will expose the various parts of the HTTP header.
    *
    * @return this returns the content type value if it exists
    */      
   public ContentType getContentType() {
      return type;
   }
   
   /**
    * This is a convenience method that can be used to determine
    * the length of the message body. This will determine if there
    * is a <code>Content-Length</code> header, if it does then the
    * length can be determined, if not then this returns -1.
    *
    * @return the content length, or -1 if it cannot be determined
    */   
   public long getContentLength() {
      return length;
   }
   
   /**
    * This is a convenience method that can be used to determine the 
    * content type of the message body. This will determine whether
    * there is a <code>Transfer-Encoding</code> header, if there is 
    * then this will parse that header and return the first token in
    * the comma separated list of values, which is the primary value.
    *
    * @return this returns the transfer encoding value if it exists
    */    
   public String getTransferEncoding() {
      return encoding;      
   }
   
   /**
    * This is a convenience method that can be used to determine the 
    * content type of the message body. This will determine whether
    * there is a <code>Content-Disposition</code> header, if there is 
    * this will parse that header and represent it as a typed object
    * which will expose the various parts of the HTTP header.
    *
    * @return this returns the content disposition value if it exists
    */    
   public ContentDisposition getDisposition() {
      return disposition;
   }
   
   /**
    * This is used to acquire the locales from the request header. The
    * locales are provided in the <code>Accept-Language</code> header.
    * This provides an indication as to the languages that the client 
    * accepts. It provides the locales in preference order.
    * 
    * @return this returns the locales preferred by the client
    */
   public List<Locale> getLocales() {
      if(language != null) {
         return language.list();
      }
      return Collections.emptyList();           
   }
   
   /**
    * This can be used to get the values of HTTP message headers
    * that have the specified name. This is a convenience method that 
    * will present that values as tokens extracted from the header.
    * This has obvious performance benefits as it avoids having to 
    * deal with <code>substring</code> and <code>trim</code> calls.
    * <p>
    * The tokens returned by this method are ordered according to
    * there HTTP quality values, or "q" values, see RFC 2616 section
    * 3.9. This also strips out the quality parameter from tokens
    * returned. So "image/html; q=0.9" results in "image/html". If
    * there are no "q" values present then order is by appearance.
    * <p> 
    * The result from this is either the trimmed header value, that
    * is, the header value with no leading or trailing whitespace
    * or an array of trimmed tokens ordered with the most preferred
    * in the lower indexes, so index 0 is has highest preference.
    *
    * @param name the name of the headers that are to be retrieved
    *
    * @return ordered array of tokens extracted from the header(s)
    */   
   public List<String> getValues(String name) {
      return header.getValues(name);
   }
   
   /**
    * This can be used to get the value of the first message header
    * that has the specified name. The value provided from this will
    * be trimmed so there is no need to modify the value, also if 
    * the header name specified refers to a comma separated list of
    * values the value returned is the first value in that list.  
    * This returns null if theres no HTTP message header.
    *
    * @param name the HTTP message header to get the value from
    *
    * @return this returns the value that the HTTP message header
    */    
   public String getValue(String name) {
      return header.getValue(name);
   }
   
   /**
    * This can be used to get the value of the first message header
    * that has the specified name. The value provided from this will
    * be trimmed so there is no need to modify the value, also if 
    * the header name specified refers to a comma separated list of
    * values the value returned is the first value in that list.  
    * This returns null if there is no HTTP message header.
    *
    * @param name the HTTP message header to get the value from
    * @param index acquires a specific header value from multiple
    *
    * @return this returns the value that the HTTP message header
    */   
   public String getValue(String name, int index) {
      return header.getValue(name, index);
   }
  
   /**
    * This is used to determine if the header represents one that
    * requires the HTTP/1.1 continue expectation. If the request
    * does require this expectation then it should be send the
    * 100 status code which prompts delivery of the message body.
    * 
    * @return this returns true if a continue expectation exists
    */
   public boolean isExpectContinue() {
      return expect;
   }
   
   /**
    * This method is used to add an additional chunk size to the 
    * internal array. Resizing of the internal array is required as
    * the consumed bytes may exceed the initial size of the array.
    * In such a scenario the array is expanded the chunk size.
    *
    * @param size this is the minimum size to expand the array to 
    */ 
   @Override
   protected void resize(int size) throws IOException {
      if(size > limit) {
         throw new IOException("Header has exceeded maximum size");
      }
      super.resize(size);
   }
   
   /**
    * This is used to process the headers when the terminal token
    * has been fully read from the consumed bytes. Processing will
    * extract all headers from the HTTP header message and further
    * parse those values if required.
    */
   @Override
   protected void process() throws IOException {
      headers();
   }
   
   /**
    * This is used to parse the headers from the consumed HTTP header
    * and add them to the segment. Once added they are available via
    * the header name in a case insensitive manner. If the header has
    * a special value, that is, if further information is required it
    * will be extracted and exposed in the segment interface.
    */
   protected void headers() {
      while(pos < count) {
         header();         
         add(name, value);        
      }
   }   

   /**
    * This is used to parse a header from the consumed HTTP message
    * and add them to the segment. Once added it is available via
    * the header name in a case insensitive manner. If the header has
    * a special value, that is, if further information is required it
    * will be extracted and exposed in the segment interface.
    */   
   private void header() {
      adjust();
      name();
      adjust();
      value();    
      end();
   }
    
   /**
    * This is used to add the name and value specified as a special
    * header within the segment. Special headers are those where 
    * there are values of interest to the segment. For instance the
    * Content-Length, Content-Type, and Cookie headers are parsed
    * using an external parser to extract the values.      
    * 
    * @param name this is the name of the header to be added
    * @param value this is the value of the header to be added
    */
   protected void add(String name, String value) {
      if(equal(ACCEPT_LANGUAGE, name)) {
         language(value);
      }else if(equal(CONTENT_LENGTH, name)) {
         length(value);
      } else if(equal(CONTENT_TYPE, name)) {
         type(value);
      } else if(equal(CONTENT_DISPOSITION, name)) {
         disposition(value);
      } else if(equal(TRANSFER_ENCODING, name)) {
         encoding(value);
      } else if(equal(EXPECT, name)) {
         expect(value);
      } else if(equal(COOKIE, name)) {
         cookie(value);
      } 
      header.addValue(name, value);
   }   
   
   /**
    * This is used to determine if the expect continue header is
    * present and thus there is a requirement to send the continue
    * status before the client sends the request body. This will
    * basically assume the expectation is always continue.
    * 
    * @param value the value in the expect continue header
    */
   protected void expect(String value) {
      expect = true;
   }
   
   /**
    * This will accept any cookie header and parse it such that all
    * cookies within it are converted to <code>Cookie</code> objects
    * and made available as typed objects. If the value can not be
    * parsed this will not add the cookie value.
    * 
    * @param value this is the value of the cookie to be parsed
    */
   protected void cookie(String value) {
      cookies.parse(value);
         
      for(Cookie cookie : cookies) {
         header.setCookie(cookie);
      }      
   } 
   
   /**
    * This is used to parse the <code>Accept-Language</code> header
    * value. This allows the locales the client is interested in to 
    * be provided in preference order and allows the client do alter
    * and response based on the locale the client has provided.
    * 
    * @param value this is the value that is to be parsed
    */
   protected void language(String value) {
      language = new LanguageParser(value);
   }
   
   /**
    * This is used to parse the content type header header so that 
    * the MIME type is available to the segment. This provides an
    * instance of the  <code>ContentType</code> object to represent
    * the content type header, which exposes the charset value.
    * 
    * @param value this is the content type value to parse
    */
   protected void type(String value) {
      type = new ContentTypeParser(value);
   }
   
   /**
    * This is used to parse the content disposition header header so 
    * that the MIME type is available to the segment. This provides 
    * an instance of the  <code>Disposition<code> object to represent
    * the content disposition, this exposes the upload type.
    * 
    * @param value this is the content type value to parse
    */   
   protected void disposition(String value) {
      disposition = new ContentDispositionParser(value);
   }
   
   /**
    * This is used to store the transfer encoding header value. This
    * is used to determine the encoding of the body this segment 
    * represents. Typically this will be the chunked encoding.
    * 
    * @param value this is the value representing the encoding
    */
   protected void encoding(String value) {
      encoding = value;
   }
   
   /**
    * This is used to parse a provided header value for the content
    * length. If the string provided is not an integer value this will
    * throw a number format exception, by default length is -1.
    * 
    * @param value this is the header value of the content length
    */
   protected void length(String value) {
      try {
         length = Long.parseLong(value);
      }catch(Exception e) {
         length = -1;
      }
   }
   
   /**
    * This updates the token for the header name. The name is parsed
    * according to the presence of a colon ':'. Once a colon character
    * is encountered then this header name is considered to be read
    * from the buffer and is used to key the value after the colon.
    */ 
   private void name() {
      Token token = new Token(pos, 0);
      
      while(pos < count){
         if(array[pos] == ':') {
            pos++;
            break;
         }
         token.size++;
         pos++;
      }
      name = token.text();
   }

    
   /**
    * This is used to parse the HTTP header value. This will parse it
    * in such a way that the line can be folded over several lines 
    * see RFC 2616 for the syntax of a folded line. The folded line 
    * is basically a way to wrap a single  HTTP header into several 
    * lines using a tab at the start of the following line to indicate
    *  that the header flows onto the next line.
    */    
   private void value() {   
      Token token = new Token(pos, 0);
      
      scan: for(int mark = 0; pos < count;){
         if(terminal(array[pos])) {  /* CR  or  LF */
            for(int i = 0; pos < count; i++){
               if(array[pos++] == 10) { /* skip the LF */  
                  if(pos < array.length) {
                     if(space(array[pos])) {
                        mark += i + 1;  /* account for bytes examined */
                        break; /* folding line */
                     }                
                  }
                  break scan; /* not a folding line */
               }             
            }       
         } else {
            if(!space(array[pos])){
               token.size = ++mark;
            } else {
               mark++;
            }
            pos++;               
         }
      }     
      value = token.text();
   }

   /**
    * This will update the offset variable so that the next read will
    * be of a non whitespace character. According to RFC 2616 a white 
    * space character is a tab or a space. This will remove multiple
    * occurrences of whitespace characters until an non-whitespace 
    * character is encountered.
    */  
   protected void adjust() {
      while(pos < count) {         
         if(!space(array[pos])){
            break;
         }         
         pos++;
      }
   }
   
   /**
    * This will update the offset variable so that the next read will
    * be a non whitespace character or terminal character. According to 
    * RFC 2616 a white space character is a tab or a space. This will 
    * remove multiple occurrences of whitespace characters until an 
    * non-whitespace character or a non-terminal is encountered. This 
    * is basically used to follow through to the end of a header line.
    */ 
   protected void end() {
      while(pos < count) {         
         if(!white(array[pos])){
            break;
         }         
         pos++;
      }
   }
   
   /**
    * This method is used to scan for the terminal token. It searches
    * for the token and returns the number of bytes in the buffer 
    * after the terminal token. Returning the excess bytes allows the
    * consumer to reset the bytes within the consumer object.
    *
    * @return this returns the number of excess bytes consumed
    */ 
   @Override
   protected int scan() {
      int length = count;
      
      while(pos < count) {
         if(array[pos++] != TERMINAL[scan++]) {
            scan = 0;
         } 
         if(scan == TERMINAL.length) {            
            done = true;
            count = pos;
            pos = 0;
            return length - count;
         }         
      }  
      return 0;
   }
   
   /**
    * This is used to determine if two header names are equal, this is
    * done to ensure that the case insensitivity of HTTP header names
    * is observed. Special headers are processed using this consumer
    * and this is used to ensure the correct header is always matched. 
    * 
    * @param name this is the name to compare the parsed token with
    * @param token this is the header name token to examine
    * 
    * @return true of the header name token is equal to the name
    */
   protected boolean equal(String name, String token) {
      return name.equalsIgnoreCase(token);
   }
   
   /**
    * This identifies a given ISO-8859-1 byte as a space character. A
    * space is either a space or a tab character in ISO-8859-1.
    *
    * @param octet the byte to determine whether it is a space
    *
    * @return true if it is a space character, false otherwise
    */    
   protected boolean space(byte octet) {
      return octet == ' ' || octet == '\t';
   }

   /**
    * This determines if an ISO-8859-1 byte is a terminal character. A
    * terminal character is a carriage return or a line feed character.
    *
    * @param octet the byte to determine whether it is a terminal
    *
    * @return true if it is a terminal character, false otherwise
    */    
   protected boolean terminal(byte octet){
      return octet == 13 || octet == 10;
   }
   
   
   /**
    * This is used to determine if a given ISO-8859-1 byte is a white
    * space character, such as a tab or space or a terminal character, 
    * such as a carriage return or a new line. If it is, this will
    * return true otherwise it returns false.
    *
    * @param octet this is to be checked to see if it is a space
    *
    * @return true if the byte is a space character, false otherwise
    */    
   protected boolean white(byte octet) {
      switch(octet) {
      case ' ': case '\r':
      case '\n': case '\t':
         return true;
      default:
         return false;
      }      
   }

   /**
    * This is used to provide a string representation of the header
    * read. Providing a string representation of the header is used
    * so that on debugging the contents of the delivered header can
    * be inspected in order to determine a cause of error.
    *
    * @return this returns a string representation of the header
    */ 
   @Override
   public String toString() {
      return new String(array, 0, count);
   }

   /**
    * This is used to track the boundaries of a token so that it can
    * be converted in to a usable string. This will track the length
    * and offset within the consumed array of the token. When the
    * token is to be used it can be converted in to a string.    
    */
   private class Token {
      
      /**
       * This is used to track the number of bytes within the array.
       */
      public int size;
      
      /**
       * This is used to mark the start offset within the array.
       */
      public int off;
      
      /**
       * Constructor for the <code>Token</code> object. This is used
       * to create a new token to track the range of bytes that will
       * be used to create a string representing the parsed value.
       * 
       * @param off the starting offset for the token range
       * @param size the number of bytes used for the token
       */
      public Token(int off, int size) {
         this.off = off;
         this.size = size;
      }
      
      /**
       * This is used to convert the byte range to a string. This 
       * will use UTF-8 encoding for the string which is compatible
       * with the HTTP default header encoding of ISO-8859-1.
       * 
       * @return the encoded string representing the token 
       */
      public String text() {
         return text("UTF-8");
      }
      
      /**
       * This is used to convert the byte range to a string. This 
       * will use specified encoding, if that encoding is not
       * supported then this will return null for the token value.
       * 
       * @return the encoded string representing the token 
       */      
      public String text(String charset) {
         try {
            return new String(array, off, size, charset);
         } catch(IOException e) {
            return null;
         }        
      }
   }
}
