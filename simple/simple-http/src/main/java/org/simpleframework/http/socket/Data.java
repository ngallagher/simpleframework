/*
 * Data.java February 2014
 *
 * Copyright (C) 2014, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.http.socket;

/**
 * The <code>Data</code> interface represents a payload for a WebScoket
 * frame. It can hold either binary data or text data. For performance
 * binary frames are a better choice as all text frames need to be 
 * encoded as UTF-8 from the native UCS2 format.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.DataFrame
 */
public interface Data {
   
   /**
    * This returns the binary payload that is to be sent with a frame.
    * It contains no headers or other meta data. If the original data
    * was text this converts it to UTF-8.
    * 
    * @return the binary payload to be sent with the frame
    */
   byte[] getBinary();
   
   /**
    * This returns the text payload that is to be sent with a frame. 
    * It contains no header information or meta data. Caution should 
    * be used with this method as binary payloads will encode to
    * garbage when decoded as UTF-8.
    * 
    * @return the text payload to be sent with the frame
    */
   String getText();
}
