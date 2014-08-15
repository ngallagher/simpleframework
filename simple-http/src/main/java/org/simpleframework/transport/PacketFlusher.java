/*
 * PacketFlusher.java February 2007
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
 * The <code>PacketFlusher</code> object is used to flush data to 
 * the underlying socket. This allows asynchronous writes to the 
 * socket to be managed in such a way that there is order to the 
 * way data is delivered over the socket. This uses a selector to 
 * dispatch flush invocations to the underlying socket when the 
 * socket is read ready. This allows the writing thread to continue 
 * without having to wait for all the data to be written.
 *
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.transport.PacketController
 */ 
interface PacketFlusher {

  /**
   * Here in this method we schedule a flush when the underlying
   * writer is write ready. This allows the writer thread to return
   * without having to fully flush the content to the underlying
   * transport. This will block if references are queued.
   */          
  void flush() throws IOException;
  
  /**
   * This is used to close the flusher ensuring that all of the
   * data within the writer will be flushed regardless of the 
   * amount of data within the writer that needs to be written. If
   * the writer does not block then this waits to be finished.
   */
  void close() throws IOException;
}