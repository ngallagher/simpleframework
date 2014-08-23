/*
 * FrameConnection.java February 2014
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
import static org.simpleframework.http.socket.service.ServiceEvent.OPEN_SOCKET;

import java.io.IOException;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameListener;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.FrameChannel;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.ByteWriter;
import org.simpleframework.transport.reactor.Reactor;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>FrameConnection</code> represents a connection that can
 * send and receivd WebSocket frames. Any instance of this will provide
 * a means to perform asynchronous writes and reads to a remote client
 * using a lightweight framing protocol. A frame is a finite length
 * sequence of bytes that can hold either text or binary data. Also,
 * control frames are used to perform heartbeat monitoring and closure. 
 * <p>
 * For convenience frames can be consumed from the socket via a
 * callback to a registered listener. This avoids having to poll each
 * socket for data and provides a asynchronous event driven model of
 * communication, which greatly reduces overhead and complication.
 * 
 * @author Niall Gallagher
 */
class FrameConnection implements FrameChannel {
   
   /**
    * The collector is used to collect frames from the TCP channel.
    */
   private final FrameCollector operation;
   
   /**
    * This encoder is used to encode data as RFC 6455 frames.
    */
   private final FrameEncoder encoder;   
   
   /**
    * This is the sender used to send frames over the channel. 
    */
   private final ByteWriter writer;      
   
   /**
    * This is the session object that has a synchronized channel.
    */
   private final Session session;   
   
   /**
    * This is the underlying TCP channel that frames are sent over.
    */
   private final Channel channel;   
   
   /**
    * The reason that is sent if at any time the channel is closed.
    */
   private final Reason reason;

   /**
    * This is used to trace all events that occur on the channel.
    */
   private final Trace trace;
   
   /**
    * Constructor for the <code>FrameConnection</code> object. This is used
    * to create a channel that can read and write frames over a TCP
    * channel. For asynchronous read and dispatch operations this will
    * produce an operation to collect and process RFC 6455 frames.
    * 
    * @param request this is the initiating request for the WebSocket
    * @param response this is the initiating response for the WebSocket
    * @param reactor this is the reactor used to process frames
    */
   public FrameConnection(Request request, Response response, Reactor reactor) {
      this.encoder = new FrameEncoder(request);  
      this.session = new ServiceSession(this, request, response);
      this.operation = new FrameCollector(encoder, session, request, reactor);
      this.reason = new Reason(NORMAL_CLOSURE);
      this.channel = request.getChannel();
      this.writer = channel.getWriter();
      this.trace = channel.getTrace();
   }    
   
   /**
    * This is used to open the channel and begin consuming frames. This
    * will also return the session that contains the details for the
    * created WebSocket such as the initiating request and response as
    * well as the <code>FrameChannel</code> object.
    * 
    * @return the session associated with the WebSocket
    */
   public Session open() throws IOException {
      trace.trace(OPEN_SOCKET);
      operation.run();
      return session;
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
   public void register(FrameListener listener) throws IOException {
      operation.register(listener);
   }

   /**
    * This is used to remove a <code>FrameListener</code> from this
    * instance. After removal the listener will no longer receive
    * any user frames or control messages from this specific instance.
    * 
    * @param listener this is the listener to be removed
    */
   public void remove(FrameListener listener) throws IOException {
      operation.remove(listener);
   }   

   /**
    * This is used to send data to the connected client. To prevent
    * an application code from causing resource issues this will block
    * as soon as a configured linked list of mapped memory buffers has
    * been exhausted. Caution should be taken when writing a broadcast
    * implementation that can write to multiple sockets as a badly
    * behaving socket that has filled its output buffering capacity
    * can cause congestion.
    * 
    * @param data this is the data that is to be sent
    */
   public void send(byte[] data) throws IOException {
      encoder.encode(data);  
   }

   /**
    * This is used to send text to the connected client. To prevent
    * an application code from causing resource issues this will block
    * as soon as a configured linked list of mapped memory buffers has
    * been exhausted. Caution should be taken when writing a broadcast
    * implementation that can write to multiple sockets as a badly
    * behaving socket that has filled its output buffering capacity
    * can cause congestion.
    * 
    * @param text this is the text that is to be sent
    */
   public void send(String text) throws IOException {
      encoder.encode(text);  
   }
   
   /**
    * This is used to send data to the connected client. To prevent
    * an application code from causing resource issues this will block
    * as soon as a configured linked list of mapped memory buffers has
    * been exhausted. Caution should be taken when writing a broadcast
    * implementation that can write to multiple sockets as a badly
    * behaving socket that has filled its output buffering capacity
    * can cause congestion.
    * 
    * @param frame this is the frame that is to be sent
    */
   public void send(Frame frame) throws IOException {           
      encoder.encode(frame); 
   } 

   /**
    * This is used to close the connection with a specific reason.
    * The close reason will be sent as a control frame before the
    * TCP connection is terminated.
    * 
    * @param reason the reason for closing the connection
    */
   public void close(Reason reason) throws IOException {
      encoder.encode(reason);  
      writer.close();
   }  
   
   /**
    * This is used to close the connection without a specific reason.
    * The close reason will be sent as a control frame before the
    * TCP connection is terminated.
    */
   public void close() throws IOException {
      encoder.encode(reason);  
      writer.close();
   }
}