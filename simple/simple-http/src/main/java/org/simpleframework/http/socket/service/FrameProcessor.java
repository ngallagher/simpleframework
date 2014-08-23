/*
 * FrameProcessor.java February 2014
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

import static org.simpleframework.http.socket.CloseCode.NORMAL_CLOSURE;
import static org.simpleframework.http.socket.service.ServiceEvent.ERROR;
import static org.simpleframework.http.socket.service.ServiceEvent.READ_FRAME;
import static org.simpleframework.http.socket.service.ServiceEvent.READ_PING;
import static org.simpleframework.http.socket.service.ServiceEvent.READ_PONG;
import static org.simpleframework.http.socket.service.ServiceEvent.WRITE_PONG;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.simpleframework.http.Request;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameListener;
import org.simpleframework.http.socket.FrameType;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.http.socket.Session;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>FrameProcessor</code> object is used to process incoming
 * data and dispatch that data as WebSocket frames. Dispatching of the
 * frames is done by making a callback to <code>FrameListener</code>
 * objects registered. In addition to frames this will also notify of
 * any errors that occur or on connection closure.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.service.FrameConsumer
 */
class FrameProcessor {
   
   /**
    * This is the set of listeners to dispatch frames to.
    */
   private final Set<FrameListener> listeners;
   
   /**
    * This is used to extract the reason description from a frame.
    */
   private final ReasonExtractor extractor;
   
   /**
    * This is used to consume the frames from the underling channel.
    */
   private final FrameConsumer consumer;
   
   /**
    * This is the encoder that is used to send control messages.
    */
   private final FrameEncoder encoder;
   
   /**
    * This is used to determine if a close notification was sent.
    */
   private final AtomicBoolean closed;   
   
   /**
    * This is the cursor used to maintain a read seek position.
    */
   private final ByteCursor cursor;   
   
   /**
    * This is the session associated with the WebSocket connection.
    */
   private final Session session;

   /**
    * This is the underlying TCP channel this reads frames from.
    */
   private final Channel channel;   
   
   /**
    * This is the reason message used for a normal closure.
    */
   private final Reason normal;
   
   /**
    * This is used to trace the events that occur on the channel.
    */
   private final Trace trace;
   
   /**
    * Constructor for the <code>FrameProcessor</code> object. This is
    * used to create a processor that can consume and dispatch frames
    * as defined by RFC 6455 to a set of registered listeners. 
    * 
    * @param encoder this is the encoder used to send control frames
    * @param session this is the session associated with the channel
    * @param channel this is the channel to read frames from
    */
   public FrameProcessor(FrameEncoder encoder, Session session, Request request) {
      this.listeners = new CopyOnWriteArraySet<FrameListener>();
      this.normal = new Reason(NORMAL_CLOSURE);
      this.extractor = new ReasonExtractor();
      this.consumer = new FrameConsumer();
      this.closed = new AtomicBoolean();
      this.channel = request.getChannel();
      this.cursor = channel.getCursor();
      this.trace = channel.getTrace();
      this.encoder = encoder;
      this.session = session;
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
      listeners.add(listener);
   }   

   /**
    * This is used to remove a <code>FrameListener</code> from this
    * instance. After removal the listener will no longer receive
    * any user frames or control messages from this specific instance.
    * 
    * @param listener this is the listener to be removed
    */
   public void remove(FrameListener listener) {
      listeners.remove(listener);
   }

   /**
    * This is used to process frames consumed from the underlying TCP
    * connection. It will respond to control frames such as pings and
    * will also handle close frames. Each frame, regardless of its
    * type will be dispatched to any <code>FrameListener</code> objects
    * that are registered with the processor. If an a close frame is
    * received it will echo that close frame, with the same close code
    * and back to the sender as suggested by RFC 6455 section 5.5.1.
    */
   public void process() throws IOException {
      if(cursor.isReady()) {
         consumer.consume(cursor);
         
         if(consumer.isFinished()) {
            Frame frame = consumer.getFrame();
            FrameType type = frame.getType();
            
            trace.trace(READ_FRAME, type);
            
            if(type.isPong()) {
               trace.trace(READ_PONG);               
            }
            if(type.isPing()){
               Frame response = frame.getFrame(FrameType.PONG);
               
               trace.trace(READ_PING);
               encoder.encode(response);
               trace.trace(WRITE_PONG);
            }            
            for(FrameListener listener : listeners) {
               listener.onFrame(session, frame);
            }             
            if(type.isClose()){
               Reason reason = extractor.extract(frame);               
              
               if(reason != null) {
                  close(reason);
               } else {
                  close();
               }
            } 
            consumer.clear();           
         }
      }
   }
   
   /**
    * This is used to report failures back to the client. Any I/O
    * or frame processing exception is reported back to all of the
    * registered listeners so that they can take action. The
    * underlying TCP connection is closed after any failure. 
    * 
    * @param reason this is the cause of the failure
    */
   public void failure(Exception reason) throws IOException {
      if(!closed.getAndSet(true)) {
         for(FrameListener listener : listeners) {   
            try {
               listener.onError(session, reason);
            } catch(Exception cause) {
               trace.trace(ERROR, cause);
            }
         }        
      }
   }
   
   /**
    * This is used to close the connection without a specific reason.
    * The close reason will be sent as a control frame before the
    * TCP connection is terminated. All registered listeners will be
    * notified of the close event.
    * 
    * @param reason this is the reason for the connection closure
    */  
   public void close(Reason reason) throws IOException{
      if(!closed.getAndSet(true)) {      
         for(FrameListener listener : listeners) {
            try {
               listener.onClose(session, reason);
            } catch(Exception cause) {
               trace.trace(ERROR, cause);
            }            
         }        
      }
   }   

   /**
    * This is used to close the connection when it has not responded
    * to any activity for a configured period of time. It may be
    * possible to send up a control frame, however if the TCP channel
    * is closed this will just notify the listeners.
    */   
   public void close() throws IOException{  
      if(!closed.getAndSet(true)) {   
         try {
            for(FrameListener listener : listeners) {
               listener.onClose(session, normal);
            }
         } catch(Exception cause) {
            trace.trace(ERROR, cause);
         }                 
      }
   }
}
