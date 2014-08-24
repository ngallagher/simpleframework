/*
 * TransportDispatcher.java February 2007
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

import java.nio.channels.SocketChannel;

import org.simpleframework.transport.reactor.Operation;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>TransportDispatcher</code> operation is used transfer a 
 * transport to the processor so it can be processed. This is used so 
 * that when a transport is given to the processor it can be dispatched
 * in another thread to the processor. This is needed so that the
 * connection thread is occupied only briefly.
 * 
 * @author Niall Gallagher
 */
class TransportDispatcher implements Operation {
   
   /**
    * This is the processor used to transfer the transport to. 
    */
   private final TransportProcessor processor;
   
   /**
    * This is the transport to be passed to the processor.
    */
   private final Transport transport;

   /**
    * Constructor for the <code>TransportDispatcher</code> object. This 
    * is used to transfer a transport to a processor. Transferring the
    * transport using an operation ensures that the thread that is
    * used to process the transport is not occupied for long.
    * 
    * @param transport this is the transport this exchange uses
    * @param processor this is the negotiation to dispatch to
    */
   public TransportDispatcher(TransportProcessor processor, Transport transport) {
      this.transport = transport;
      this.processor = processor;      
   }
   
   /**
    * This is the <code>SelectableChannel</code> which is used to 
    * determine if the operation should be executed. If the channel   
    * is ready for a given I/O event it can be run. For instance if
    * the operation is used to perform some form of read operation
    * it can be executed when ready to read data from the channel.
    *
    * @return this returns the channel used to govern execution
    */ 
   public SocketChannel getChannel() {
      return transport.getChannel();
   }   
   
   /**
    * This is used to acquire the trace object that is associated
    * with the operation. A trace object is used to collection details
    * on what operations are being performed. For instance it may 
    * contain information relating to I/O events or errors. 
    * 
    * @return this returns the trace associated with this operation
    */     
   public Trace getTrace() {
      return transport.getTrace();
   }   
   
   /**
    * This is used to transfer the transport to the processor. This
    * will typically be executed asynchronously so that it does not
    * delay the thread that passes the <code>Transport</code> to the
    * transport processor, ensuring quicker processing.
    */
   public void run() {
      try {
         processor.process(transport);
      }catch(Exception e) {
         cancel();
      }
   }
   
   /**
    * This is used to cancel the operation if it has timed out. This
    * is typically invoked when it has been waiting in a selector for
    * an extended duration of time without any active operations on
    * it. In such a case the reactor must purge the operation to free
    * the memory and open channels associated with the operation.
    */ 
   public void cancel() {
      try {
         transport.close();
      }catch(Exception e) {
         return;
      }
   }
}
