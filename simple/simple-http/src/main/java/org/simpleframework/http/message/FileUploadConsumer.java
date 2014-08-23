/*
 * FileUploadConsumer.java February 2013
 *
 * Copyright (C) 2013, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.http.message;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.transport.ByteCursor;

/**
 * The <code>FileUploadConsumer</code> object is used to consume a
 * list of parts encoded in the multipart format. This is can consume 
 * any number of parts from a cursor. Each part consumed is added to an
 * internal part list which can be used to acquire the contents of the
 * upload and inspect the headers provided for each uploaded part. To
 * ensure that only a fixed number of bytes are consumed this wraps
 * the provided cursor with a counter to ensure reads a limited amount.
 * 
 * @author Niall Gallagher
 */
public class FileUploadConsumer implements BodyConsumer {

   /**
    * This is used to read and parse the contents of the part series.
    */
   private final BodyConsumer consumer;
   
   /**
    * This counts the number of bytes remaining the the part series. 
    */
   private final AtomicLong count;
   
   /**
    * Constructor for the <code>FileUploadConsumer</code> object.
    * This is used to create an object that read a series of parts 
    * from a fixed length body. When consuming the body this will not
    * read any more than the content length from the cursor.
    * 
    * @param allocator this is the allocator used to allocate buffers
    * @param boundary this is the boundary that is used by this
    * @param length this is the number of bytes for this part series
    */
   public FileUploadConsumer(Allocator allocator, byte[] boundary, long length) {
      this.consumer = new PartSeriesConsumer(allocator, boundary, length);
      this.count = new AtomicLong(length);
   }   
   
   /**
    * This is used to acquire the body that has been consumed. This
    * will return a body which can be used to read the content of
    * the message, also if the request is multipart upload then all
    * of the parts are provided as <code>Part</code> objects. 
    * Each part can then be read as an individual message.
    *  
    * @return the body that has been consumed by this instance
    */
   public Body getBody() {
      return consumer.getBody();
   }
   
   /**
    * This method is used to consume bytes from the provided cursor.
    * Consuming of bytes from the cursor should be done in such a
    * way that it does not block. So typically only the number of
    * ready bytes in the <code>ByteCursor</code> object should be 
    * read. If there are no ready bytes then this will return.
    *
    * @param cursor used to consume the bytes from the HTTP pipeline
    */ 
   public void consume(ByteCursor cursor) throws IOException {
      ByteCounter counter = new ByteCounter(cursor);
      
      while(counter.isReady()) {
         if(consumer.isFinished()) {
            break;
         }
         consumer.consume(counter);
      }
   }

   /**
    * This is used to determine whether the consumer has finished 
    * reading. The consumer is considered finished if it has read a
    * terminal token or if it has exhausted the stream and can not
    * read any more. Once finished the consumed bytes can be parsed.
    *
    * @return true if the consumer has finished reading its content
    */ 
   public boolean isFinished() {
      long remaining = count.get();
      
      if(consumer.isFinished()) {
         return true;
      }
      return remaining <= 0;
   }
   
   /**
    * The <code>ByteCounter</code> is a wrapper for a cursor that can
    * be used to restrict the number of bytes consumed. This will
    * count the bytes consumed and ensure that any requested data is
    * restricted to a chunk less than or equal to the remaining bytes.
    */
   private class ByteCounter implements ByteCursor {
      
      /**
       * This is the cursor that this counter will delegate to.
       */
      private final ByteCursor cursor;
      
      /**
       * Constructor for the <code>Counter</code> object. This is used
       * to create a special cursor that counts the bytes read and 
       * limits reads to the remaining bytes left in the part series. 
       * 
       * @param cursor this is the cursor that is delegated to
       */
      public ByteCounter(ByteCursor cursor) {
         this.cursor = cursor;
      }

      /**
       * Determines whether the cursor is still open. The cursor is
       * considered open if there are still bytes to read. If there is
       * still bytes buffered and the underlying transport is closed
       * then the cursor is still considered open. 
       * 
       * @return true if the read method does not return a -1 value
       */
      public boolean isOpen() throws IOException {
         return cursor.isOpen();
      }

      /**
       * Determines whether the cursor is ready for reading. When the
       * cursor is ready then it guarantees that some amount of bytes
       * can be read from the underlying stream without blocking.
       *
       * @return true if some data can be read without blocking
       */ 
      public boolean isReady() throws IOException {
         long limit = count.get();         
         
         if(limit > 0) {
            return cursor.isReady();
         }
         return false;
      }

      /**
       * Provides the number of bytes that can be read from the stream
       * without blocking. This is typically the number of buffered or
       * available bytes within the stream. When this reaches zero then
       * the cursor may perform a blocking read.
       *
       * @return the number of bytes that can be read without blocking
       */
      public int ready() throws IOException {
         int limit = (int)count.get();
         int ready = cursor.ready();
         
         if(ready > limit) {
            return limit;
         }
         return ready;
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
         int limit = (int)count.get();
         int size = Math.min(limit, len);
         int chunk = cursor.read(data, off, size);
         
         if(chunk > 0) {
            count.addAndGet(-chunk);
         }
         return chunk;
      }

      /**
       * Pushes the provided data on to the cursor. Data pushed on to
       * the cursor will be the next data read from the cursor. This
       * complements the <code>reset</code> method which will reset
       * the cursors position on a stream. Allowing data to be pushed
       * on to the cursor allows more flexibility.
       * 
       * @param data this is the data to be pushed on to the cursor
       */
      public void push(byte[] data) throws IOException {
         push(data, 0, data.length);
      }

      /**
       * Pushes the provided data on to the cursor. Data pushed on to
       * the cursor will be the next data read from the cursor. This
       * complements the <code>reset</code> method which will reset
       * the cursors position on a stream. Allowing data to be pushed
       * on to the cursor allows more flexibility.
       * 
       * @param data this is the data to be pushed on to the cursor
       * @param off this is the offset to begin reading the bytes
       * @param len this is the number of bytes that are to be used 
       */
      public void push(byte[] data, int off, int len) throws IOException {
         if(len > 0) {
            count.addAndGet(len);
         }
         cursor.push(data, off, len);
      }

      /**
       * Moves the cursor backward within the stream. This ensures 
       * that any bytes read from the last read can be pushed back
       * in to the stream so that they can be read again. This will
       * throw an exception if the reset can not be performed.
       *
       * @param len this is the number of bytes to reset back
       *
       * @return this is the number of bytes that have been reset
       */
      public int reset(int len) throws IOException {
         int reset = cursor.reset(len);
         
         if(reset > 0) {
            count.addAndGet(reset);
         }
         return reset;
      }  
   }
}
