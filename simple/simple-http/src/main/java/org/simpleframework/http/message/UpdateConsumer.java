/*
 * UpdateConsumer.java February 2007
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

package org.simpleframework.http.message;

import java.io.IOException;

import org.simpleframework.transport.ByteCursor;

/**
 * The <code>UpdateConsumer</code> object is used to create a consumer
 * that is used to consume and process large bodies. Typically a large
 * body will be one that is delivered as part of a multipart upload
 * or as a large form POST. The task of the large consumer is to
 * consume all the bytes for the body, and reset the cursor after the
 * last byte that has been send with the body. This ensures that the
 * next character read from the cursor is the first character of a
 * HTTP header within the pipeline.
 *
 * @author Niall Gallagher
 */ 
public abstract class UpdateConsumer implements BodyConsumer { 

   /**
    * This is an external array used to copy data between buffers.
    */        
   protected byte[] array;
   
   /**
    * This is used to determine whether the consumer has finished.
    */ 
   protected boolean finished;
  
   /**
    * Constructor for the <code>UpdateConsumer</code> object. This is
    * used to create a consumer with a one kilobyte buffer used to
    * read the contents from the cursor and transfer it to the buffer.
    */  
   protected UpdateConsumer() {
      this(2048);
   }
   
   /**
    * Constructor for the <code>UpdateConsumer</code> object. This is
    * used to create a consumer with a variable size buffer used to
    * read the contents from the cursor and transfer it to the buffer.
    *
    * @param chunk this is the size of the buffer used to read bytes
    */     
   protected UpdateConsumer(int chunk) {
      this.array = new byte[chunk];
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
      return finished;
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
      int ready = cursor.ready();      
      
      while(ready > 0) {
         int size = Math.min(ready, array.length);
         int count = cursor.read(array, 0, size);        

         if(count > 0) {
            int reset = update(array, 0, count);            
            
            if(reset > 0) {
               cursor.reset(reset);
            }
         }
         if(finished) {
            commit(cursor);
        	   break;
         }
         ready = cursor.ready();        	 
      }
   } 
   
   /**
    * This method can be used to commit the consumer when all data 
    * has been consumed. It is often used to push back some data on
    * to the cursor so that the next consumer can read valid tokens
    * from the stream of bytes. If no commit is required then the
    * default implementation of this will simply return quietly.
    * 
    * @param cursor this is the cursor used by this consumer
    */
   protected void commit(ByteCursor cursor) throws IOException {
      if(!finished) {
         throw new IOException("Consumer not finished");
      }
   }
  
   /**
    * This is used to process the bytes that have been read from the
    * cursor. Depending on the delimiter used this knows when the
    * end of the body has been encountered. If the end is encountered
    * this method must return the number of bytes overflow, and set
    * the state of the consumer to finished.
    *
    * @param array this is a chunk read from the cursor
    * @param off this is the offset within the array the chunk starts
    * @param count this is the number of bytes within the array
    *
    * @return this returns the number of bytes overflow that is read
    */    
   protected abstract int update(byte[] array, int off, int count) throws IOException;
}


