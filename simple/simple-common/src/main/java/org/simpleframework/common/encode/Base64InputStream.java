/*
 * Base64InputStream.java February 2014
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

package org.simpleframework.common.encode;

import java.io.IOException;
import java.io.InputStream;

/**
 * The <code>Base64InputStream</code> is used to read base64 text in
 * the form of a string through a conventional input stream. This is
 * provided for convenience so that it is possible to encode and
 * decode binary data as base64 for implementations that would 
 * normally use a binary format.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.common.encode.Base64Encoder
 */
public class Base64InputStream extends InputStream {

   /**
    * This is that original base64 text that is to be decoded.
    */
   private char[] encoded;
   
   /**
    * This is used to accumulate the decoded text as an array.
    */
   private byte[] decoded;
   
   /**
    * This is a temporary buffer used to read one byte at a time.
    */
   private byte[] temp;
   
   /**
    * This is the total number of bytes that have been read.
    */
   private int count;

   /**
    * Constructor for the <code>Base64InputStream</code> object.
    * This takes an encoded string and reads it as binary data.
    * 
    * @param source this string containing the encoded data
    */
   public Base64InputStream(String source) {
      this.encoded = source.toCharArray();
      this.temp = new byte[1];
   }

   /**
    * This is used to read the next byte decoded from the text. If 
    * the data has been fully consumed then this will return the
    * standard -1. 
    * 
    * @return this returns the next octet decoded
    */
   @Override
   public int read() throws IOException {
      int count = read(temp);

      if (count == -1) {
         return -1;
      }
      return temp[0] & 0xff;
   }

   /**
    * This is used to read the next byte decoded from the text. If 
    * the data has been fully consumed then this will return the
    * standard -1. 
    * 
    * @param array this is the array to decode the text to
    * @param offset this is the offset to decode in to the array
    * @param this is the number of bytes available to decode to
    * 
    * @return this returns the number of octets decoded
    */
   @Override
   public int read(byte[] array, int offset, int length) throws IOException {
      if (decoded == null) {
         decoded = Base64Encoder.decode(encoded);
      }
      if (count >= decoded.length) {
         return -1;
      }
      int size = Math.min(length, decoded.length - count);

      if (size > 0) {
         System.arraycopy(decoded, count, array, offset, size);
         count += size;
      }
      return size;
   }

   /**
    * This returns the original base64 text that was encoded. This
    * is useful for debugging purposes to see the source data.
    * 
    * @return this returns the original base64 text to decode
    */
   @Override
   public String toString() {
      return new String(encoded);
   }
}
