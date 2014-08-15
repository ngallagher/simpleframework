/*
 * DataConverter.java February 2014
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
 * The <code>DataConverter</code> object is used to convert binary data
 * to text data and vice versa. According to RFC 6455 a particular text 
 * frame might include a partial UTF-8 sequence; however, the whole 
 * message MUST contain valid UTF-8.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.DataFrame
 */
public class DataConverter {
   
   /**
    * This is the character encoding used to convert the text data.
    */
   private final String charset;

   /**
    * Constructor for the <code>DataConverter</code> object. By default
    * this uses UTF-8 character encoding to convert text data as this
    * is what is required for RFC 6455 section 5.6.
    */
   public DataConverter() {
      this("UTF-8");
   }
   
   /**
    * Constructor for the <code>DataConverter</code> object. This can be
    * used to specific a character encoding other than UTF-8. However it
    * is not recommended as RFC 6455 section 5.6 suggests the frame must
    * contain valid UTF-8 data.
    * 
    * @param charset the character encoding to be used
    */
   public DataConverter(String charset) {
      this.charset = charset;
   }

   /**
    * This method is used to convert text using the character encoding
    * specified when constructing the converter. Typically this will use
    * UTF-8 as required by RFC 6455.
    * 
    * @param text this is the string to convert to a byte array
    * 
    * @return a byte array decoded using the specified encoding
    */
   public byte[] convert(String text) {
      try {
         return text.getBytes(charset);
      } catch(Exception e) {
         throw new IllegalStateException("Could not encode text as " + charset, e);
      }  
   }
   
   /**
    * This method is used to convert data using the character encoding
    * specified when constructing the converter. Typically this will use
    * UTF-8 as required by RFC 6455.
    * 
    * @param text this is the byte array to convert to a string
    * 
    * @return a string encoded using the specified encoding
    */
   public String convert(byte[] binary) {
      try {
         return new String(binary, charset);
      } catch(Exception e) {
         throw new IllegalStateException("Could not decode data as " + charset, e);
      }
   }
   
   /**
    * This method is used to convert data using the character encoding
    * specified when constructing the converter. Typically this will use
    * UTF-8 as required by RFC 6455.
    * 
    * @param text this is the byte array to convert to a string
    * @param offset the is the offset to read the bytes from
    * @param size this is the number of bytes to be used
    * 
    * @return a string encoded using the specified encoding
    */
   public String convert(byte[] binary, int offset, int size) {
      try {
         return new String(binary, offset, size, charset);
      } catch(Exception e) {
         throw new IllegalStateException("Could not decode data as " + charset, e);
      }
   }
}
