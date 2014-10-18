/*
 * StatusChecker.java February 2014
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

import static org.simpleframework.http.socket.CloseCode.INTERNAL_SERVER_ERROR;
import static org.simpleframework.http.socket.CloseCode.NORMAL_CLOSURE;
import static org.simpleframework.http.socket.FrameType.PING;
import static org.simpleframework.http.socket.service.ServiceEvent.ERROR;
import static org.simpleframework.http.socket.service.ServiceEvent.PING_EXPIRED;
import static org.simpleframework.http.socket.service.ServiceEvent.PONG_RECEIVED;
import static org.simpleframework.http.socket.service.ServiceEvent.WRITE_PING;

import java.util.concurrent.atomic.AtomicLong;

import org.simpleframework.common.thread.Scheduler;
import org.simpleframework.http.Request;
import org.simpleframework.http.socket.DataFrame;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>StatusChecker</code> object is used to perform health 
 * checks on connected sessions. Health is determined using the ping
 * pong protocol defined in RFC 6455. The ping pong protocol requires
 * that any endpoint must respond to a ping control frame with a pong
 * control frame containing the same payload. This session checker 
 * will send out out ping controls frames and wait for a pong frame.
 * If it does not receive a pong frame after a configured expiry time
 * then it will close the associated session.
 * 
 * @author Niall Gallagher
 */
class StatusChecker implements Runnable{
   
   /**
    * This is used to perform the monitoring of the sessions.
    */
   private final StatusResultListener listener;
   
   /**
    * This is the WebSocket this this pinger will be monitoring.
    */
   private final FrameConnection connection; 
   
   /**
    * This is the shared scheduler used to execute this checker.
    */
   private final Scheduler scheduler;      
   
   /**
    * This is a count of the number of unacknowledged ping frames.
    */
   private final AtomicLong counter;        
   
   /**
    * This is the underling TCP channel that is being checked.
    */
   private final Channel channel;  
   
   /**
    * The only reason for a close is for an unexpected error.
    */
   private final Reason normal;
   
   /**
    * The only reason for a close is for an unexpected error.
    */
   private final Reason error;      
   
   /**
    * This is used to trace various events for this pinger.
    */
   private final Trace trace;       
   
   /**
    * This is the frame that contains the ping to send.
    */
   private final Frame frame;
   
   /**
    * This is the frequency with which the checker should run.
    */
   private final long frequency;

   /**
    * Constructor for the <code>StatusChecker</code> object. This
    * is used to create a pinger that will send out ping frames at
    * a specified interval. If a session does not respond within 
    * three times the duration of the ping the connection is reset.
    * 
    * @param connection this is the WebSocket to send the frames
    * @param request this is the associated request
    * @param scheduler this is the scheduler used to execute this     
    * @param frequency this is the frequency with which to ping
    */
   public StatusChecker(FrameConnection connection, Request request, Scheduler scheduler, long frequency) {
      this.listener = new StatusResultListener(this);
      this.error = new Reason(INTERNAL_SERVER_ERROR);
      this.normal = new Reason(NORMAL_CLOSURE);      
      this.frame = new DataFrame(PING);
      this.counter = new AtomicLong();
      this.channel = request.getChannel();
      this.trace = channel.getTrace();
      this.connection = connection;       
      this.scheduler = scheduler;
      this.frequency = frequency;        
   }   
   
   /**
    * This is used to kick of the status checking. Here an initial
    * ping is sent over the socket and the task is then scheduled to
    * check the result after the frequency period has expired. If
    * this method fails for any reason the TCP channel is closed.
    */
   public void start() {
      try {            
         connection.register(listener);
         trace.trace(WRITE_PING);           
         connection.send(frame);    
         counter.getAndIncrement();         
         scheduler.execute(this, frequency); 
      } catch(Exception cause) {
         trace.trace(ERROR, cause);
         channel.close();         
      }
   }

   /**
    * This method is used to check to see if a session has expired.
    * If there have been three unacknowledged ping events then this
    * will force a closure of the WebSocket connection. This is done
    * to ensure only healthy connections are maintained within the
    * server, also RFC 6455 recommends using the ping pong protocol.
    */
   public void run() {
      long count = counter.get();

      try { 
         if(count < 3) {
            trace.trace(WRITE_PING);           
            connection.send(frame);
            counter.getAndIncrement();
            scheduler.execute(this, frequency); // schedule the next one
         } else {
            trace.trace(PING_EXPIRED);
            connection.close(normal);
         }
      } catch (Exception cause) {              
         trace.trace(ERROR, cause);
         channel.close();         
      }
   }
   
   /**
    * If the connection gets a response to its ping message then this
    * will reset the internal counter. This ensure that the connection
    * does not time out. If after three pings there is not response
    * from the other side then the connection will be terminated.
    */
   public void refresh() {
      try {
         trace.trace(PONG_RECEIVED);
         counter.set(0);
      } catch(Exception cause) {
         trace.trace(ERROR, cause);
         channel.close();         
      }
   }      
   
   /**
    * This is used to close the session and send a 1011 close code
    * to the client indicating an internal server error. Closing
    * of the session in this manner only occurs if there is an
    * expiry of the session or an I/O error, both of which are
    * unexpected and violate the behaviour as defined in RFC 6455.
    */ 
   public void failure() {
      try {
         connection.close(error);
         channel.close();           
      } catch(Exception cause) {
         trace.trace(ERROR, cause);       
         channel.close();           
      }
   }      
   
   /**
    * This is used to close the session and send a 1000 close code
    * to the client indicating a normal closure. This will be called
    * when there is a close notification dispatched to the status
    * listener. Typically here a graceful closure is best.
    */ 
   public void close() {
      try {
         connection.close(normal);
         channel.close();           
      } catch(Exception cause) {
         trace.trace(ERROR, cause);       
         channel.close();           
      }
   }      
} 