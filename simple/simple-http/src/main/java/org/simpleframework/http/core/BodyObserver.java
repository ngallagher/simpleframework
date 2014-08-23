/*
 * BodyObserver.java February 2007
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

package org.simpleframework.http.core;

import org.simpleframework.transport.ByteWriter;

/**
 * The <code>BodyObserver</code> object is core to how the requests 
 * are processed from a pipeline. This observes the progress of the
 * response streams as they are written to the underlying transport
 * which is typically TCP. If at any point there is an error in 
 * the delivery of the response the observer is notified. It can
 * then shutdown the connection, as RFC 2616 suggests on errors. 
 * <p>
 * If however the response is delivered successfully the monitor is
 * notified of this event. On successful delivery the monitor will
 * hand the <code>Channel</code> back to the server kernel so that
 * the next request can be processed. This ensures ordering of the
 * responses matches ordering of the requests.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.core.Controller
 */
interface BodyObserver {

   /**
    * This is used to close the underlying transport. A closure is
    * typically done when the response is to a HTTP/1.0 client
    * that does not require a keep alive connection. Also, if the
    * container requests an explicit closure this is used when all
    * of the content for the response has been sent.
    * 
    * @param writer this is the writer used to send the response
    */
   void close(ByteWriter writer);

   /**
    * This is used when there is an error sending the response. On
    * error RFC 2616 suggests a connection closure is the best
    * means to handle the condition, and the one clients should be
    * expecting and support. All errors result in closure of the
    * underlying transport and no more requests are processed.
    * 
    * @param writer this is the writer used to send the response
    */
   void error(ByteWriter writer);

   /**
    * This is used when the response has been sent correctly and
    * the connection supports persisted HTTP. When ready the channel
    * is handed back in to the server kernel where the next request
    * on the pipeline is read and used to compose the next entity.
    * 
    * @param writer this is the writer used to send the response
    */
   void ready(ByteWriter writer);
   
   /**
    * This is used to notify the monitor that the HTTP response is
    * committed and that the header can no longer be changed. It 
    * is also used to indicate whether the response can be reset.
    * 
    * @param writer this is the writer used to send the response
    */
   void commit(ByteWriter writer);
   
   /**
    * This can be used to determine whether the response has been
    * committed. If the response is committed then the header can
    * no longer be manipulated and the response has been partially
    * send to the client.
    * 
    * @return true if the response headers have been committed
    */ 
   boolean isCommitted();

   /**
    * This is used to determine if the response has completed or
    * if there has been an error. This basically allows the writer
    * of the response to take action on certain I/O events.
    * 
    * @return this returns true if there was an error or close
    */
   boolean isClosed();

   /**
    * This is used to determine if the response was in error. If
    * the response was in error this allows the writer to throw an
    * exception indicating that there was a problem responding.
    * 
    * @return this returns true if there was a response error
    */
   boolean isError();
   
   /**
    * This represents the time at which the response was either
    * ready, closed or in error. Providing a time here is useful
    * as it allows the time taken to generate a response to be 
    * determined even if the response is written asynchronously.
    * 
    * @return the time when the response completed or failed
    */
   long getTime();
}