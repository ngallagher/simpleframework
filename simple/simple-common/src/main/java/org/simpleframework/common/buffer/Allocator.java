/*
 * Allocator.java February 2001
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

package org.simpleframework.common.buffer;

import java.io.IOException;

/**
 * The <code>Allocator</code> interface is used to describe a resource
 * that can allocate a buffer. This is used so that memory allocation
 * can be implemented as a strategy allowing many different sources of
 * memory. Typically memory will be allocated as an array of bytes but
 * can be a mapped region of shared memory or a file.
 *
 * @author Niall Gallagher
 */ 
public interface Allocator {
 
   /**
    * This method is used to allocate a default buffer. Typically this
    * will allocate a buffer of predetermined size, allowing it to 
    * grow to an upper limit to accommodate extra data. If the buffer
    * can not be allocated for some reason this throws an exception.
    *
    * @return this returns an allocated buffer with a default size
    */         
   Buffer allocate() throws IOException;

   /**
    * This method is used to allocate a default buffer. This is used
    * to allocate a buffer of the specified size, allowing it to 
    * grow to an upper limit to accommodate extra data. If the buffer
    * can not be allocated for some reason this throws an exception.
    *
    * @param size this is the initial capacity the buffer should have
    *
    * @return this returns an allocated buffer with a specified size
    */
   Buffer allocate(long size) throws IOException;
}
