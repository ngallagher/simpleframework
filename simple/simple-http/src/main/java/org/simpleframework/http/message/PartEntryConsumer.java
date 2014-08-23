/*
 * PartEntryConsumer.java February 2007
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

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.transport.ByteCursor;

/**
 * The <code>PartEntryConsumer</code> object is used to consume each
 * part from the part list. This is combines the task of consuming
 * the part, which consists of a header and a body, and a boundary
 * which identifies the end of the message content.
 *
 * @author Niall Gallagher
 */  
class PartEntryConsumer implements ByteConsumer {
   
   /**
    * This is used to consume the boundary at the end of a part.
    */            
   private final BoundaryConsumer boundary;

   /**
    * This is used to consume the actual part from the list.
    */  
   private final ByteConsumer consumer;
   
   /**
    * Constructor for the <code>PartEntryConsumer</code> object. This 
    * is used to create a consumer that will read the message part
    * and the boundary that terminates the part. All contents that
    * are read are appended to an internal buffer.
    *
    * @param allocator this is the allocator used for the buffer      
    * @param series this is the list used to accumulate the parts
    * @param terminal this is the terminal token for the part list 
    * @param length this is the length of the parent part series
    */       
   public PartEntryConsumer(Allocator allocator, PartSeries series, byte[] terminal, long length) {
      this.consumer = new PartConsumer(allocator, series, terminal, length); 
      this.boundary = new BoundaryConsumer(allocator, terminal);        
   }
   
   /** 
    * This is used to consume the part body from the cursor. This
    * initially reads the body of the part, which represents the
    * actual content exposed via the <code>Part</code> interface
    * once the content has been consumed the terminal is consumed.
    *
    * @param cursor this is the cursor to consume the body from
    */       
   public void consume(ByteCursor cursor) throws IOException {
      while(cursor.isReady()) {
         if(!boundary.isFinished()) {
            boundary.consume(cursor);
         } else {               
            if(consumer.isFinished()) {
               break;
            }
            if(boundary.isEnd()) {
               break;
            }
            consumer.consume(cursor);
         }
      }
   }
   
   /**
    * This is used to determine whether the part body has been read
    * from the cursor successfully. In order to determine if all of
    * the bytes have been read successfully this will check to see
    * of the terminal token had been consumed.
    *
    * @return true if the part body and terminal have been read 
    */       
   public boolean isFinished() {
      if(boundary.isEnd()) {
         return true;
      }
      return consumer.isFinished();
   }

   /**
    * This is used to determine whether the terminal token read is
    * the final terminal token. The final terminal token is a 
    * normal terminal token, however it ends with two hyphens and
    * a carriage return line feed, this ends the part list.
    *
    * @return true if this was the last part within the list
    */      
   public boolean isEnd() {
      return boundary.isEnd();
   }
}