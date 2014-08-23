/*
 * Base64OutputStream.java February 2014
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
import java.io.OutputStream;
import java.util.Arrays;

/**
 * The <code>Base64OutputStream</code> is used to write base64 text 
 * in the form of a string through a conventional output stream. This 
 * is provided for convenience so that it is possible to encode and
 * decode binary data as base64 for implementations that would 
 * normally use a binary format.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.common.encode.Base64Encoder
 */
public class Base64OutputStream extends OutputStream {

   private char[] encoded;
   private byte[] buffer;
   private byte[] temp;
   private int count;

   /**
    * Constructor for the <code>Base64OutputStream</code> object. A
    * stream created with this constructor uses an initial capacity
    * of one kilobyte, the capacity is increased as bytes are written.
    */
   public Base64OutputStream() {
      this(1024);
   }

   /**
    * Constructor for the <code>Base64OutputStream</code> object. A
    * stream created with this constructor can have an initial capacity
    * specified. Typically it is a good rule of thumb to use a capacity
    * that is just over an additional third of the source binary data.
    * 
    * @param capacity this is the initial capacity of the buffer
    */
   public Base64OutputStream(int capacity) {
      this.buffer = new byte[capacity];
      this.temp = new byte[1];
   }

   /**
    * This method is used to write data as base64 to an internal buffer.
    * The <code>toString</code> method can be used to acquire the text
    * encoded from the written binary data.
    * 
    * @param octet the octet to encode in to the internal buffer
    */
   @Override
   public void write(int octet) throws IOException {
      temp[0] = (byte) octet;
      write(temp);
   }

   /**
    * This method is used to write data as base64 to an internal buffer.
    * The <code>toString</code> method can be used to acquire the text
    * encoded from the written binary data.     
    * 
    * @param array the octets to encode to the internal buffer
    * @param offset this is the offset in the array to encode from
    * @param length this is the number of bytes to be encoded
    */
   @Override
   public void write(byte[] array, int offset, int length) throws IOException {
      if (encoded != null) {
         throw new IOException("Stream has been closed");
      }
      if (count + length > buffer.length) {
         expand(count + length);
      }
      System.arraycopy(array, offset, buffer, count, length);
      count += length;
   }

   /**
    * This will expand the size of the internal buffer. To allow for 
    * a variable length number of bytes to be written the internal
    * buffer can grow as demand exceeds space available.
    * 
    * @param capacity this is the minimum capacity required
    */
   private void expand(int capacity) throws IOException {
      int length = Math.max(buffer.length * 2, capacity);

      if (buffer.length < capacity) {
         buffer = Arrays.copyOf(buffer, length);
      }
   }

   /**
    * This is used to close the stream and encode the buffered bytes
    * to base64. Once this method is invoked no further data can be
    * encoded with the stream. The <code>toString</code> method can
    * be used to acquire the base64 encoded text. 
    */
   @Override
   public void close() throws IOException {
      if (encoded == null) {
         encoded = Base64Encoder.encode(buffer, 0, count);
      }
   }

   /**
    * This returns the base64 text encoded from the bytes written to
    * the stream. This is the primary means for acquiring the base64
    * encoded text once the stream has been closed.
    * 
    * @return this returns the base64 text encoded
    */
   @Override
   public String toString() {
      return new String(encoded);
   }
}
