/*
 * TransportWriter.java February 2007
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

package org.simpleframework.transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The <code>TransportWriter</code> object is used to write bytes to
 * and underlying transport. This is essentially an adapter between
 * an <code>OutputStream</code> and the underlying transport. Each
 * byte array segment written to the underlying transport is wrapped
 * in a bytes buffer so that it can be sent by the transport layer.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.Transport
 */
public class TransportWriter implements ByteWriter {

   /**
    * This is used to determine if the transport has been closed.
    */ 
   private final AtomicBoolean closed;   

   /**
    * This is the underlying transport to write the bytes to. 
    */   
   private final Transport transport;
   
   /**
    * Constructor for the <code>TransportWriter</code> object. This
    * is used to create an adapter for the transport such that a
    * byte array can be used to write bytes to the array.
    * 
    * @param transport the underlying transport to write bytes to
    */
   public TransportWriter(Transport transport) {  
      this.closed = new AtomicBoolean();
      this.transport = transport;
   }

   /**
    * This method is used to deliver the provided array of bytes to
    * the underlying transport. Depending on the connection type the
    * array may be encoded for SSL transport or write directly. Any
    * implementation may choose to buffer the bytes for performance.
    *
    * @param array this is the array of bytes to write to the client
    */    
   public void write(byte[] array) throws IOException {
      write(array, 0, array.length);      
   }

   /**
    * This method is used to deliver the provided array of bytes to
    * the underlying transport. Depending on the connection type the
    * array may be encoded for SSL transport or write directly. Any
    * implementation may choose to buffer the bytes for performance.
    *
    * @param array this is the array of bytes to write to the client
    * @param off this is the offset within the array to write from
    * @param len this is the number of bytes that are to be sent
    */    
   public void write(byte[] array, int off, int len) throws IOException {
      ByteBuffer buffer = ByteBuffer.wrap(array, off, len);
      
      if(len > 0) {
         write(buffer);
      }      
   }
   
   /**
    * This method is used to deliver the provided buffer of bytes to
    * the underlying transport. Depending on the connection type the
    * array may be encoded for SSL transport or write directly. Any
    * implementation may choose to buffer the bytes for performance.
    *
    * @param buffer this is the buffer of bytes to write to the client
    */             
   public void write(ByteBuffer buffer) throws IOException {
      int mark = buffer.position();
      int size = buffer.limit();
      
      if(mark > size) {
         throw new IOException("Buffer position greater than limit");
      }
      write(buffer, 0, size - mark);
   }
   
   /**
    * This method is used to deliver the provided buffer of bytes to
    * the underlying transport. Depending on the connection type the
    * array may be encoded for SSL transport or write directly. Any
    * implementation may choose to buffer the bytes for performance.
    *
    * @param buffer this is the buffer of bytes to write to the client
    * @param off this is the offset within the buffer to write from
    * @param len this is the number of bytes that are to be sent
    */    
   public void write(ByteBuffer buffer, int off, int len) throws IOException {
      int mark = buffer.position();
      int limit = buffer.limit();
      
      if(limit - mark > len) {
         buffer.limit(mark + len); // reduce usable size
      }               
      transport.write(buffer);
      buffer.limit(limit);                  
   }

   /**
    * This method is used to flush the contents of the buffer to 
    * the client. This method will block until such time as all of
    * the data has been sent to the client. If at any point there
    * is an error writing the content an exception is thrown.    
    */     
   public void flush() throws IOException {
      transport.flush();                             
   }
   
   /**
    * This is used to close the writer and the underlying transport.
    * If a close is performed on the writer then no more bytes can
    * be read from or written to the transport and the client will
    * received a connection close on their side.
    */     
   public void close() throws IOException {
      if(!closed.getAndSet(true)) {           
         transport.close();
       }                 
   }
}