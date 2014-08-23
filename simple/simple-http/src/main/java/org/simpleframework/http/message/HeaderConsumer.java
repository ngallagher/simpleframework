/*
 * HeaderConsumer.java February 2007
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

import org.simpleframework.http.Cookie;

/**
 * The <code>HeaderConsumer</code> object is used to consume a HTTP
 * header from the cursor. This extends the segment consumer with
 * methods specific to the header. Also this enables session cookies
 * to be created using the cookies extracted from the header.
 * 
 * @author Niall Gallagher
 */
public abstract class HeaderConsumer extends SegmentConsumer implements Header {

   /**
    * Constructor for the <code>HeaderConsumer</code> object. This 
    * is used to create a consumer capable of reading a header from
    * a provided cursor. All methods of the <code>Header</coder> 
    * interface are implemented in this object.
    */
   protected HeaderConsumer() {
      super();     
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
}
