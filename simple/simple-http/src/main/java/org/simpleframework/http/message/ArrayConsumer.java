/*
 * ArrayConsumer.java February 2007
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
 * The <code>ArrayConsumer</code> object is a consumer that consumes 
 * bytes in to an internal array before processing. This consumes
 * all bytes read in to an internal array. Each read is met with an
 * invocation of the <code>scan</code> method, which searches for
 * the terminal token within the read chunk. Once the terminal token
 * has been read the excess bytes are reset and the data can be
 * processed by the subclass implementation. The internal array is
 * expanded if the number of consumed bytes exceeds its capacity.
 *
 * @author Niall Gallagher
 */ 
public abstract class ArrayConsumer implements ByteConsumer {

   /**
    * This is the array that is used to contain the read bytes.
    */         
   protected byte[] array;

   /**
    * This is the number of bytes that have been consumed so far.
    */ 
   protected int count;
   
   /**
    * This is the size of the chunk of bytes to read each time.
    */ 
   protected int chunk;
   
   /**
    * This determines whether the terminal token has been read.
    */
   protected boolean done;

   /**
    * Constructor for the <code>ArrayConsumer</code> object. This is
    * used to create a consumer that will consume all bytes in to an
    * internal array until a terminal token has been read. If excess
    * bytes are read by this consumer they are reset in the cursor.   
    */ 
   public ArrayConsumer() {
      this(1024);
   }        

   /**
    * Constructor for the <code>ArrayConsumer</code> object. This is
    * used to create a consumer that will consume all bytes in to an
    * internal array until a terminal token has been read. If excess
    * bytes are read by this consumer they are reset in the cursor.   
    *
    * @param size this is the initial array and chunk size to use
    */   
   public ArrayConsumer(int size) {
      this(size, 512);
   }
   
   /**
    * Constructor for the <code>ArrayConsumer</code> object. This is
    * used to create a consumer that will consume all bytes in to an
    * internal array until a terminal token has been read. If excess
    * bytes are read by this consumer they are reset in the cursor.   
    *
    * @param size this is the initial array size that is to be used
    * @param chunk this is the chunk size to read bytes as
    */   
   public ArrayConsumer(int size, int chunk) {
      this.array = new byte[size];
      this.chunk = chunk;
   }
   
   /**
    * This method is used to consume bytes from the provided cursor.
    * Each read performed is done in a specific chunk size to ensure
    * that a sufficiently large or small amount of data is read from
    * the <code>ByteCursor</code> object. After each read the byte 
    * array is scanned for the terminal token. When the terminal 
    * token is found the bytes are processed by the implementation.
    *
    * @param cursor this is the cursor to consume the bytes from
    */ 
   public void consume(ByteCursor cursor) throws IOException {
      if(!done) {
         int ready = cursor.ready();
         
         while(ready > 0) {
            int size = Math.min(ready, chunk);
         
            if(count + size > array.length) {
               resize(count + size);
            }
            size = cursor.read(array, count, size);
            count += size;
            
            if(size > 0) {
               int reset = scan();
               
               if(reset > 0) {
                  cursor.reset(reset);
               }
               if(done) {
                  process(); 
                  break;
               }
            }         
            ready = cursor.ready();         
         }
      }
   }
   
   /**
    * This method is used to add an additional chunk size to the 
    * internal array. Resizing of the internal array is required as
    * the consumed bytes may exceed the initial size of the array.
    * In such a scenario the array is expanded the chunk size.
    *
    * @param size this is the minimum size to expand the array to 
    */ 
   protected void resize(int size) throws IOException {
      if(array.length < size) {
         int expand = array.length + chunk;
         int max = Math.max(expand, size);
         byte[] temp = new byte[max];
         
         System.arraycopy(array, 0, temp, 0, count); 
         array = temp;
      }
   } 
   
   /**
    * When the terminal token is read from the cursor this will be
    * true. The <code>scan</code> method is used to determine the
    * terminal token. It is invoked after each read, when the scan
    * method returns a non-zero value then excess bytes are reset
    * and the consumer has finished.
    *
    * @return this returns true when the terminal token is read
    */ 
   public boolean isFinished() {
      return done;
   }
  
   /**
    * This method is invoked after the terminal token has been read.
    * It is used to process the consumed data and is typically used to
    * parse the input such that it can be used by the subclass for
    * some useful purpose. This is called only once by the consumer.
    */  
   protected abstract void process() throws IOException;
   
   /**
    * This method is used to scan for the terminal token. It searches
    * for the token and returns the number of bytes in the buffer 
    * after the terminal token. Returning the excess bytes allows the
    * consumer to reset the bytes within the consumer object.
    *
    * @return this returns the number of excess bytes consumed
    */ 
   protected abstract int scan() throws IOException;

}
