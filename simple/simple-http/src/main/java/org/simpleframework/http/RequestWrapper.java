/*
 * RequestWrapper.java February 2001
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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.simpleframework.transport.Certificate;
import org.simpleframework.transport.Channel;

/** 
 * The <code>RequestWrapper</code> object is used so that the original
 * <code>Request</code> object can be wrapped in a filtering proxy
 * object. This allows a <code>Container</code> that interacts with 
 * a modified request object. To add functionality to the request it
 * can be wrapped in a subclass of this and the overridden methods 
 * can provide modified functionality to the standard request.
 *
 * @author Niall Gallagher
 */
public class RequestWrapper implements Request {
   
   /**
    * This is the request instance that is being wrapped.
    */
   protected Request request;          

   /** 
    * Constructor for <code>RequestWrapper</code> object. This allows
    * the original <code>Request</code> object to be wrapped so that 
    * adjustments to the behaviour of a request object handed to the 
    * container can be provided by a subclass implementation. 
    *
    * @param request the request object that is being wrapped
    */
   public RequestWrapper(Request request){
      this.request = request;
   }

   /**
    * This can be used to get the major number from a HTTP version.
    * The major version corresponds to the major type that is the 1
    * of a HTTP/1.0 version string. 
    *
    * @return the major version number for the request message
    */ 
   public int getMajor() {
      return request.getMajor();
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
      return request.getMinor();
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
      return request.getMethod();
   }

   /**
    * This can be used to get the URI specified for this HTTP request.
    * This corresponds to the either the full HTTP URI or the path
    * part of the URI depending on how the client sends the request.
    *
    * @return the URI address that this HTTP request is targeting
    */ 
   public String getTarget() {
      return request.getTarget();
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
      return request.getAddress();
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
      return request.getPath();
   }
   
   /**
    * This method is used to acquire the query part from the HTTP 
    * request URI target and a form post if it exists. Both the 
    * query and the form post are merge together in a single query.
    * 
    * @return the query associated with the HTTP target URI
    */ 
   public Query getQuery() {
      return request.getQuery();
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
      return request.getNames();
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
      return request.getInteger(name);
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
      return request.getDate(name);
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
      return request.getCookie(name);
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
      return request.getCookies();
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
      return request.getValue(name);
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
      return request.getValue(name, index);
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
      return request.getValues(name);
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
      return request.getLocales();
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
      return request.getContentType();
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
      return request.getContentLength();
   }
   
   /**
    * This is used to determine if the request has been transferred
    * over a secure connection. If the protocol is HTTPS and the 
    * content is delivered over SSL then the request is considered
    * to be secure. Also the associated response will be secure.
    * 
    * @return true if the request is transferred securely
    */
   public boolean isSecure() {
      return request.isSecure();
   }
   
   /**
    * This is a convenience method that is used to determine whether 
    * or not this message has the <code>Connection: close</code> 
    * header. If the close token is present then this stream is not
    * a keep-alive connection. If this has no <code>Connection</code> 
    * header then the keep-alive status is determined by the HTTP
    * version, that is, HTTP/1.1 is keep-alive by default, HTTP/1.0
    * is not keep-alive by default.
    *
    * @return returns true if this has a keep-alive stream
    */ 
   public boolean isKeepAlive() {
      return request.isKeepAlive();
   }
   
   /**
    * This is the time in milliseconds when the request was first
    * read from the underlying socket. The time represented here
    * represents the time collection of this request began. This 
    * does not necessarily represent the time the bytes arrived as
    * as some data may have been buffered before it was parsed.
    * 
    * @return this represents the time the request arrived at
    */
   public long getRequestTime() {
      return request.getRequestTime();
   }
   
   /**
    * This provides the underlying channel for the request. It
    * contains the TCP socket channel and various other low level
    * components. Typically this will only ever be needed when
    * there is a need to switch protocols.  
    * 
    * @return the underlying channel for this request 
    */
   public Channel getChannel() {
      return request.getChannel();
   }   
   
   /**
    * This is used to acquire the SSL certificate used when the
    * server is using a HTTPS connection. For plain text connections
    * or connections that use a security mechanism other than SSL
    * this will be null. This is only available when the connection
    * makes specific use of an SSL engine to secure the connection.
    * 
    * @return this returns the associated SSL certificate if any
    */
   public Certificate getClientCertificate() {
      return request.getClientCertificate();
   }
   
   /**
    * This can be used to retrieve the response attributes. These can
    * be used to keep state with the response when it is passed to
    * other systems for processing. Attributes act as a convenient
    * model for storing objects associated with the response. This 
    * also inherits attributes associated with the client connection.
    *
    * @return the attributes that have been set on this response
    */ 
   public Map getAttributes() {
      return request.getAttributes();
   }

   /**
    * This is used as a shortcut for acquiring attributes for the
    * response. This avoids acquiring the attribute <code>Map</code>
    * in order to retrieve the attribute directly from that object.
    * The attributes contain data specific to the response.
    * 
    * @param key this is the key of the attribute to acquire
    * 
    * @return this returns the attribute for the specified name
    */ 
   public Object getAttribute(Object key) {
      return request.getAttribute(key);
   }
   
   /**
    * This is used to acquire the remote client address. This can 
    * be used to acquire both the port and the I.P address for the 
    * client. It allows the connected clients to be logged and if
    * require it can be used to perform course grained security.
    * 
    * @return this returns the client address for this request
    */
   public InetSocketAddress getClientAddress() {
      return request.getClientAddress();
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
      return request.getHeader();
   }
   
   /**
    * This is used to get the content body. This will essentially get
    * the content from the body and present it as a single string.
    * The encoding of the string is determined from the content type
    * charset value. If the charset is not supported this will throw
    * an exception. Typically only text values should be extracted
    * using this method if there is a need to parse that content.
    * 
    * @exception IOException signifies that there is an I/O problem
    *
    * @return the body content as an encoded string value
    */    
   public String getContent() throws IOException {
      return request.getContent();
   }

   /**
    * This is used to read the content body. The specifics of the data
    * that is read from this <code>InputStream</code> can be determined
    * by the <code>getContentLength</code> method. If the data sent by
    * the client is chunked then it is decoded, see RFC 2616 section
    * 3.6. Also multipart data is available as <code>Part</code> objects
    * however the raw content of the multipart body is still available.
    *
    * @exception Exception signifies that there is an I/O problem
    *
    * @return returns the input stream containing the message body 
    */ 
   public InputStream getInputStream() throws IOException {
      return request.getInputStream();
   }
   
   /**
    * This is used to read the content body. The specifics of the data
    * that is read from this <code>ReadableByteChannel</code> can be 
    * determined by the <code>getContentLength</code> method. If the 
    * data sent by the client is chunked then it is decoded, see RFC 
    * 2616 section 3.6. This stream will never provide empty reads as
    * the content is internally buffered, so this can do a full read.
    * 
    * @return this returns the byte channel used to read the content
    */
   public ReadableByteChannel getByteChannel() throws IOException {
      return request.getByteChannel();
   }
   
   /**
    * This is used to provide quick access to the parameters. This
    * avoids having to acquire the request <code>Form</code> object.
    * This basically acquires the parameters object and invokes 
    * the <code>getParameters</code> method with the given name.
    * 
    * @param name this is the name of the parameter value      
    */   
   public String getParameter(String name) {
      return request.getParameter(name);
   }
   
   /**
    * This method is used to acquire a <code>Part</code> from the
    * HTTP request using a known name for the part. This is typically 
    * used  when there is a file upload with a multipart POST request.
    * All parts that are not files can be acquired as string values
    * from the attachment object.
    * 
    * @param name this is the name of the part object to acquire
    * 
    * @return the named part or null if the part does not exist
    */   
   public Part getPart(String name) {
      return request.getPart(name);
   }
   
   /**
    * This method is used to get all <code>Part</code> objects that
    * are associated with the request. Each attachment contains the 
    * body and headers associated with it. If the request is not a 
    * multipart POST request then this will return an empty list.
    * 
    * @return the list of parts associated with this request
    */     
   public List<Part> getParts() {
      return request.getParts();
   }
   
   /**
    * This method returns a string representing the header that was
    * consumed for this request. For performance reasons it is better
    * to acquire the character sequence representing the header as it
    * does not require the allocation on new memory.
    * 
    * @return this returns a string representation of this request
    */
   public String toString() {
      return request.toString();
   }
}
