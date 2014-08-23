/*
 * PartEntryFactory.java February 2007
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

import org.simpleframework.common.buffer.Allocator;

/**
 * This <code>PartEntryFactory</code> object provides a factory for
 * creating part entry consumers. The part entry consumers created
 * read individual entries from a list of parts within a stream. 
 * This is basically a convenience factory for the list consumer.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.message.PartSeriesConsumer
 */
class PartEntryFactory {   

   /**
    * This is used to accumulate all the parts of the upload.
    */ 
   private final PartSeries series;   
   
   /**
    * This is used to allocate the buffers used by the entry.
    */ 
   private final Allocator allocator;
   
   /**
    * This is the terminal token used to delimiter the upload.
    */ 
   private final byte[] terminal;
   
   /**
    * This is the length of the parent part series body.
    */
   private final long length;
   
   /**
    * Constructor for the <code>PartEntryFactory</code> object.
    * This is used to create a factory for entry consumers that
    * can be used to read an entry from a part list.
    * 
    * @param allocator this is the allocator used for buffers
    * @param series this is the list of parts that are extracted
    * @param terminal this is the terminal buffer to be used
    * @param length this is the length of the parent part series     
    */
   public PartEntryFactory(Allocator allocator, PartSeries series, byte[] terminal, long length) {
      this.allocator = allocator;
      this.terminal = terminal;
      this.series = series;
      this.length = length;
   }
   
   
   /**
    * This creates a new part entry consumer that can be used to
    * read the next part from the list. The consumer instantiated
    * by this factory acquires the allocator, list and boundary 
    * from the enclosing part list consumer instance.
    * 
    * @return a part entry consumer for this part list consumer 
    */
   public PartEntryConsumer getInstance() {
      return new PartEntryConsumer(allocator, series, terminal, length);
   }
}