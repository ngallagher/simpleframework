/*
 * TransportReader.java February 2007
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
 * The <code>TransportReader</code> object represents a reader that
 * can read and buffer data from an underlying transport. If the
 * number of bytes read from the reader is more than required for
 * the HTTP request then those bytes can be pushed back in to the
 * cursor using the <code>reset</code> method. This will only allow
 * the last read to be reset within the cursor safely. 
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.Transport
 */
class TransportReader implements ByteReader {

   /**
    * This is the underlying transport to read the bytes from. 
    */
   private Transport transport;

   /**
    * This is used to store the bytes read from the transport.
    */
   private ByteBuffer buffer;
   
   /**
    * This is used to determine if the transport has been closed.
    */ 
   private boolean closed;
   
   /**
    * This represents the number of bytes that are ready to read.
    */
   private int count;
 
   /**
    * Constructor for the <code>TransportReader</code> object. This
    * requires a transport to read the bytes from. By default this 
    * will create a buffer of two kilobytes to read the input in to
    * which ensures several requests can be read at once.
    * 
    * @param transport this is the underlying transport to use
    */
   public TransportReader(Transport transport) {
      this(transport, 2048);
   }

   /**
    * Constructor for the <code>TransportReader</code> object. This
    * requires a transport to read the bytes from. By default this 
    * will create a buffer of of the specified size to read the 
    * input in to which enabled bytes to be buffered internally.
    * 
    * @param transport this is the underlying transport to use
    * @param size this is the size of the internal buffer to use
    */   
   public TransportReader(Transport transport, int size) {
      this.buffer = ByteBuffer.allocate(size);
      this.transport = transport;
   }
   
   /**
    * Determines whether the source is still open. The source is
    * considered open if there are still bytes to read. If there is
    * still bytes buffered and the underlying transport is closed
    * then the source is still considered open. 
    * 
    * @return true if there is nothing more to be read from this
    */   
   public boolean isOpen() throws IOException {
      return count != -1;
   }

   /**
    * Determines whether the source is ready for reading. When the
    * source is ready then it guarantees that some amount of bytes
    * can be read from the underlying stream without blocking.
    *
    * @return true if some data can be read without blocking
    */   
   public boolean isReady() throws IOException {
      return ready() > 0;
   }

   /**
    * Reads a block of bytes from the underlying stream. This will
    * read up to the requested number of bytes from the underlying
    * stream. If there are no ready bytes on the stream this can 
    * return zero, representing the fact that nothing was read.
    *
    * @param data this is the array to read the bytes in to 
    *
    * @return this returns the number of bytes read from the stream 
    */    
   public int read(byte[] data) throws IOException {
      return read(data, 0, data.length);
   }

   /**
    * Reads a block of bytes from the underlying stream. This will
    * read up to the requested number of bytes from the underlying
    * stream. If there are no ready bytes on the stream this can 
    * return zero, representing the fact that nothing was read.
    *
    * @param data this is the array to read the bytes in to
    * @param off this is the offset to begin writing the bytes to
    * @param len this is the number of bytes that are requested 
    *
    * @return this returns the number of bytes read from the stream 
    */    
   public int read(byte[] data, int off, int len) throws IOException {
      if(count <= 0) { // has the channel ended
         return count;
      }
      int size = Math.min(len, count); // get the minimum

      if(size > 0) {
         buffer.get(data, off, size); // get the bytes
         count -= size;
      }
      return Math.max(0, size);
   }

   /**
    * Provides the number of bytes that can be read from the stream
    * without blocking. This is typically the number of buffered or
    * available bytes within the stream. When this reaches zero then
    * the source may perform a blocking read.
    *
    * @return the number of bytes that can be read without blocking
    */   
   public int ready() throws IOException {
      if(count < 0) {
         return count;
      }
      if(count > 0) { // if the are ready bytes don't read
         return count;      
      }
      return peek();
   }

   /**
    * Provides the number of bytes that can be read from the stream
    * without blocking. This is typically the number of buffered or
    * available bytes within the stream. When this reaches zero then
    * the source may perform a blocking read.
    *
    * @return the number of bytes that can be read without blocking
    */  
   private int peek() throws IOException {
      if(count <= 0) { // reset the buffer for filling
         buffer.clear();
      }
      if(count > 0) {
         buffer.compact(); // compact the buffer
      }
      count += transport.read(buffer); // how many were read

      if(count > 0) {
         buffer.flip(); // if there is something then flip
      }
      if(count < 0) { // close when stream is fully read
         close();
      }
      return count;
   }

   /**
    * Moves the source backward within the stream. This ensures 
    * that any bytes read from the last read can be pushed back
    * in to the stream so that they can be read again. This will
    * throw an exception if the reset can not be performed.
    *
    * @param size this is the number of bytes to reset back
    *
    * @return this is the number of bytes that have been reset
    */
   public int reset(int size) throws IOException {
      int mark = buffer.position();
      
      if(size > mark) {
         size = mark;
      }
      if(mark > 0) {
         buffer.position(mark - size);
         count += size;
      }
      return size;
   }

   /**
    * This is used to close the underlying transport. This is used
    * when the transport returns a negative value, indicating that
    * the client has closed the connection on the other side. If
    * this is invoked the read method returns -1 and the reader
    * is no longer open, further bytes can no longer be read.
    */
   public void close() throws IOException {
      if(!closed) {
        transport.close();
        closed = true;
        count = -1;
      }
   }
}


