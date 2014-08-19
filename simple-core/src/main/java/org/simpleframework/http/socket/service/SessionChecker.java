/*
 * SessionChecker.java February 2014
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
import static org.simpleframework.http.socket.FrameType.PING;
import static org.simpleframework.http.socket.service.ServiceEvent.ERROR;
import static org.simpleframework.http.socket.service.ServiceEvent.WRITE_PING;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.simpleframework.http.Request;
import org.simpleframework.http.socket.DataFrame;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameListener;
import org.simpleframework.http.socket.FrameType;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.WebSocket;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.trace.Trace;
import org.simpleframework.util.thread.ScheduledExecutor;

/**
 * The <code>SessionChecker</code> object is used to perform health 
 * checks on connected sessions. Health is determined using the ping
 * pong protocol defined in RFC 6455. The ping pong protocol requires
 * that any enpoint must respond to a ping control frame with a pong
 * control frame containing the same payload. This session checker 
 * will send out out ping controls frames and wait for a pong frame.
 * If it does not receive a pong frame after a configured expiry time
 * then it will close the associated session.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.service.SessionMonitor
 */
class SessionChecker {      
   
   /**
    * This is the scheduler used to schedule ping operations.
    */
   private final ScheduledExecutor scheduler;
   
   /**
    * This is the frequency with which sessions are pinged at.
    */
   private final long frequency;
   
   /**
    * This is the length of time a session can remain idle.
    */
   private final long expiry;   

   /**
    * Constructor for the <code>SessionChecker</code> object. This will
    * create a checker using the specified ping frequency and expiry
    * for sessions. Here the expiry is the length of time a ping can
    * remain go unacknowledged, if there is no pong frame for this 
    * length of time then the session is closed.
    * 
    * @param scheduler this is the executor used to schedule pings
    * @param frequency this is the frequency to ping all sessions
    * @param expiry this the expiry duration for an idle session
    */
   public SessionChecker(ScheduledExecutor scheduler, long frequency, long expiry) {   
      this.scheduler = scheduler;
      this.frequency = frequency;
      this.expiry = expiry;
   }

   /**
    * This is used to register the session for health checks. Once 
    * registered this will have ping control frames sent at an interval
    * defined on construction. The ping interval on a given session 
    * will not be monotonically spaced, it depends on the time taken
    * to respond with a pong frame.
    * 
    * @param session this is the session to send ping frames to
    */
   public void register(Session session) {
      ChannelPinger pinger = new ChannelPinger(session); 

      try {
         pinger.start();
      } catch(Exception e) {
         pinger.close();
      }
   }

   /**
    * The <code>ChannelPinger</code> is used to dispatch ping frames
    * at a specified interval to the connected sessions. If at any
    * point during the delivery of a ping frame there is an I/O error
    * or an unexpected exception this will close the session.
    */
   public class ChannelPinger implements Runnable{
      
      /**
       * This is used to perform the monitoring of the sessions.
       */
      private final ChannelMonitor monitor;
      
      /**
       * This is the last time a ping was sent by this.
       */
      private final AtomicLong counter;      
      
      /**
       * This is the last time a ping was sent by this.
       */
      private final AtomicLong timer;
      
      /**
       * This is the WebSocket this this pinger will be monitoring.
       */
      private final WebSocket socket;

      /**
       * This is the request that is associated with the session.
       */
      private final Request request;
      
      /**
       * This is the channel that the session is associated with.
       */
      private final Channel channel;
      
      /**
       * This is used to trace various events for this pinger.
       */
      private final Trace trace;      
      
      /**
       * The only reason for a close is for an unexpected error.
       */
      private final Reason reason;
      
      /**
       * This is the frame that contains the ping to send.
       */
      private final Frame frame;

      /**
       * Constructor for the <code>ChannelPinger</code> object. This
       * is used to create a pinger that will send out ping frames at
       * a specified interval. If a session does not respond within 
       * the configured expiry time then it is terminated.
       * 
       * @param session this is the session to be pinged
       */
      public ChannelPinger(Session session) {
         this.monitor = new ChannelMonitor(this);
         this.reason = new Reason(INTERNAL_SERVER_ERROR);
         this.frame = new DataFrame(PING);
         this.counter = new AtomicLong();
         this.timer = new AtomicLong();
         this.socket = session.getSocket();
         this.request = session.getRequest();
         this.channel = request.getChannel();
         this.trace = channel.getTrace();
      }    
      
      public void start() {
         long time = System.currentTimeMillis();
         
         try {            
            timer.set(time);
            socket.register(monitor);
            scheduler.execute(this);
         } catch(Exception cause) {
            error(cause);
         }
      }

      /**
       * This method is used to ping the provided session. The ping 
       * will send the control frame and remember the time it was sent
       * so that if there is no pong frame it can terminate it. If
       * there are any I/O errors the session is terminated.
       * 
       * @param session this is the session to send to ping to
       */
      public void ping() {
         long time = System.currentTimeMillis();
         
         try {
            timer.set(time);  
            trace.trace(WRITE_PING);           
            socket.send(frame);
            counter.getAndIncrement();
            scheduler.execute(this, frequency); // schedule the next one
         } catch (Exception cause) {              
            error(cause);
         }
      }
      
      public void refresh() {
         long time = System.currentTimeMillis();
         long count = counter.get();
         
          if(count > 0) {
             timer.set(time);  
             counter.getAndDecrement();
          }
      }

      /**
       * This method is used to check to see if a session has expired.
       * If the duration of the unacknowledged ping exceeds the expiry 
       * time the session is considered expired and is terminated.
       * 
       * @param sent this is the time the last ping was sent
       */
      public void run() {
         long count = counter.get();
         
         if(count > 0) {  
            expire();
         } else {
            ping();
         }
      }
      
      private void expire() {
         long time = System.currentTimeMillis();
         long sent = timer.get();

         try {  
            if (time - sent < expiry) {               
               scheduler.execute(this, frequency); // reschedule
            } else {               
               socket.close();
            }
         } catch (Exception cause) {
            error(cause);
         }
      }
      
      /**
       * This method is used to terminate the session due to an error.
       * An attempt is made to terminate gracefully by sending a 
       * close control frame with a reason, if this fails the TCP
       * channel is closed and all references are removed.       
       * 
       * @param cause this is the cause of the failure
       */
      private void error(Exception cause) {
         try {               
            trace.trace(ERROR, cause);
            socket.close(reason);
         } catch(Exception e) {
            trace.trace(ERROR, cause);
         }
      }      
      
      /**
       * This is used to close the session and send a 1011 close code
       * to the client indicating an internal server error. Closing
       * of the session in this manner only occurs if there is an
       * expiry of the session or an I/O error, both of which are
       * unexpected and violate the behaviour as defined in RFC 6455.
       */ 
      public void close() {
         try {
            socket.close(reason);
         } catch(Exception cause) {
            error(cause);          
         }
      }      
   }
   
   /**
    * The <code>SessionMonitor</code> is used to listen for responses to
    * ping frames sent out by the server. A response to the ping frame 
    * is a pong frame. When a pong is received it allows the session to
    * be scheduled to receive another ping.
    * 
    * @author Niall Gallagher
    */
   private class ChannelMonitor implements FrameListener {
      
      /**
       * This is used to ping sessions to check for health.
       */
      private final ChannelPinger pinger;
      
      /**
       * Constructor for the <code>SessionMonitor</code> object. This 
       * requires the session health checker that performs the pings 
       * so that it can reschedule the session for multiple pings if
       * the connection responds with a pong.
       * 
       * @param pinger this is the session health checker
       */
      public ChannelMonitor(ChannelPinger pinger) {
         this.pinger = pinger;
      }

      /**
       * This is called when a new frame arrives on the WebSocket. If
       * the frame is a pong then this will reschedule the the session
       * to receive another ping frame.
       * 
       * @param session this is the associated session
       * @param frame this is the frame that has been received
       */   
      public void onFrame(Session session, Frame frame) {
         FrameType type = frame.getType();
         
         if(type.isPong()) {         
            pinger.refresh(); 
         }
      }

      /**
       * This is called when there is an error with the connection.
       * When called the session is removed from the checker and no
       * more ping frames are sent.
       * 
       * @param session this is the associated session
       * @param cause this is the cause of the error
       */
      public void onError(Session session, Exception cause) {
         pinger.error(cause);     
      }

      /**
       * This is called when the connection is closed from the other
       * side. When called the session is removed from the checker
       * and no more ping frames are sent.
       * 
       * @param session this is the associated session
       * @param reason this is the reason the connection was closed
       */
      public void onClose(Session session, Reason reason) {
         pinger.close();
      }
   }   
}
