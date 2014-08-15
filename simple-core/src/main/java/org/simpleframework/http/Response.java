/*
 * Response.java February 2001
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
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.channels.WritableByteChannel;

/**
 * This is used to represent the HTTP response. This provides methods 
 * that can be used to set various characteristics of the response.
 * An <code>OutputStream</code> can be acquired via this interface
 * which can be used to write the response body. A buffer size can be
 * specified when acquiring the output stream which allows data to
 * be buffered until it over flows or is flushed explicitly. This
 * buffering allows a partially written response body to be reset.
 * <p>
 * This should never allow the message body be sent if it should not 
 * be sent with the headers as of RFC 2616 rules for the presence of 
 * a message body. A message body must not be included with a HEAD 
 * request or with a 304 or a 204 response. A proper implementation
 * of this will prevent a message body being sent if the response
 * is to a HEAD request of if there is a 304 or 204 response code.
 * <p>
 * It is important to note that the <code>Response</code> controls
 * the processing of the HTTP pipeline. The next HTTP request is 
 * not processed until the response has been sent. To ensure that
 * the response is sent the <code>close</code> method of the response
 * or the output stream should be used. This will notify the server
 * to dispatch the next request in the pipeline for processing.
 *
 * @author Niall Gallagher
 */ 
public interface Response extends ResponseHeader { 
   
   /**
    * This should be used when the size of the message body is known. 
    * This ensures that Persistent HTTP (PHTTP) connections can be 
    * maintained for both HTTP/1.0 and HTTP/1.1 clients. If the length
    * of the output is not known HTTP/1.0 clients will require a
    * connection close, which reduces performance (see RFC 2616).
    * <p>
    * This removes any previous Content-Length headers from the message 
    * header. This will then set the appropriate Content-Length header 
    * with the correct length. If a the Connection header is set with the 
    * close token then the semantics of the connection are such that the 
    * server will close it once the output stream or request is closed.
    *
    * @param length this is the length of the HTTP message body
    */ 
   void setContentLength(long length);
   
   /**
    * This is used to set the content type for the response. Typically
    * a response will contain a message body of some sort. This is used
    * to conveniently set the type for that response. Setting the 
    * content type can also be done explicitly if desired.
    * 
    * @param type this is the type that is to be set in the response
    */
   void setContentType(String type);
   
   /**
    * Used to write a message body with the <code>Response</code>. The 
    * semantics of this <code>OutputStream</code> will be determined 
    * by the HTTP version of the client, and whether or not the content
    * length has been set, through the <code>setContentLength</code>
    * method. If the length of the output is not known then the output
    * is chunked for HTTP/1.1 clients and closed for HTTP/1.0 clients.
    *
    * @return an output stream object with the specified semantics
    */ 
   OutputStream getOutputStream() throws IOException; 

   /**
    * Used to write a message body with the <code>Response</code>. The 
    * semantics of this <code>OutputStream</code> will be determined 
    * by the HTTP version of the client, and whether or not the content
    * length has been set, through the <code>setContentLength</code>
    * method. If the length of the output is not known then the output
    * is chunked for HTTP/1.1 clients and closed for HTTP/1.0 clients. 
    * <p>
    * This will ensure that there is buffering done so that the output
    * can be reset using the <code>reset</code> method. This will 
    * enable the specified number of bytes to be written without
    * committing the response. This specified size is the minimum size
    * that the response buffer must be. 
    *
    * @return an output stream object with the specified semantics
    */ 
   OutputStream getOutputStream(int size) throws IOException;

   /**
    * This method is provided for convenience so that the HTTP content
    * can be written using the <code>print</code> methods provided by
    * the <code>PrintStream</code>. This will basically wrap the 
    * <code>getOutputStream</code> with a buffer size of zero.
    * <p>
    * The retrieved <code>PrintStream</code> uses the charset used to
    * describe the content, with the Content-Type header. This will
    * check the charset parameter of the contents MIME type. So if 
    * the Content-Type was <code>text/plain; charset=UTF-8</code> the
    * resulting <code>PrintStream</code> would encode the written data
    * using the UTF-8 encoding scheme. Care must be taken to ensure
    * that bytes written to the stream are correctly encoded.
    * <p> 
    * Implementations of the <code>Response</code> must guarantee
    * that this can be invoked repeatedly without effecting any issued 
    * <code>OutputStream</code> or <code>PrintStream</code> object.
    *
    * @return a print stream that provides convenience writing
    */
   PrintStream getPrintStream() throws IOException;

   /**
    * This method is provided for convenience so that the HTTP content
    * can be written using the <code>print</code> methods provided by
    * the <code>PrintStream</code>. This will basically wrap the 
    * <code>getOutputStream</code> with a specified buffer size.
    * <p>
    * The retrieved <code>PrintStream</code> uses the charset used to
    * describe the content, with the Content-Type header. This will
    * check the charset parameter of the contents MIME type. So if 
    * the Content-Type was <code>text/plain; charset=UTF-8</code> the
    * resulting <code>PrintStream</code> would encode the written data
    * using the UTF-8 encoding scheme. Care must be taken to ensure
    * that bytes written to the stream are correctly encoded.
    * <p> 
    * Implementations of the <code>Response</code> must guarantee
    * that this can be invoked repeatedly without effecting any issued 
    * <code>OutputStream</code> or <code>PrintStream</code> object.
    *
    * @param size the minimum size that the response buffer must be
    *
    * @return a print stream that provides convenience writing
    */
   PrintStream getPrintStream(int size) throws IOException;
   
   /**
    * Used to write a message body with the <code>Response</code>. The 
    * semantics of this <code>WritableByteChannel</code> are determined 
    * by the HTTP version of the client, and whether or not the content
    * length has been set, through the <code>setContentLength</code>
    * method. If the length of the output is not known then the output
    * is chunked for HTTP/1.1 clients and closed for HTTP/1.0 clients.
    * 
    * @return a writable byte channel used to write the message body
    */ 
   WritableByteChannel getByteChannel() throws IOException;

   /**
    * Used to write a message body with the <code>Response</code>. The 
    * semantics of this <code>WritableByteChannel</code> are determined 
    * by the HTTP version of the client, and whether or not the content
    * length has been set, through the <code>setContentLength</code>
    * method. If the length of the output is not known then the output
    * is chunked for HTTP/1.1 clients and closed for HTTP/1.0 clients.
    * <p>
    * This will ensure that there is buffering done so that the output
    * can be reset using the <code>reset</code> method. This will 
    * enable the specified number of bytes to be written without
    * committing the response. This specified size is the minimum size
    * that the response buffer must be. 
    * 
    * @param size the minimum size that the response buffer must be
    * 
    * @return a writable byte channel used to write the message body
    */ 
   WritableByteChannel getByteChannel(int size) throws IOException;
   
   /**
    * This represents the time at which the response has fully written.
    * Because the response is delivered asynchronously to the client
    * this response time does not represent the time to last byte.
    * It simply represents the time at which the response has been
    * fully generated and written to the output buffer or queue. This
    * returns zero if the response has not finished.
    *  
    * @return this is the time taken to complete the response
    */
   long getResponseTime();
   
   /**
    * This is used to determine if the HTTP response message is a 
    * keep alive message or if the underlying socket was closed. Even
    * if the client requests a connection keep alive and supports
    * persistent connections, the response can still be closed by
    * the server. This can be explicitly indicated by the presence
    * of the <code>Connection</code> HTTP header, it can also be
    * implicitly indicated by using version HTTP/1.0.
    * 
    * @return this returns true if the connection was closed
    */
   boolean isKeepAlive();
   
   /**
    * This can be used to determine whether the <code>Response</code>
    * has been committed. This is true if the <code>Response</code> 
    * was committed, either due to an explicit invocation of the
    * <code>commit</code> method or due to the writing of content. If
    * the <code>Response</code> has committed the <code>reset</code> 
    * method will not work in resetting content already written.
    *
    * @return true if the response headers have been committed
    */ 
   boolean isCommitted();
   
   /**
    * This is used to write the headers that where given to the
    * <code>Response</code>. Any further attempts to give headers 
    * to the <code>Response</code> will be futile as only the headers
    * that were given at the time of the first commit will be used 
    * in the message header.
    * <p>
    * This also performs some final checks on the headers submitted.
    * This is done to determine the optimal performance of the 
    * output. If no specific Connection header has been specified
    * this will set the connection so that HTTP/1.0 closes by default.
    *
    * @exception IOException thrown if there was a problem writing
    */
   void commit() throws IOException;

   /**
    * This can be used to determine whether the <code>Response</code>
    * has been committed. This is true if the <code>Response</code> 
    * was committed, either due to an explicit invocation of the
    * <code>commit</code> method or due to the writing of content. If
    * the <code>Response</code> has committed the <code>reset</code> 
    * method will not work in resetting content already written.
    *
    * @throws IOException thrown if there is a problem resetting 
    */ 
   void reset() throws IOException;
   
   /**
    * This is used to close the connection and commit the request. 
    * This provides the same semantics as closing the output stream
    * and ensures that the HTTP response is committed. This will
    * throw an exception if the response can not be committed.
    * 
    * @throws IOException thrown if there is a problem writing
    */
   void close() throws IOException;
}
