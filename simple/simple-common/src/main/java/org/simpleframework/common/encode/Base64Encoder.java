/*
 * Base64Encoder.java February 2014
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

/**
 * The <code>Base64Encoder</code> is used to encode and decode base64
 * content. The implementation used here provides a reasonably fast 
 * memory efficient encoder for use with input and output streams. It 
 * is possible to achieve higher performance, however, ease of use
 * and convenience are the priorities with this implementation. This 
 * can only decode complete blocks.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.common.encode.Base64OutputStream
 * @see org.simpleframework.common.encode.Base64InputStream
 */
public class Base64Encoder {

   /**
    * This maintains reference data used to fast decoding.
    */
   private static final int[] REFERENCE = {
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 62,  0,  0,  0, 63, 
   52, 53, 54, 55, 56, 57, 58, 59, 60, 61,  0,  0,  0,  0,  0,  0,  
    0,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 
   15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,  0,  0,  0,  0,  0,  
    0,  26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 
   41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,  0,  0,  0,  0,  0,};
     
   /**
    * This contains the base64 alphabet used for encoding.
    */
   private static final char[] ALPHABET = {
   'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 
   'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 
   'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 
   'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 
   '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/', };
   
   /**
    * This method is used to encode the specified byte array of binary
    * data in to base64 data. The block is complete and must be decoded
    * as a complete block.
    * 
    * @param buf this is the binary data to be encoded
    * 
    * @return this is the base64 encoded value of the data
    */
   public static char[] encode(byte[] buf) {
      return encode(buf, 0, buf.length);
   }

   /**
    * This method is used to encode the specified byte array of binary
    * data in to base64 data. The block is complete and must be decoded
    * as a complete block.
    * 
    * @param buf this is the binary data to be encoded
    * @param off this is the offset to read the binary data from
    * @param len this is the length of data to encode from the array
    * 
    * @return this is the base64 encoded value of the data
    */
   public static char[] encode(byte[] buf, int off, int len) {
      char[] text = new char[((len + 2) / 3) * 4];
      int last = off + len;
      int a = 0;
      int i = 0;

      while (i < last) {
         byte one = buf[i++];
         byte two = (i < len) ? buf[i++] : 0;
         byte three = (i < len) ? buf[i++] : 0;

         int mask = 0x3F;
         text[a++] = ALPHABET[(one >> 2) & mask];
         text[a++] = ALPHABET[((one << 4) | ((two & 0xFF) >> 4)) & mask];
         text[a++] = ALPHABET[((two << 2) | ((three & 0xFF) >> 6)) & mask];
         text[a++] = ALPHABET[three & mask];
      }
      switch (len % 3) {
      case 1:
         text[--a] = '=';
      case 2:
         text[--a] = '=';
      }
      return text;
   }

   /**
    * This is used to decode the provide base64 data back in to an 
    * array of binary data. The data provided here must be a full block
    * of base 64 data in order to be decoded.
    * 
    * @param text this is the base64 text to be decoded
    * 
    * @return this returns the resulting byte array
    */
   public static byte[] decode(char[] text) {
      return decode(text, 0, text.length);
   }

   /**
    * This is used to decode the provide base64 data back in to an 
    * array of binary data. The data provided here must be a full block
    * of base 64 data in order to be decoded.
    * 
    * @param text this is the base64 text to be decoded
    * @param off this is the offset to read the text data from
    * @param len this is the length of data to decode from the text     
    * 
    * @return this returns the resulting byte array
    */
   public static byte[] decode(char[] text, int off, int len) {
      int delta = 0;

      if (text[off + len - 1] == '=') {
         delta = text[off + len - 2] == '=' ? 2 : 1;
      }
      byte[] buf = new byte[len * 3 / 4 - delta];
      int mask = 0xff;
      int index = 0;

      for (int i = 0; i < len; i += 4) {
         int pos = off + i;
         int one = REFERENCE[text[pos]];
         int two = REFERENCE[text[pos + 1]];

         buf[index++] = (byte) (((one << 2) | (two >> 4)) & mask);

         if (index >= buf.length) {
            return buf;
         }
         int three = REFERENCE[text[pos + 2]];

         buf[index++] = (byte) (((two << 4) | (three >> 2)) & mask);

         if (index >= buf.length) {
            return buf;
         }
         int four = REFERENCE[text[pos + 3]];
         buf[index++] = (byte) (((three << 6) | four) & mask);
      }
      return buf;
   }

}