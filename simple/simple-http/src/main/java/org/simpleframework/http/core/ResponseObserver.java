/*
 * ResponseObserver.java February 2007
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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.simpleframework.http.core.ContainerEvent.ERROR;
import static org.simpleframework.http.core.ContainerEvent.RESPONSE_FINISHED;

import java.util.concurrent.atomic.AtomicBoolean;

import org.simpleframework.http.message.Entity;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.ByteWriter;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>ResponseObserver</code> is used to observe the response
 * streams. If there is an error or a close requested this will
 * close the underlying transport. If however there is a successful
 * response then this will flush the transport and hand the channel
 * for the pipeline back to the server kernel. This ensures that
 * the next HTTP request can be consumed from the transport.
 * 
 * @author Niall Gallagher
 */
class ResponseObserver implements BodyObserver {  
   
   /**
    * This is used to determine if the response has committed.
    */
   private AtomicBoolean committed;
   
   /**
    * This flag determines whether the connection was closed.
    */
   private AtomicBoolean closed;
   
   /**
    * This flag determines whether the was a response error.
    */
   private AtomicBoolean error;      
   
   /**
    * This is the controller used to initiate a new request.
    */
   private Controller controller;
   
   /**
    * This is the channel associated with the client connection.
    */
   private Channel channel;
   
   /**
    * This is the trace used to observe the state of the stream.
    */
   private Trace trace;
   
   /**
    * This represents a time stamp that records the finish time.
    */
   private Timer timer;
   
   /**
    * Constructor for the <code>ResponseObserver</code> object. This 
    * is used to create an observer using a HTTP request entity and an
    * initiator which is used to reprocess a channel if there was a
    * successful deliver of a response.
    * 
    * @param controller the controller used to process channels
    * @param entity this is the entity associated with the channel
    */ 
   public ResponseObserver(Controller controller, Entity entity) {
      this.timer = new Timer(MILLISECONDS);       
      this.committed = new AtomicBoolean();     
      this.closed = new AtomicBoolean();
      this.error = new AtomicBoolean();
      this.channel = entity.getChannel();
      this.trace = channel.getTrace();
      this.controller = controller;
   }

   /**
    * This is used to close the underlying transport. A closure is
    * typically done when the response is to a HTTP/1.0 client
    * that does not require a keep alive connection. Also, if the
    * container requests an explicit closure this is used when all
    * of the content for the response has been sent.
    * 
    * @param writer this is the writer used to send the response
    */   
   public void close(ByteWriter writer) {
      try {
         if(!isClosed()) {
            closed.set(true);
            timer.set();
            trace.trace(RESPONSE_FINISHED);
            writer.close();
         }
      } catch(Exception cause) {
         trace.trace(ERROR, cause);
         fail(writer);
      }
   }
   
   /**
    * This is used when there is an error sending the response. On
    * error RFC 2616 suggests a connection closure is the best
    * means to handle the condition, and the one clients should be
    * expecting and support. All errors result in closure of the
    * underlying transport and no more requests are processed.
    * 
    * @param writer this is the writer used to send the response
    */   
   public void error(ByteWriter writer) {
      try {
         if(!isClosed()) {
            error.set(true);
            timer.set();
            trace.trace(RESPONSE_FINISHED);
            writer.close();
         }            
      } catch(Exception cause) {
         trace.trace(ERROR, cause);
         fail(writer);
      }
   }
   
   /**
    * This is used when the response has been sent correctly and
    * the connection supports persisted HTTP. When ready the channel
    * is handed back in to the server kernel where the next request
    * on the pipeline is read and used to compose the next entity.
    * 
    * @param writer this is the writer used to send the response
    */   
   public void ready(ByteWriter writer) {
      try {
         if(!isClosed()) {
            closed.set(true);
            writer.flush();
            timer.set();
            trace.trace(RESPONSE_FINISHED);
            controller.start(channel);
         }
      } catch(Exception cause) {
         trace.trace(ERROR, cause);
         fail(writer);
      }
   }
   
   /**
    * This is used to purge the writer so that it closes the socket
    * ensuring there is no connection leak on shutdown. This is used
    * when there is an exception signalling the state of the writer. 
    * 
    * @param writer this is the writer that is to be purged
    */
   private void fail(ByteWriter writer) {
      try {
         writer.close();
      } catch(Exception cause) {
         trace.trace(ERROR, cause);
      }
   }
   
   /**
    * This is used to notify the observer that the HTTP response is
    * committed and that the header can no longer be changed. It 
    * is also used to indicate whether the response can be reset.
    * 
    * @param writer this is the writer used to send the response
    */
   public void commit(ByteWriter writer) {
      committed.set(true);
   }
   
   /**
    * This can be used to determine whether the response has been
    * committed. If the response is committed then the header can
    * no longer be manipulated and the response has been partially
    * send to the client.
    * 
    * @return true if the response headers have been committed
    */ 
   public boolean isCommitted() {
      return committed.get();
   }
   
   /**
    * This is used to determine if the response has completed or
    * if there has been an error. This basically allows the writer
    * of the response to take action on certain I/O events.
    * 
    * @return this returns true if there was an error or close
    */   
   public boolean isClosed() {
      return closed.get() || error.get();
   }
   
   /**
    * This is used to determine if the response was in error. If
    * the response was in error this allows the writer to throw an
    * exception indicating that there was a problem responding.
    * 
    * @return this returns true if there was a response error
    */   
   public boolean isError(){
      return error.get();
   }
   
   /**
    * This represents the time at which the response was either
    * ready, closed or in error. Providing a time here is useful
    * as it allows the time taken to generate a response to be 
    * determined even if the response is written asynchronously.
    * 
    * @return the time when the response completed or failed
    */
   public long getTime() {
      return timer.get();
   }
}
