/*
 * Transport.java February 2007
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

package org.simpleframework.transport;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The <code>Transport</code> interface represents a low level means
 * to deliver content to the connected client. Typically this will
 * be a connected, non-blocking, TCP connection. However, for tests
 * and other purposes this may be adapted. The general contract of
 * the transport is that it provides non-blocking reads and blocking
 * writes. Blocking writes are required to ensure that memory does
 * not build up in output buffers during high load periods.
 *
 * @author Niall Gallagher
 */ 
public interface Transport extends Socket {
   
   /**
    * This is used to acquire the SSL certificate used when the
    * server is using a HTTPS connection. For plain text connections
    * or connections that use a security mechanism other than SSL
    * this will be null. This is only available when the connection
    * makes specific use of an SSL engine to secure the connection.
    * 
    * @return this returns the associated SSL certificate if any
    */
   Certificate getCertificate() throws IOException;
   
   /**
    * This is used to perform a non-blocking read on the transport.
    * If there are no bytes available on the input buffers then
    * this method will return zero and the buffer will remain the
    * same. If there is data and the buffer can be filled then this
    * will return the number of bytes read. Finally if the socket
    * is closed this will return a -1 value.
    *
    * @param buffer this is the buffer to append the bytes to
    *
    * @return this returns the number of bytes that have been read  
    */ 
   int read(ByteBuffer buffer) throws IOException;
   
   /**
    * This method is used to deliver the provided buffer of bytes to
    * the underlying transport. Depending on the connection type the
    * array may be encoded for SSL transport or send directly. Any
    * implementation may choose to buffer the bytes for performance.
    *
    * @param buffer this is the buffer of bytes to send to the client
    */      
   void write(ByteBuffer buffer) throws IOException;
   
   /**
    * This method is used to flush the contents of the buffer to 
    * the client. This method will block not block but will simply
    * flush any data to the underlying transport. Internally the
    * data will be queued for delivery to the connected entity.    
    */       
   void flush() throws IOException;
   
   /**
    * This is used to close the transport and the underlying socket.
    * If a close is performed on the transport then no more bytes 
    * can be read from or written to the transport and the client 
    * will receive a connection close on their side.
    */      
   void close() throws IOException;
}




