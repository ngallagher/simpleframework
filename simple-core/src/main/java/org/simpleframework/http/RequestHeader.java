/*
 * RequestHeader.java February 2001
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

package org.simpleframework.http;

import java.util.List;
import java.util.Locale;

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
public interface RequestHeader extends RequestLine {
   
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
    * This is used to acquire a cookie using the name of that cookie.
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
   String getValue(String name);
   
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
   String getValue(String name, int index);
   
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
   List<String> getValues(String name);
   
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
    * This is a convenience method that can be used to determine the 
    * content type of the message body. This will determine whether
    * there is a <code>Content-Type</code> header, if there is then
    * this will parse that header and represent it as a typed object
    * which will expose the various parts of the HTTP header.
    *
    * @return this returns the content type value if it exists
    */   
   ContentType getContentType();  
   
   /**
    * This is a convenience method that can be used to determine
    * the length of the message body. This will determine if there
    * is a <code>Content-Length</code> header, if it does then the
    * length can be determined, if not then this returns -1.
    *
    * @return the content length, or -1 if it cannot be determined
    */
   long getContentLength();  
   
   /**
    * This method returns a <code>CharSequence</code> holding the header
    * consumed for the request. A character sequence is returned as it
    * can provide a much more efficient means of representing the header 
    * data by just wrapping the consumed byte array.
    * 
    * @return this returns the characters consumed for the header
    */
   CharSequence getHeader();
   
   /**
    * This method returns a string representing the header that was
    * consumed for this request. For performance reasons it is better
    * to acquire the character sequence representing the header as it
    * does not require the allocation on new memory.
    * 
    * @return this returns a string representation of this request
    */
   String toString();
}
