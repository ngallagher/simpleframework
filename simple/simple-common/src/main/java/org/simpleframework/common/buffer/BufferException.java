/*
 * BufferException.java February 2001
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
 * The <code>BufferException</code> is used to report problems that
 * can occur during the use or allocation of a buffer. Typically
 * this is thrown if the upper capacity limit is exceeded.
 *
 * @author Niall Gallagher
 */ 
public class BufferException extends IOException {

  /**
   * Constructor for the <code>BufferException</code> object. The
   * exception can be provided with a message describing the issue
   * that has arisen in the use or allocation of the buffer.
   *
   * @param format this is the template for the exception
   * @param values these are the values to be added to the template
   */         
  public BufferException(String format, Object... values) {
    super(String.format(format, values));          
  }        
}
