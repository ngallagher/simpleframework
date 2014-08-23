/*
 * ByteConsumer.java February 2007
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

import java.io.IOException;

import org.simpleframework.transport.ByteCursor;

/**
 * The <code>ByteConsumer</code> object is used to consume and process 
 * bytes from a cursor. This is used to consume bytes from a pipeline
 * and process the content in order to produce a valid HTTP message.
 * Using a consumer allows the server to gather and process the data
 * from the stream bit by bit without blocking.
 * <p>
 * A consumer has completed its task when it has either exhausted its
 * stream, or when it has consume a terminal token. For instance a 
 * consumer for a HTTP header will have two <code>CRLF</code> bytes
 * tokens to identify the end of the header, once this has been read
 * any excess bytes are reset on the cursor and it has finished.
 *
 * @author Niall Gallagher
 *
 * @see org.simpleframework.transport.ByteCursor
 */ 
public interface ByteConsumer {

   /**
    * This method is used to consume bytes from the provided cursor.
    * Consuming of bytes from the cursor should be done in such a
    * way that it does not block. So typically only the number of
    * ready bytes in the <code>ByteCursor</code> object should be 
    * read. If there are no ready bytes then this method return.
    *
    * @param cursor used to consume the bytes from the cursor
    */ 
   void consume(ByteCursor cursor) throws IOException;
   
   /**
    * This is used to determine whether the consumer has finished 
    * reading. The consumer is considered finished if it has read a
    * terminal token or if it has exhausted the stream and can not
    * read any more. Once finished the consumed bytes can be parsed.
    *
    * @return true if the consumer has finished reading its content
    */ 
   boolean isFinished();
}
