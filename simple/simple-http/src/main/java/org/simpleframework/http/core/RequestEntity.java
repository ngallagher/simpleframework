/*
 * RequestEntity.java February 2001
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

import static org.simpleframework.http.Protocol.CLOSE;
import static org.simpleframework.http.Protocol.CONNECTION;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simpleframework.http.ContentType;
import org.simpleframework.http.Part;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.message.Body;
import org.simpleframework.http.message.Entity;
import org.simpleframework.transport.Certificate;
import org.simpleframework.transport.Channel;

/**
 * This object is used to represent a HTTP request. This defines the
 * attributes that a HTTP request has such as a request line and the
 * headers that come with the message header. 
 * <p>
 * The <code>Request</code> is used to provide an interface to the 
 * HTTP <code>InputStream</code> and message header. The stream can
 * have certain characteristics, these characteristics are available 
 * by this object. The <code>Request</code> provides methods that 
 * allow the <code>InputStream</code>'s semantics to be known, for 
 * example if the stream is keep-alive or if the stream has a length.
 * <p>
 * The <code>Request</code> origin is also retrievable from the
 * <code>Request</code> as is the attributes <code>Map</code> object
 * which defines specific connection attributes. And acts as a 
 * simple model for the request transaction.
 * <p>
 * It is important to note that the <code>Request</code> controls
 * the processing of the HTTP pipeline. The next HTTP request is 
 * not processed until the request has read all of the content body
 * within the <code>InputStream</code>. The stream must be fully
 * read or closed for the next request to be processed. 
 *
 * @author Niall Gallagher
 */ 
class RequestEntity extends RequestMessage implements Request {
   
   /**
    * This is the certificate associated with the connection.
    */
   private Certificate certificate;
   
   /**
    * This will create the form object using the query and body.
    */
   private QueryBuilder builder; 
   
   /**
    * This channel represents the connected pipeline used.
    */
   private Channel channel;
   
   /**
    * The query contains all the parameters for the request.
    */
   private Query query;
   
   /**
    * The body contains the message content sent by the client.
    */
   private Body body;
   
   /**
    * This is used to contain the values for this request.
    */
   private Map map;
   
   /**
    * This is the time at which the request is ready to be used.
    */
   private long time;
   
   /**
    * Constructor for the <code>RequestEntity</code> object. This is
    * used to create a request that contains all the parts sent by
    * the client, including the headers and the request body. Each of
    * the request elements are accessible through this object in a
    * convenient manner, all parts and parameters, as well as cookies
    * can be accessed and used without much effort.
    * 
    * @param observer this is the observer used to monitor events     
    * @param entity this is the entity that was sent by the client
    */
   public RequestEntity(ResponseObserver observer, Entity entity) {
      this.certificate = new RequestCertificate(observer, entity);
      this.builder = new QueryBuilder(this, entity);
      this.channel = entity.getChannel();
      this.header = entity.getHeader();      
      this.body = entity.getBody();
      this.time = entity.getTime();
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
      return channel.isSecure();
   }
   
   /**
    * This is a convenience method that is used to determine whether 
    * or not this message has the Connection header with the close 
    * token. If the close token is present then this stream is not a 
    * keep-alive connection. However if this has no Connection header 
    * then the keep alive status is determined by the HTTP version, 
    * that is HTTP/1.1 is keep alive by default, HTTP/1.0 has the 
    * connection close by default.
    *
    * @return returns true if this is keep alive connection
    */ 
   public boolean isKeepAlive(){
      String value = getValue(CONNECTION);
      
      if(value == null) {
         int major = getMajor();
         int minor = getMinor();
         
         if(major > 1) {
            return true;         
         }
         if(major == 1) {
            return minor > 0;
         }
         return false;
      }
      return !value.equalsIgnoreCase(CLOSE);
   }
   
   /**
    * This is the time in milliseconds when the request was first
    * read from the underlying socket. The time represented here
    * represents the time collection of this request began. This 
    * does not necessarily represent the time the bytes arrived on
    * the receive buffers as some data may have been buffered.
    * 
    * @return this represents the time the request arrived at
    */
   public long getRequestTime() {
      return time;
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
      return channel;
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
      if(channel.isSecure()) {
         return certificate;
      }
      return null;
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
      SocketChannel socket = channel.getSocket();
      Socket client = socket.socket();
      
      return getClientAddress(client);
   }
   
   /**
    * This is used to acquire the remote client address. This can 
    * be used to acquire both the port and the I.P address for the 
    * client. It allows the connected clients to be logged and if
    * require it can be used to perform course grained security.
    * 
    * @param socket this is the socket to get the address for
    * 
    * @return this returns the client address for this request
    */
   private InetSocketAddress getClientAddress(Socket socket) {
      InetAddress address = socket.getInetAddress();
      int port = socket.getPort();
      
      return new InetSocketAddress(address, port);
   }

   /**
    * This is used to get the content body. This will essentially get
    * the content from the body and present it as a single string.
    * The encoding of the string is determined from the content type
    * charset value. If the charset is not supported this will throw
    * an exception. Typically only text values should be extracted
    * using this method if there is a need to parse that content.
    *
    * @return the body content containing the message body
    */ 
   public String getContent() throws IOException {
      ContentType type = getContentType();
      
      if(type == null) {
         return body.getContent("ISO-8859-1");
      }      
      return getContent(type);
   }
   
   /**
    * This is used to get the content body. This will essentially get
    * the content from the body and present it as a single string.
    * The encoding of the string is determined from the content type
    * charset value. If the charset is not supported this will throw
    * an exception. Typically only text values should be extracted
    * using this method if there is a need to parse that content.
    * 
    * @param type this is the content type used with the request
    *
    * @return the input stream containing the message body
    */ 
   public String getContent(ContentType type) throws IOException {
      String charset = type.getCharset();
      
      if(charset == null) {
         charset = "ISO-8859-1";
      }
      return body.getContent(charset);
   }
   
   /**
    * This is used to read the content body. The specifics of the data
    * that is read from this <code>InputStream</code> can be determined
    * by the <code>getContentLength</code> method. If the data sent by
    * the client is chunked then it is decoded, see RFC 2616 section
    * 3.6. Also multipart data is available as <code>Part</code> objects
    * however the raw content of the multipart body is still available.
    *
    * @return the input stream containing the message body
    */ 
   public InputStream getInputStream() throws IOException {
      return body.getInputStream();
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
      InputStream source = getInputStream();
      
      if(source != null) {
         return Channels.newChannel(source);
      }
      return null;
   }
   
   /**
    * This can be used to retrieve the response attributes. These can
    * be used to keep state with the response when it is passed to
    * other systems for processing. Attributes act as a convenient
    * model for storing objects associated with the response. This 
    * also inherits attributes associated with the client connection.
    *
    * @return the attributes that have been added to this request
    */ 
   public Map getAttributes() {
      Map common = channel.getAttributes();
      
      if(map == null) {
         map = new HashMap(common);
      }
      return map;
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
      return getAttributes().get(key);
   }
   
   /**
    * This method is used to acquire the query part from the HTTP 
    * request URI target and a form post if it exists. Both the 
    * query and the form post are merge together in a single query.
    * 
    * @return the query associated with the HTTP target URI
    */   
   public Query getQuery() {
      if(query == null) {
         query = builder.build();
       }
       return query;
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
      return getQuery().get(name);
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
      return body.getPart(name);
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
      return body.getParts();
   }
}
