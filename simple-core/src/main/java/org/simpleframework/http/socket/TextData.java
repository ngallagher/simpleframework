/*
 * TextData.java February 2014
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
 * The <code>TextData</code> object represents a text payload for 
 * a WebScoket frame. This can be used to send any type of data. If 
 * however it is used to send binary data then it is encoded as UTF-8.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.DataFrame
 */
public class TextData implements Data {

   /**
    * This is used to convert the text payload to a byte array.
    */
   private final DataConverter converter;
   
   /**
    * This is the text string representing a frame payload.
    */
   private final String data;
   
   /**
    * Constructor for the <code>TextData</code> object. It requires
    * an text string that will be sent as UTF-8  within a frame. 
    * 
    * @param data the text string representing the frame payload
    */
   public TextData(String data) {
      this.converter = new DataConverter();
      this.data = data;
   }

   /**
    * This returns the binary payload that is to be sent with a frame.
    * It contains no headers or other meta data. If the original data
    * was text this converts it to UTF-8.
    * 
    * @return the binary payload to be sent with the frame
    */
   public byte[] getBinary() {
      return converter.convert(data);
   }

   /**
    * This returns the text payload that is to be sent with a frame. 
    * It contains no header information or meta data. Caution should 
    * be used with this method as binary payloads will encode to
    * garbage when decoded as UTF-8.
    * 
    * @return the text payload to be sent with the frame
    */
   public String getText() {
      return data;
   }
}
