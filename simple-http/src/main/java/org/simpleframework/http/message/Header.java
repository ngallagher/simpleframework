/*
 * Header.java February 2001
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
import java.util.Locale;

import org.simpleframework.http.Address;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;

/**
 * This is a <code>Header</code> object that is used to represent a
 * basic form for the HTTP request message. This is used to extract
 * values such as the request line and header values from the request
 * message. Access to header values is done case insensitively.
 * <p>
 * As well as providing the header values and request line values 
 * this will also provide convenience methods which enable the user
 * to determine the length of the body this message header prefixes.
 *
 * @author Niall Gallagher
 */ 
public interface Header extends Segment {
   
   /**
    * This can be used to get the target specified for this HTTP
    * request. This corresponds to the URI sent in the request 
    * line. Typically this will be the path part of the URI, but
    * can be the full URI if the request is a proxy request.
    *
    * @return the target URI that this HTTP request specifies
    */
   String getTarget();   
   
   /**
    * This method returns a <code>CharSequence</code> holding the data
    * consumed for the request. A character sequence is returned as it
    * can provide a much more efficient means of representing the header 
    * data by just wrapping the consumed byte array.
    * 
    * @return this returns the characters consumed for the header
    */
   CharSequence getHeader();   
   
   /**
    * This is used to acquire the address from the request line.
    * An address is the full URI including the scheme, domain,
    * port and the query parts. This allows various parameters
    * to be acquired without having to parse the target.
    * 
    * @return this returns the address of the request line
    */
   Address getAddress();
   
   /**
    * This is used to acquire the path as extracted from the
    * the HTTP request URI. The <code>Path</code> object that is
    * provided by this method is immutable, it represents the
    * normalized path only part from the request URI.
    * 
    * @return this returns the normalized path for the request
    */   
   Path getPath();
   
   /**
    * This method is used to acquire the query part from the
    * HTTP request URI target. This will return only the values
    * that have been extracted from the request URI target.
    * 
    * @return the query associated with the HTTP target URI
    */
   Query getQuery();
   
   /**
    * This can be used to get the HTTP method for this request. The
    * HTTP specification RFC 2616 specifies the HTTP request methods
    * in section 9, Method Definitions. Typically this will be a 
    * GET or POST method, but can be any valid alphabetic token.
    *
    * @return the HTTP method that this request has specified
    */   
   String getMethod();

   /**
    * This can be used to get the major number from a HTTP version.
    * The major version corresponds to the major protocol type, that
    * is the 1 of a HTTP/1.1 version string. Typically the major 
    * type is 1, by can be 0 for HTTP/0.9 clients.
    *
    * @return the major version number for the HTTP message
    */
   int getMajor();

   /**
    * This can be used to get the minor number from a HTTP version. 
    * The minor version corresponds to the minor protocol type, that
    * is the 0 of a HTTP/1.0 version string. This number is typically
    * used to determine whether persistent connections are supported.
    *
    * @return the minor version number for the HTTP message
    */
   int getMinor();
   
   /**
    * This method is used to get a <code>List</code> of the names
    * for the headers. This will provide the original names for the
    * HTTP headers for the message. Modifications to the provided
    * list will not affect the header, the list is a simple copy.
    *
    * @return this returns a list of the names within the header
    */
   List<String> getNames();
  
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
   int getInteger(String name);
  
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
   long getDate(String name);

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
   Cookie getCookie(String name);

   /**
    * This is used to acquire all cookies that were sent in the header.    
    * If any cookies exists within the HTTP header they are returned
    * as <code>Cookie</code> objects. Otherwise this method will an
    * empty list. Each cookie object will contain the name, value and 
    * path of the cookie as well as the optional domain part.
    * 
    * @return this returns all cookie objects from the HTTP header
    */ 
   List<Cookie> getCookies();
   
   /**
    * This is used to acquire the locales from the request header. The
    * locales are provided in the <code>Accept-Language</code> header.
    * This provides an indication as to the languages that the client 
    * accepts. It provides the locales in preference order.
    * 
    * @return this returns the locales preferred by the client
    */
   List<Locale> getLocales();  
   
   /**
    * This is used to determine if the header represents one that
    * requires the HTTP/1.1 continue expectation. If the request
    * does require this expectation then it should be send the
    * 100 status code which prompts delivery of the message body.
    * 
    * @return this returns true if a continue expectation exists
    */
   boolean isExpectContinue();
   
   /**
    * This method returns a string representing the header that was
    * consumed by this consumer. For performance reasons it is better
    * to acquire the character sequence representing the header as it
    * does not require the allocation on new memory.
    * 
    * @return this returns a string representation of this request
    */
   String toString();
}


