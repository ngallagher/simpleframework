/*
 * Cleaner.java May 2004
 *
 * Copyright (C) 2004, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.common.lease;

/**
 * The <code>Cleaner</code> represents an object that is used to
 * clean up after the keyed resource. Typically this is used when
 * a <code>Lease</code> referring a resource has expired meaning
 * that any memory, file descriptors, or other such limited data
 * should be released for the keyed resource. The resource keys
 * used should be distinct over time to avoid conflicts.
 *
 * @author Niall Gallagher
 *
 * @see org.simpleframework.common.lease.Lease
 */
public interface Cleaner<T> {

   /**
    * This method is used to clean up after a the keyed resource.
    * To ensure that the leasing infrastructure operates properly
    * this should not block releasing resources. If required this
    * should spawn a thread to perform time consuming tasks.    
    *
    * @param key this is the key for the resource to clean
    */
   void clean(T key) throws Exception;
}
