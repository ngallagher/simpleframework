/*
 * BufferRecycler.java February 2008
 *
 * Copyright (C) 2008, Niall Gallagher <niallg@users.sf.net>
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

import java.nio.ByteBuffer;

/**
 * The <code>BufferRecycler</code> interface is used to represent a 
 * pool of buffers that accepts used instances for recycling. This 
 * allows buffers to be passed back in to a pool implementation when 
 * the buffer has been used. Such occasions are when packets close.
 * 
 * @author Niall Gallagher
 */
interface BufferRecycler {
   
   /**
    * This method is used to recycle the buffer. Invoking this with
    * a buffer instance will pass the buffer back in to the pool.
    * Once passed back in to the pool the buffer should no longer
    * be used as it may affect future uses of the buffer.
    *
    * @param buffer this is the buffer that is to be recycled
    */
   void recycle(ByteBuffer buffer);
}
