/*
 * Request.java February 2001
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
import java.util.Map;

import org.simpleframework.transport.Certificate;
import org.simpleframework.transport.Channel;

/**
 * The <code>Request</code> is used to provide an interface to the 
 * HTTP entity body and message header. This provides methods that 
 * allow the entity body to be acquired as a stream, string, or if
 * the message is a multipart encoded body, then the individual
 * parts of the request body can be acquired.
 * <p>
 * This can also maintain data during the request lifecycle as well
 * as the session lifecycle. A <code>Session</code> is made available
 * for convenience. It provides a means for the services to associate
 * data with a given client session, which can be retrieved when 
 * there are subsequent requests sent to the server.
 * <p>
 * It is important to note that the entity body can be read multiple
 * times from the request. Calling <code>getInputStream</code> will
 * start reading from the first byte in the body regardless of the
 * number of times it is called. This allows POST parameters as well
 * as multipart bodies to be read from the stream if desired.
 *
 * @author Niall Gallagher
 */ 
public interface Request extends RequestHeader {
   
   /**
    * This is used to determine if the request has been transferred
    * over a secure connection. If the protocol is HTTPS and the 
    * content is delivered over SSL then the request is considered
    * to be secure. Also the associated response will be secure.
    * 
    * @return true if the request is transferred securely
    */
   boolean isSecure();
   
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
   boolean isKeepAlive();  
   
   /**
    * This is the time in milliseconds when the request was first
    * read from the underlying socket. The time represented here
    * represents the time collection of this request began. This 
    * does not necessarily represent the time the bytes arrived as
    * as some data may have been buffered before it was parsed.
    * 
    * @return this represents the time the request arrived at
    */
   long getRequestTime();
   
   /**
    * This provides the underlying channel for the request. It
    * contains the TCP socket channel and various other low level
    * components. Typically this will only ever be needed when
    * there is a need to switch protocols.  
    * 
    * @return the underlying channel for this request 
    */
   Channel getChannel();

   /**
    * This is used to acquire the SSL certificate used when the
    * server is using a HTTPS connection. For plain text connections
    * or connections that use a security mechanism other than SSL
    * this will be null. This is only available when the connection
    * makes specific use of an SSL engine to secure the connection.
    * 
    * @return this returns the associated SSL certificate if any
    */
   Certificate getClientCertificate();
   
   /**
    * This is used to acquire the remote client address. This can 
    * be used to acquire both the port and the I.P address for the 
    * client. It allows the connected clients to be logged and if
    * require it can be used to perform course grained security.
    * 
    * @return this returns the client address for this request
    */
   InetSocketAddress getClientAddress();
   
   /**
    * This can be used to retrieve the response attributes. These can
    * be used to keep state with the response when it is passed to
    * other systems for processing. Attributes act as a convenient
    * model for storing objects associated with the response. This 
    * also inherits attributes associated with the client connection.
    *
    * @return the attributes of that have been set on the request
    */ 
   Map getAttributes();

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
   Object getAttribute(Object key);
   
   /**
    * This is used to provide quick access to the parameters. This
    * avoids having to acquire the request <code>Form</code> object.
    * This basically acquires the parameters object and invokes 
    * the <code>getParameters</code> method with the given name.
    * 
    * @param name this is the name of the parameter value
    */   
   String getParameter(String name); 
   
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
   Part getPart(String name);
   
   /**
    * This method is used to get all <code>Part</code> objects that
    * are associated with the request. Each attachment contains the 
    * body and headers associated with it. If the request is not a 
    * multipart POST request then this will return an empty list.
    * 
    * @return the list of parts associated with this request
    */     
   List<Part> getParts();
   
   /**
    * This is used to get the content body. This will essentially get
    * the content from the body and present it as a single string.
    * The encoding of the string is determined from the content type
    * charset value. If the charset is not supported this will throw
    * an exception. Typically only text values should be extracted
    * using this method if there is a need to parse that content.
    *     
    * @return this returns the message bytes as an encoded string
    */    
   String getContent() throws IOException;
   
   /**
    * This is used to read the content body. The specifics of the data
    * that is read from this <code>InputStream</code> can be determined
    * by the <code>getContentLength</code> method. If the data sent by
    * the client is chunked then it is decoded, see RFC 2616 section
    * 3.6. Also multipart data is available as <code>Part</code> objects
    * however the raw content of the multipart body is still available.
    *
    * @return this returns an input stream containing the message body
    */ 
   InputStream getInputStream() throws IOException;
   
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
   ReadableByteChannel getByteChannel() throws IOException; 
}
