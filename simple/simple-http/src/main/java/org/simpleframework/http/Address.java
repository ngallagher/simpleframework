/*
 * Address.java February 2001
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

import org.simpleframework.common.KeyMap;

/** 
 * The <code>Address</code> interface is used to represent a generic
 * uniform resource identifier. This interface allows each section 
 * of the uniform resource identifier to be represented. A generic
 * uniform resource identifier syntax is represented in RFC 2616 
 * section 3.2.2 for the HTTP protocol, this allows similar URI's
 * for example ftp, http, https, tftp. The syntax is
 * <code><pre>
 *
 *    URI = [scheme "://"] host [ ":" port ] [ path [ "?" query ]]
 *
 * </pre></code>
 * This interface represents the host, port, path and query part
 * of the uniform resource identifier. The parameters are also 
 * represented by the URI. The parameters in a URI consist of name 
 * and value pairs in the path segment of the URI.
 * <p>
 * This will normalize the path part of the uniform resource
 * identifier. A normalized path is one that contains no back
 * references like "./" and "../". The normalized path will not
 * contain the path parameters.
 *
 * @author Niall Gallagher
 */
public interface Address {

   /**
    * This allows the scheme of the URL given to be returned.
    * If the URI does not contain a scheme then this will
    * return null. The scheme of the URI is the part that
    * specifies the type of protocol that the URI is used
    * for, an example <code>http://domain/path</code> is
    * a URI that is intended for the http protocol. The
    * scheme is the string <code>http</code>.
    * 
    * @return the scheme tag for the address if available
    */
   String getScheme();

   /** 
    * This is used to retrieve the domain of this URI. The 
    * domain part in the URI is an optional part, an example
    * <code>http://domain/path?querypart</code>. This will 
    * return the value of the domain part. If there is no 
    * domain part then this will return null otherwise the 
    * domain value found in the uniform resource identifier.
    *
    * @return the domain part of the address if available
    */
   String getDomain();

   /** 
    * This is used to retrieve the port of the uniform resource 
    * identifier. The port part in this is an optional part, an 
    * example <code>http://host:port/path?querypart</code>. This 
    * will return the value of the port. If there is no port then 
    * this will return <code>-1</code> because this represents 
    * an impossible uniform resource identifier port. The port 
    * is an optional part.
    *   
    * @return this returns the port part if it is available
    */   
   int getPort();
   
   /** 
    * This is used to retrieve the path of this URI. The path part 
    * is the most fundamental part of the URI. This will return 
    * the value of the path. If there is no path part then this 
    * will return a Path implementation that represents the root
    * path represented by <code>/</code>. 
    * 
    * @return the path part of the uniform resource identifier
    */
   Path getPath();

   /** 
    * This is used to retrieve the query of this URI. The query part 
    * in the URI is an optional part. This will return the value 
    * of the query part. If there is no query part then this will 
    * return an empty <code>Query</code> object. The query is 
    * an optional member of a URI and comes after the path part, it
    * is preceded by a question mark, <code>?</code> character. 
    * For example the following URI contains <code>query</code> for
    * its query part, <code>http://host:port/path?query</code>.
    * <p>
    * This returns a <code>org.simpleframework.http.Query</code> 
    * object that can be used to interact directly with the query 
    * values. The <code>Query</code> object is a read-only interface
    * to the query parameters, and so will not affect the URI.
    *
    * @return a <code>Query</code> object for the query part
    */ 
   Query getQuery();

   /**
    * This extracts the parameter values from the uniform resource
    * identifier represented by this object. The parameters that a  
    * uniform resource identifier contains are embedded in the path 
    * part of the URI. If the path contains no parameters then this
    * will return an empty <code>Map</code> instance. 
    * <p>
    * This will produce unique name and value parameters. Thus if the 
    * URI contains several path segments with similar parameter names
    * this will return the deepest parameter. For example if the URI
    * represented was <code>http://domain/path1;x=y/path2;x=z</code>
    * the value for the parameter named <code>x</code> would be
    * <code>z</code>.
    *
    * @return this will return the parameter names found in the URI
    */
   KeyMap<String> getParameters();

   /** 
    * This is used to convert this URI object into a <code>String</code> 
    * object. This will only convert the parts of the URI that exist, so 
    * the URI may not contain the domain or the query part and it will 
    * not contain the path parameters. If the URI contains all these 
    * parts then it will return something like 
    * <pre>
    * scheme://host:port/path/path?querypart
    * </pre>
    * <p>
    * It can return <code>/path/path?querypart</code> style relative 
    * URI's. If any of the parts are set to null then that part will be 
    * missing, for example if only the path is available then this will
    * omit the domain, port and scheme. Showing a relative address.
    * <pre>
    * scheme://host:port/?querypart
    * </pre>
    *
    * @return the URI address with the optional parts if available
    */
   String toString();
}

