/*
 * FrameCollector.java February 2014
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

package org.simpleframework.http.socket.service;

import static org.simpleframework.http.socket.service.ServiceEvent.ERROR;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

import org.simpleframework.http.Request;
import org.simpleframework.http.socket.FrameListener;
import org.simpleframework.http.socket.Session;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.reactor.Operation;
import org.simpleframework.transport.reactor.Reactor;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>FrameCollector</code> operation is used to collect frames
 * from a channel and dispatch them to a <code>FrameListener</code>.
 * To ensure that stale connections do not linger any connection that
 * does not send a control ping or pong frame within two minutes will
 * be terminated and the close control frame will be sent.
 * 
 * @author Niall Gallagher
 */
class FrameCollector implements Operation {

   /**
    * This decodes the frame bytes from the channel and processes it.
    */
   private final FrameProcessor processor;   
   
   /**
    * This is the cursor used to maintain a stream seek position.
    */
   private final ByteCursor cursor;   
   
   /**
    * This is the underlying channel for this frame collector.
    */
   private final Channel channel;
   
   /**
    * This is the reactor used to schedule this operation for reads.
    */
   private final Reactor reactor;
   
   /**
    * This is the tracer that is used to trace the frame collection.
    */
   private final Trace trace;
   
   /**
    * Constructor for the <code>FrameCollector</code> object. This is
    * used to create a collector that will process and dispatch web 
    * socket frames as defined by RFC 6455.
    * 
    * @param encoder this is the encoder used to send messages
    * @param session this is the web socket session
    * @param channel this is the underlying TCP communication channel
    * @param reactor this is the reactor used for read notifications
    */
   public FrameCollector(FrameEncoder encoder, Session session, Request request, Reactor reactor) {
      this.processor = new FrameProcessor(encoder, session, request);
      this.channel = request.getChannel();
      this.cursor = channel.getCursor();
      this.trace = channel.getTrace();
      this.reactor = reactor;
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
      return trace;
   }

   /**
    * This is the channel associated with this collector. This is used
    * to register for notification of read events. If at any time the
    * remote endpoint is closed then this will cause the collector
    * to perform a final execution before closing.
    * 
    * @return this returns the selectable TCP channel
    */
   public SelectableChannel getChannel() {
      return channel.getSocket();
   }

   /**
    * This is used to register a <code>FrameListener</code> to this
    * instance. The registered listener will receive all user frames
    * and control frames sent from the client. Also, when the frame
    * is closed or when an unexpected error occurs the listener is
    * notified. Any number of listeners can be registered at any time.
    * 
    * @param listener this is the listener that is to be registered
    */
   public void register(FrameListener listener) {
      processor.register(listener);
   }

   /**
    * This is used to remove a <code>FrameListener</code> from this
    * instance. After removal the listener will no longer receive
    * any user frames or control messages from this specific instance.
    * 
    * @param listener this is the listener to be removed
    */
   public void remove(FrameListener listener) {
      processor.remove(listener);
   }

   /**
    * This is used to execute the collection operation. Collection is
    * done by reading the frame header from the incoming data, once
    * consumed the remainder of the frame is collected until such 
    * time as it has been fully consumed. When consumed it will be
    * dispatched to the registered frame listeners.
    */
   public void run() {
      try {
         processor.process();
         
         if(cursor.isOpen()) {
            reactor.process(this, SelectionKey.OP_READ);
         } else {
            processor.close();
         }
      } catch(Exception cause) {
         trace.trace(ERROR, cause);
         
         try {
            processor.failure(cause);
         } catch(Exception fatal) {
            trace.trace(ERROR, fatal);
         } finally {
            channel.close();
         }
      }
   }

   /**
    * This is called when a read operation has timed out. To ensure 
    * that stale channels do not remain registered they are cleared
    * out with this method and a close frame is sent if possible.
    */
   public void cancel() {
      try{
         processor.close();
      } catch(Exception cause) {
         trace.trace(ERROR, cause);
         channel.close();         
      }
   }      
}