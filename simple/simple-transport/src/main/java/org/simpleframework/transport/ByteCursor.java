/*
 * ByteCursor.java February 2007
 *
 * Copyright (C) 2007, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.transport;

import java.io.IOException;

/**
 * The <code>ByteCursor</code> object is used to acquire bytes from a 
 * given source. This provides a cursor style reading of bytes from 
 * a stream in that it will allow the reader to move the cursor back
 * if the amount of bytes read is too much. Allowing the cursor to 
 * move ensures that excess bytes back be placed back in the stream.
 * <p>
 * This is used when parsing input from a stream as it ensures that
 * on arrival at a terminal token any excess bytes can be placed 
 * back in to the stream. This allows data to be read efficiently
 * in large chunks from blocking streams such as sockets.
 *
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.TransportCursor
 */ 
public interface ByteCursor {
 
   /**
    * Determines whether the cursor is still open. The cursor is
    * considered open if there are still bytes to read. If there is
    * still bytes buffered and the underlying transport is closed
    * then the cursor is still considered open. 
    * 
    * @return true if the read method does not return a -1 value
    */
   boolean isOpen() throws IOException;
   
   /**
    * Determines whether the cursor is ready for reading. When the
    * cursor is ready then it guarantees that some amount of bytes
    * can be read from the underlying stream without blocking.
    *
    * @return true if some data can be read without blocking
    */         
   boolean isReady() throws IOException;

   /**
    * Provides the number of bytes that can be read from the stream
    * without blocking. This is typically the number of buffered or
    * available bytes within the stream. When this reaches zero then
    * the cursor may perform a blocking read.
    *
    * @return the number of bytes that can be read without blocking
    */ 
   int ready() throws IOException;

   /**
    * Reads a block of bytes from the underlying stream. This will
    * read up to the requested number of bytes from the underlying
    * stream. If there are no ready bytes on the stream this can 
    * return zero, representing the fact that nothing was read.
    *
    * @param data this is the array to read the bytes in to 
    *
    * @return this returns the number of bytes read from the stream 
    */ 
   int read(byte[] data) throws IOException;

   /**
    * Reads a block of bytes from the underlying stream. This will
    * read up to the requested number of bytes from the underlying
    * stream. If there are no ready bytes on the stream this can 
    * return zero, representing the fact that nothing was read.
    *
    * @param data this is the array to read the bytes in to
    * @param off this is the offset to begin writing the bytes to
    * @param len this is the number of bytes that are requested 
    *
    * @return this returns the number of bytes read from the stream 
    */ 
   int read(byte[] data, int off, int len) throws IOException;
   
   /**
    * Pushes the provided data on to the cursor. Data pushed on to
    * the cursor will be the next data read from the cursor. This
    * complements the <code>reset</code> method which will reset
    * the cursors position on a stream. Allowing data to be pushed
    * on to the cursor allows more flexibility.
    * 
    * @param data this is the data to be pushed on to the cursor
    */
   void push(byte[] data) throws IOException;
   
   /**
    * Pushes the provided data on to the cursor. Data pushed on to
    * the cursor will be the next data read from the cursor. This
    * complements the <code>reset</code> method which will reset
    * the cursors position on a stream. Allowing data to be pushed
    * on to the cursor allows more flexibility.
    * 
    * @param data this is the data to be pushed on to the cursor
    * @param off this is the offset to begin reading the bytes
    * @param len this is the number of bytes that are to be used 
    */
   void push(byte[] data, int off, int len) throws IOException;

   /**
    * Moves the cursor backward within the stream. This ensures 
    * that any bytes read from the last read can be pushed back
    * in to the stream so that they can be read again. This will
    * throw an exception if the reset can not be performed.
    *
    * @param len this is the number of bytes to reset back
    *
    * @return this is the number of bytes that have been reset
    */
   int reset(int len) throws IOException;   
}
