/*
 * Message.java February 2007
 *
 * Copyright (C) 2007, Niall Gallagher <niallg@users.sf.net>
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

import org.simpleframework.http.Cookie;

/**
 * The <code>Message</code> object is used to store an retrieve the
 * headers for both a request and response. Headers are stored and
 * retrieved in a case insensitive manner according to RFC 2616. 
 * The message also allows multiple header values to be added to a
 * single header name, headers such as Cookie and Set-Cookie can be 
 * added multiple times with different values.
 * 
 * @author Niall Gallagher
 */
public interface Message {

   /**
    * This is used to acquire the names of the of the headers that
    * have been set in the response. This can be used to acquire all
    * header values by name that have been set within the response.
    * If no headers have been set this will return an empty list.
    * 
    * @return a list of strings representing the set header names
    */
   List<String> getNames();

   /**
    * This can be used to set a HTTP message header to this object.
    * The name and value of the HTTP message header will be used to
    * create a HTTP message header object which can be retrieved using
    * the <code>getValue</code> in combination with the get methods.
    * This will perform a <code>remove</code> using the issued header
    * name before the header value is set.       
    *
    * @param name the name of the HTTP message header to be added
    * @param value the value the HTTP message header will have
    */
   void setValue(String name, String value);

   /**
    * This can be used to set a HTTP message header to this object.
    * The name and value of the HTTP message header will be used to
    * create a HTTP message header object which can be retrieved using
    * the <code>getValue</code> in combination with the get methods.
    * This will perform a <code>remove</code> using the issued header
    * name before the header value is set.       
    *
    * @param name the name of the HTTP message header to be added
    * @param value the value the HTTP message header will have
    */
   void setInteger(String name, int value);

   /**
    * This is used as a convenience method for adding a header that
    * needs to be parsed into a HTTP date string. This will convert
    * the date given into a date string defined in RFC 2616 sec 3.3.1.
    * This will perform a <code>remove</code> using the issued header
    * name before the header value is set.       
    *
    * @param name the name of the HTTP message header to be added
    * @param date the value constructed as an RFC 1123 date string
    */
   void setDate(String name, long date);

   /**
    * This can be used to add a HTTP message header to this object.
    * The name and value of the HTTP message header will be used to
    * create a HTTP message header object which can be retrieved using
    * the <code>getValue</code> in combination with the get methods.
    *
    * @param name the name of the HTTP message header to be added
    * @param value the value the HTTP message header will have
    */
   void addValue(String name, String value);

   /**
    * This can be used to add a HTTP message header to this object.
    * The name and value of the HTTP message header will be used to
    * create a HTTP message header object which can be retrieved using
    * the <code>getInteger</code> in combination with the get methods.
    *
    * @param name the name of the HTTP message header to be added
    * @param value the value the HTTP message header will have
    */
   void addInteger(String name, int value);

   /**
    * This is used as a convenience method for adding a header that
    * needs to be parsed into a HTTPdate string. This will convert
    * the date given into a date string defined in RFC 2616 sec 3.3.1.
    *
    * @param name the name of the HTTP message header to be added
    * @param date the value constructed as an RFC 1123 date string
    */
   void addDate(String name, long date);

   /**
    * This can be used to get the value of the first message header
    * that has the specified name. This will return the full string
    * representing the named header value. If the named header does
    * not exist then this will return a null value.
    *
    * @param name the HTTP message header to get the value from
    *
    * @return this returns the value that the HTTP message header
    */
   String getValue(String name);
   
   /**
    * This can be used to get the value of the first message header
    * that has the specified name. This will return the full string
    * representing the named header value. If the named header does
    * not exist then this will return a null value.
    *
    * @param name the HTTP message header to get the value from
    * @param index gets the value at the index if there are multiple
    *
    * @return this returns the value that the HTTP message header
    */
   String getValue(String name, int index);

   /**
    * This can be used to get the value of the first message header
    * that has the specified name. This will return the integer
    * representing the named header value. If the named header does
    * not exist then this will return a value of minus one, -1.
    *
    * @param name the HTTP message header to get the value from
    *
    * @return this returns the value that the HTTP message header
    */
   int getInteger(String name);

   /**
    * This can be used to get the value of the first message header
    * that has the specified name. This will return the long value
    * representing the named header value. If the named header does
    * not exist then this will return a value of minus one, -1.
    *
    * @param name the HTTP message header to get the value from
    *
    * @return this returns the value that the HTTP message header
    */
   long getDate(String name);

   /**
    * This returns the <code>Cookie</code> object stored under the
    * specified name. This is used to retrieve cookies that have been 
    * set with the <code>setCookie</code> methods. If the cookie does
    * not exist under the specified name this will return null. 
    *
    * @param name this is the name of the cookie to be retrieved
    * 
    * @return returns the <code>Cookie</code> by the given name
    */
   Cookie getCookie(String name);

   /**
    * This returns all <code>Cookie</code> objects stored under the
    * specified name. This is used to retrieve cookies that have been 
    * set with the <code>setCookie</code> methods. If there are no
    * cookies then this will return an empty list. 
    * 
    * @return returns all the <code>Cookie</code> in the response
    */
   List<Cookie> getCookies();

   /**
    * The <code>setCookie</code> method is used to set a cookie value 
    * with the cookie name. This will add a cookie to the response
    * stored under the name of the cookie, when this is committed it 
    * will be added as a Set-Cookie header to the resulting response.
    * This is a convenience method that avoids cookie creation.     
    *
    * @param name this is the cookie to be added to the response
    * @param value this is the cookie value that is to be used
    * 
    * @return returns the cookie that has been set in the response
    */
   Cookie setCookie(String name, String value);

   /**
    * The <code>setCookie</code> method is used to set a cookie value 
    * with the cookie name. This will add a cookie to the response
    * stored under the name of the cookie, when this is committed it 
    * will be added as a Set-Cookie header to the resulting response.
    *
    * @param cookie this is the cookie to be added to the response
    * 
    * @return returns the cookie that has been set in the response
    */
   Cookie setCookie(Cookie cookie);

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
    * @return ordered list of tokens extracted from the header(s)
    */
   List<String> getValues(String name);

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
    * @param list this is the list of individual header values
    *
    * @return ordered list of tokens extracted from the header(s)
    */
   List<String> getValues(List<String> list);

   /**
    * This is used to acquire all the individual header values from
    * the message. The header values provided by this are unparsed
    * and represent the actual string values that have been added to
    * the message keyed by a given header name.
    * 
    * @param name the name of the header to get the values for
    * 
    * @return this returns a list of the values for the header name
    */
   List<String> getAll(String name);
}
