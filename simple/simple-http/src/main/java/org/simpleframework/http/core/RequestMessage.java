/*
 * RequestMessage.java February 2001
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

package org.simpleframework.http.core;

import java.util.List;
import java.util.Locale;

import org.simpleframework.http.Address;
import org.simpleframework.http.ContentType;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;
import org.simpleframework.http.RequestHeader;
import org.simpleframework.http.message.Header;

/**
 * The <code>RequestMessage</code> object is used to create a HTTP
 * request header representation. All requests for details within a
 * request message delegates to an underlying header, which contains
 * all of the header names and values sent by the client. The header
 * names are case insensitively mapped as required by RFC 2616. 
 * 
 * @author Niall Gallagher
 */
class RequestMessage implements RequestHeader {
     
   /**
    * This is the underlying header used to house the headers.
    */
   protected Header header;
   
   /**
    * Constructor for the <code>RequestMessage</code> object. This 
    * is used to create a request message without an underlying
    * header. In such an event it is up to the subclass to provide 
    * the instance, this is useful for testing the request.
    */
   public RequestMessage() {
      super();
   }
   
   /**
    * Constructor for the <code>RequestMessage</code> object. This 
    * is used to create a request with a header instance. In such
    * a case the header provided will be queried for headers and is
    * used to store headers added to this message instance.
    * 
    * @param header this is the backing header for the message
    */
   public RequestMessage(Header header) {
      this.header = header;      
   }
   
   /**
    * This can be used to get the URI specified for this HTTP
    * request. This corresponds to the /index part of a 
    * http://www.domain.com/index URL but may contain the full
    * URL. This is a read only value for the request.
    *
    * @return the URI that this HTTP request is targeting
    */    
   public String getTarget() {
      return header.getTarget();
   }
   
   /**
    * This is used to acquire the address from the request line.
    * An address is the full URI including the scheme, domain, port
    * and the query parts. This allows various parameters to be 
    * acquired without having to parse the raw request target URI.
    * 
    * @return this returns the address of the request line
    */   
   public Address getAddress() {
      return header.getAddress();
   }
  
   /**
    * This is used to acquire the path as extracted from the HTTP 
    * request URI. The <code>Path</code> object that is provided by
    * this method is immutable, it represents the normalized path 
    * only part from the request uniform resource identifier.
    * 
    * @return this returns the normalized path for the request
    */   
   public Path getPath() {
      return header.getPath();
   }

   /**
    * This method is used to acquire the query part from the
    * HTTP request URI target. This will return only the values
    * that have been extracted from the request URI target.
    * 
    * @return the query associated with the HTTP target URI
    */   
   public Query getQuery() {
      return header.getQuery();
   }
   
   /**
    * This can be used to get the HTTP method for this request. The
    * HTTP specification RFC 2616 specifies the HTTP request methods
    * in section 9, Method Definitions. Typically this will be a
    * GET, POST or a HEAD method, although any string is possible.
    *
    * @return the request method for this request message
    */    
   public String getMethod() {
      return header.getMethod();     
   }

   /**
    * This can be used to get the major number from a HTTP version.
    * The major version corresponds to the major type that is the 1
    * of a HTTP/1.0 version string. 
    *
    * @return the major version number for the request message
    */    
   public int getMajor() {
      return header.getMajor();
   }
   
   /**
    * This can be used to get the major number from a HTTP version.
    * The major version corresponds to the major type that is the 0
    * of a HTTP/1.0 version string. This is used to determine if 
    * the request message has keep alive semantics.
    *
    * @return the major version number for the request message
    */    
   public int getMinor() {
      return header.getMinor();
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
    * This can be used to get the value of the first message header
    * that has the specified name. The value provided from this will
    * be trimmed so there is no need to modify the value, also if 
    * the header name specified refers to a comma seperated list of
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
    * This returns null if theres no HTTP message header.
    *
    * @param name the HTTP message header to get the value from
    * @param index if there are multiple values this selects one
    *
    * @return this returns the value that the HTTP message header
    */  
   public String getValue(String name, int index) {
      return header.getValue(name, index);
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
    * This can be used to get the values of HTTP message headers
    * that have the specified name. This is a convenience method that 
    * will present that values as tokens extracted from the header.
    * This has obvious performance benifits as it avoids having to 
    * deal with <code>substring</code> and <code>trim</code> calls.
    * <p>
    * The tokens returned by this method are ordered according to
    * there HTTP quality values, or "q" values, see RFC 2616 section
    * 3.9. This also strips out the quality parameter from tokens
    * returned. So "image/html; q=0.9" results in "image/html". If
    * there are no "q" values present then order is by appearence.
    * <p> 
    * The result from this is either the trimmed header value, that
    * is, the header value with no leading or trailing whitespace
    * or an array of trimmed tokens ordered with the most preferred
    * in the lower indexes, so index 0 is has higest preference.
    *
    * @param name the name of the headers that are to be retrieved
    *
    * @return ordered array of tokens extracted from the header(s)
    */   
   public List<String> getValues(String name) {
      return header.getValues(name);
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
      return header.getLocales();
   }
  
   /**
    * This is used to acquire a cookie usiing the name of that cookie.
    * If the cookie exists within the HTTP header then it is returned
    * as a <code>Cookie</code> object. Otherwise this method will
    * return null. Each cookie object will contain the name, value
    * and path of the cookie as well as the optional domain part.
    *
    * @param name this is the name of the cookie object to acquire
    * 
    * @return this returns a cookie object from the header or null
    */ 
   public Cookie getCookie(String name) {
      return header.getCookie(name);
   }
   
   /**
    * This is used to acquire all cookies that were sent in the header.    
    * If any cookies exists within the HTTP header they are returned
    * as <code>Cookie</code> objects. Otherwise this method will an
    * empty list. Each cookie object will contain the name, value and 
    * path of the cookie as well as the optional domain part.
    * 
    * @return this returns all cookie objects from the HTTP header
    */    
   public List<Cookie> getCookies() {
      return header.getCookies();
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
      return header.getContentType();
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
      return header.getContentLength();
   }   
   
   /**
    * This method returns a <code>CharSequence</code> holding the header
    * consumed for the request. A character sequence is returned as it
    * can provide a much more efficient means of representing the header 
    * data by just wrapping the consumed byte array.
    * 
    * @return this returns the characters consumed for the header
    */
   public CharSequence getHeader() {
      return header.getHeader();
   }
   
   /**
    * This is used to provide a string representation of the header
    * read. Providing a string representation of the header is used
    * so that on debugging the contents of the delivered header can
    * be inspected in order to determine a cause of error.
    *
    * @return this returns a string representation of the header
    */    
   public String toString() {   
      return header.toString();
   }
}
