/*
 * BodyConsumer.java February 2007
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

package org.simpleframework.http.message;

/**
 * The <code>BodyConsumer</code> is used to consume the body of an
 * HTTP message. Implementations of this consumer must provide the
 * <code>Body</code> that has been consumed. If there is no body
 * associated with the consumer then an empty body is returned.
 *
 * @author Niall Gallagher
 */
public interface BodyConsumer extends ByteConsumer {
   
   /**
    * This is used to acquire the body that has been consumed. This
    * will return a body which can be used to read the content of
    * the message, also if the request is multipart upload then all
    * of the parts are provided as <code>Part</code> objects. 
    * Each part can then be read as an individual message.
    *  
    * @return the body that has been consumed by this instance
    */
   Body getBody(); 
}


