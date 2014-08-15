/*
 * EmptyInputStream.java October 2002
 *
 * Copyright (C) 2002, Niall Gallagher <niallg@users.sf.net>
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

import java.io.InputStream;

/**
 * The <code>EmptyInputStream</code> object provides a stream that 
 * is immediately empty. Each read method with this input stream 
 * will return a -1 value indicating that the stream has come to an
 * end and no more data can be read from it.
 * 
 * @author Niall Gallagher
 */
class EmptyInputStream extends InputStream {
   
   /**
    * This is used to provide a -1 value when an attempt is made to
    * read from the stream. Implementing this method as so also 
    * ensures that all the other read methods return a -1 value.
    * 
    * @return this returns a -1 when an attempt is made to read
    */
   public int read() {
      return -1;
   }

}
