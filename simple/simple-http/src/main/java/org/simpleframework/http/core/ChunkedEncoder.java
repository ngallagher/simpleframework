/*
 * ChunkedEncoder.java February 2007
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

import java.io.IOException;
import java.nio.ByteBuffer;

import org.simpleframework.transport.ByteWriter;

/**
 * The <code>ChunkedEncoder</code> object is used to encode data in
 * the chunked encoding format. A chunked producer is required when
 * the length of the emitted content is unknown. It enables the HTTP
 * pipeline to remain open as it is a self delimiting format. This
 * is preferred over the <code>CloseEncoder</code> for HTTP/1.1 as
 * it maintains the pipeline and thus the cost of creating it.
 *
 * @author Niall Gallagher
 *
 * @see org.simpleframework.http.message.ChunkedConsumer
 */ 
class ChunkedEncoder implements BodyEncoder {

   /**
    * This is the size line which is used to generate the size.
    */         
   private byte[] size = { '0', '0', '0', '0', '0', '0', '0', '0', '\r', '\n' };

   /**
    * This is the hexadecimal alphabet used to translate the size.
    */ 
   private byte[] index = { '0', '1', '2', '3', '4', '5','6', '7', '8', '9', 'a', 'b', 'c', 'd','e', 'f' };

   /**
    * This is the zero length chunk sent when this is completed.
    */ 
   private byte[] zero = { '0', '\r', '\n', '\r', '\n' };

   /**
    * This is the observer used to notify the selector of events.
    */ 
   private BodyObserver observer;

   /**
    * This is the underlying writer used to deliver the encoded data.
    */ 
   private ByteWriter writer;
   
   /**
    * Constructor for the <code>ChunkedEncoder</code> object. This 
    * is used to create a producer that can sent data in the chunked 
    * encoding format. Once the data is encoded in the format it is
    * handed to the provided <code>ByteWriter</code> object which will
    * then deliver it to the client using the underlying transport.
    *
    * @param observer this is the observer used to signal I/O events    
    * @param writer this is the writer used to deliver the content
    */
   public ChunkedEncoder(BodyObserver observer, ByteWriter writer) {
      this.observer = observer;   
      this.writer = writer;
   }

   /**
    * This method is used to encode the provided array of bytes in
    * a HTTP/1.1 complaint format and sent it to the client. Once
    * the data has been encoded it is handed to the transport layer
    * within the server, which may choose to buffer the data if the
    * content is too small to send efficiently or if the socket is
    * not write ready.
    *
    * @param array this is the array of bytes to send to the client
    */        
   public void encode(byte[] array) throws IOException {
      encode(array, 0, array.length);
   }
   
   /**
    * This method is used to encode the provided array of bytes in
    * a HTTP/1.1 complaint format and sent it to the client. Once
    * the data has been encoded it is handed to the transport layer
    * within the server, which may choose to buffer the data if the
    * content is too small to send efficiently or if the socket is
    * not write ready.
    *
    * @param array this is the array of bytes to send to the client
    * @param off this is the offset within the array to send from
    * @param len this is the number of bytes that are to be sent
    */       
   public void encode(byte[] array, int off, int len) throws IOException {
      ByteBuffer buffer = ByteBuffer.wrap(array, off, len);
      
      if(len > 0) {
         encode(buffer);
      }  
   }
   
   /**
    * This method is used to encode the provided buffer of bytes in
    * a HTTP/1.1 compliant format and sent it to the client. Once
    * the data has been encoded it is handed to the transport layer
    * within the server, which may choose to buffer the data if the
    * content is too small to send efficiently or if the socket is
    * not write ready.
    *
    * @param buffer this is the buffer of bytes to send to the client
    */         
   public void encode(ByteBuffer buffer) throws IOException {
      int mark = buffer.position();
      int size = buffer.limit();
      
      if(mark > size) {
         throw new BodyEncoderException("Buffer position greater than limit");
      }
      encode(buffer, 0, size - mark);
   }

   /**
    * This method is used to encode the provided buffer of bytes in
    * a HTTP/1.1 compliant format and sent it to the client. Once
    * the data has been encoded it is handed to the transport layer
    * within the server, which may choose to buffer the data if the
    * content is too small to send efficiently or if the socket is
    * not write ready.
    *
    * @param buffer this is the buffer of bytes to send to the client
    * @param off this is the offset within the buffer to send from
    * @param len this is the number of bytes that are to be sent
    */          
   public void encode(ByteBuffer buffer, int off, int len) throws IOException {
      int pos = 7;

      if(observer.isClosed()) {
         throw new BodyEncoderException("Stream has been closed");
      }
      if(len > 0) {
         for(int num = len; num > 0; num >>>= 4){      
            size[pos--] = index[num & 0xf];
         }
         try {
            writer.write(size, pos + 1, 9 - pos);      
            writer.write(buffer, off, len);
            writer.write(size, 8, 2);
         } catch(Exception cause) {
            if(writer != null) {
               observer.error(writer);
            }
            throw new BodyEncoderException("Error sending response", cause);
         }
      }
   }

   /**
    * This method is used to flush the contents of the buffer to 
    * the client. This method will block until such time as all of
    * the data has been sent to the client. If at any point there
    * is an error sending the content an exception is thrown.    
    */ 
   public void flush() throws IOException {
      try {
         if(!observer.isClosed()) {
            writer.flush();
         }
      } catch(Exception cause) {
         if(writer != null) {
            observer.close(writer);
         }
         throw new BodyEncoderException("Error sending response", cause);
      }
   }

   /**
    * This method is used to write the zero length chunk. Writing
    * the zero length chunk tells the client that the response has
    * been fully sent, and the next sequence of bytes from the HTTP
    * pipeline is the start of the next response. This will signal
    * to the server kernel that the next request is read to read.
    */    
   private void finish() throws IOException {
      try {
         writer.write(zero);
         observer.ready(writer);
      } catch(Exception cause) {
         if(writer != null) {
            observer.close(writer);
         }
         throw new BodyEncoderException("Error flushing response", cause);
      }
   }
  
   /**
    * This is used to signal to the producer that all content has 
    * been written and the user no longer needs to write. This will
    * either close the underlying transport or it will notify the
    * monitor that the response has completed and the next request
    * can begin. This ensures the content is flushed to the client.
    */     
   public void close() throws IOException {
      if(!observer.isClosed()) {
         finish();         
      }     
   }
}


