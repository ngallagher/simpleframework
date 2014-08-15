/*
 * RequestLine.java February 2001
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

/**
 * The <code>RequestLine</code> is used to represent a HTTP request
 * line. The  methods provided for this can be used to provide easy 
 * access to the components of a HTTP request line. For the syntax 
 * of a HTTP request line see RFC 2616.
 *
 * @author Niall Gallagher
 */ 
public interface RequestLine {

   /**
    * This can be used to get the HTTP method for this request. The
    * HTTP specification RFC 2616 specifies the HTTP request methods
    * in section 9, Method Definitions. Typically this will be a
    * GET, POST or a HEAD method, although any string is possible.
    *
    * @return the request method for this request message
    */ 
   String getMethod();

   /**
    * This can be used to get the URI specified for this HTTP
    * request. This corresponds to the /index part of a 
    * http://www.domain.com/index URL but may contain the full
    * URL. This is a read only value for the request.
    *
    * @return the URI that this HTTP request is targeting
    */ 
   String getTarget();
   
   /**
    * This is used to acquire the address from the request line.
    * An address is the full URI including the scheme, domain, port
    * and the query parts. This allows various parameters to be 
    * acquired without having to parse the raw request target URI.
    * 
    * @return this returns the address of the request line
    */
   Address getAddress();
   
   /**
    * This is used to acquire the path as extracted from the HTTP 
    * request URI. The <code>Path</code> object that is provided by
    * this method is immutable, it represents the normalized path 
    * only part from the request uniform resource identifier.
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
    * This can be used to get the major number from a HTTP version.
    * The major version corresponds to the major type that is the 1
    * of a HTTP/1.0 version string. 
    *
    * @return the major version number for the request message
    */ 
   int getMajor();

   /**
    * This can be used to get the major number from a HTTP version.
    * The major version corresponds to the major type that is the 0
    * of a HTTP/1.0 version string. This is used to determine if 
    * the request message has keep alive semantics.
    *
    * @return the major version number for the request message
    */ 
   int getMinor();
}
