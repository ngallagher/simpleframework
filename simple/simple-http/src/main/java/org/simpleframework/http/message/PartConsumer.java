/*
 * PartConsumer.java February 2007
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
 * The <code>PartConsumer</code> object is used to consume a part 
 * from a part list. A part consists of a header and a body, which
 * can be either a simple chunk of data or another part list. This
 * must be able to cope with either a simple body or a part list.
 *  
 * @author Niall Gallagher
 */ 
class PartConsumer implements ByteConsumer {
 
   /**
    * This is used to consume the header message of the part.
    */  
   private SegmentConsumer header;
   
   /**
    * This is used to consume the body data from the part.
    */ 
   private BodyConsumer body;
   
   /**
    * This is used to determine what type the body data is.
    */ 
   private PartFactory factory;   
   
   /**
    * This is used to add the consumed parts to when finished.
    */  
   private PartSeries series;   
   
   /**
    * This is the current consumer used to read from the cursor.
    */ 
   private ByteConsumer current;
   
   /**
    * This is the terminal token that ends the part payload.
    */ 
   private byte[] terminal;
   
   /**
    * Constructor for the <code>PartConsumer</code> object. This is
    * used to create a consumer used to read the contents of a part
    * and the boundary that terminates the content. Any parts that
    * are created by this are added to the provided part list.
    *
    * @param allocator this is the allocator used to creat buffers
    * @param series this is the part list used to store the parts
    * @param terminal this is the terminal token for the part
    * @param length this is the length of the parent part series    
    */
   public PartConsumer(Allocator allocator, PartSeries series, byte[] terminal, long length) {
      this.header = new PartHeaderConsumer(allocator);
      this.factory = new PartFactory(allocator, header, length);      
      this.terminal = terminal;
      this.current = header;
      this.series = series;
   }
 
   /**
    * This is used to create a new body consumer used to consume the
    * part body from for the list. This will ensure that the part 
    * data is created based on the part header consumed. The types 
    * of part supported are part lists and part body.
    *
    * @return this returns a consumed for the part content
    */   
   private BodyConsumer getConsumer() {
      return factory.getInstance(series, terminal);
   }
   
   /** 
    * This is used to consume the part body from the cursor. This
    * initially reads the body of the part, which represents the
    * actual payload exposed via the <code>Part</code> interface
    * once the payload has been consumed the terminal is consumed.
    *
    * @param cursor this is the cursor to consume the body from
    */    
   public void consume(ByteCursor cursor) throws IOException {
      while(cursor.isReady()) {        
         if(header.isFinished()) {
            if(body == null) {
               body = getConsumer();
               current = body;
            } else {
               if(body.isFinished())             
                  break;               
            }
         }
         current.consume(cursor);
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
      if(body != null) {
         return body.isFinished();
      }
      return false;
   }
}

