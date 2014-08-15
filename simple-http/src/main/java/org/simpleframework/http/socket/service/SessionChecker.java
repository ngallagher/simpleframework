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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.simpleframework.http.Request;
import org.simpleframework.http.socket.DataFrame;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.WebSocket;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.trace.Trace;
import org.simpleframework.util.thread.Daemon;

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
    * This is used to hold the times the last ping was sent.
    */
   private final Map<Session, Long> times;
   
   /**
    * This is used to hold a reference to all ready sessions.
    */
   private final Set<Session> ready;
   
   /**
    * This holds sessions that are currently waiting for a pong.
    */
   private final Set<Session> waiting;
   
   /**
    * This is registered with each session to listen for pongs.
    */
   private final SessionMonitor monitor;
   
   /**
    * This is used to perform the pinging of the channels.
    */
   private final ChannelPinger pinger;

   /**
    * Constructor for the <code>SessionChecker</code> object. This will
    * create a checker using the specified ping frequency and expiry
    * for sessions. Here the expiry is the length of time a ping can
    * remain go unacknowledged, if there is no pong frame for this 
    * length of time then the session is closed.
    * 
    * @param frequency this is the frequency to ping all sessions
    * @param expiry this the expiry duration for an idle session
    */
   public SessionChecker(long frequency, long expiry) {
      this.pinger = new ChannelPinger(frequency, expiry);      
      this.times = new ConcurrentHashMap<Session, Long>();
      this.waiting = new CopyOnWriteArraySet<Session>();
      this.ready = new CopyOnWriteArraySet<Session>();
      this.monitor = new SessionMonitor(this);
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
      WebSocket socket = session.getSocket();
      Request request = session.getRequest();
      Channel channel = request.getChannel();

      try {
         if(pinger.isActive()) {
            socket.register(monitor);
            ready.add(session);
         }
      } catch (Exception e) {
         channel.close();
      }
   }

   /**
    * This notifies the sesion checker that a pong has been received 
    * in response to a ping. Upon receipt of a pong frame the session
    * can be rescheduled to receive a ping. If this is not called 
    * before the expiry duration elapses then the session is closed.
    * 
    * @param session this is the session that received a pong
    */
   public void refresh(Session session) {
      waiting.remove(session);
      times.remove(session);
   }

   /**
    * This is used to remove a session from notification. Upon removal
    * the session will no longer receive any ping frames. A session
    * can remain active without receiving any pings from the server
    * as the client often sends a ping from its end.
    * 
    * @param session this is the session to remove 
    */
   public void remove(Session session) {
      waiting.remove(session);
      ready.remove(session);
      times.remove(session);
   }

   /**
    * This is used to initiating session management by pinging all
    * connected WebSocket channels. If after a specific number of 
    * pings the WebSocket does not respond then the WebSocket is
    * closed using a control frame.
    */
   public void start() {
      pinger.start();
   }

   /**
    * This is used to stop session management. Stopping the session
    * manger means connected WebSocket channels will not receive
    * any ping messages, they will however still receive pong frames
    * if a ping is sent to it. Session management can be started 
    * and stopped at will.
    */
   public void stop() {
      pinger.stop();
   }

   /**
    * The <code>ChannelPinger</code> is used to dispatch ping frames
    * at a specified interval to the connected sessions. If at any
    * point during the delivery of a ping frame there is an I/O error
    * or an unexpected exception this will close the session.
    */
   private class ChannelPinger extends Daemon {

      /**
       * The only reason for a close is for an unexpected error.
       */
      private final Reason reason;
      
      /**
       * This is the frame that contains the ping to send.
       */
      private final Frame frame;
      
      /**
       * This is the frequency with which sessions are pinged at.
       */
      private final long frequency;
      
      /**
       * This is the length of time a session can remain idle.
       */
      private final long expiry;

      /**
       * Constructor for the <code>ChannelPinger</code> object. This
       * is used to create a pinger that will send out ping frames at
       * a specified interval. If a session does not respond within 
       * the configured expiry time then it is terminated.
       * 
       * @param frequency this is the frequency to send out pings
       * @param expiry this is the idle time for a session
       */
      public ChannelPinger(long frequency, long expiry) {
         this.reason = new Reason(INTERNAL_SERVER_ERROR);
         this.frame = new DataFrame(PING);
         this.frequency = frequency;
         this.expiry = expiry;
      }

      /**
       * This is used to perform a dispatch of the ping control frame
       * to all sessions that are currently not awaiting a pong. Before
       * a ping is sent out the sessions are checked to see if there are
       * any lingering sessions waiting for a pong that have been closed.
       */
      public void run() {
         try {
            execute();
         } finally {          
            purge();
         }
      }
      
      /**
       * This is used to perform a dispatch of the ping control frame
       * to all sessions that are currently not awaiting a pong. Before
       * a ping is sent out the sessions are checked to see if there are
       * any lingering sessions waiting for a pong that have been closed.
       */
      private void execute() {
         while (isActive()) {
            try {
               sleep();
               clean();                  
               process();
            } catch (Exception e) {
               continue;
            }
         }
      }
      
      /**
       * This is used to clear out all sessions. When the pinger stops
       * it is important to clear out all sessions so that there are
       * no memory leaks created by the pinger. Further attempts to 
       * register sessions are blocked before this is called.
       */
      private void purge() {
         times.clear();
         waiting.clear();
         ready.clear();
      }

      /**
       * To throttle the number of pings sent out the pinger sleeps for
       * a specified interval. If for some reason the thread cannot
       * sleep then the pinger is deactivaed and pinging is stopped.
       */
      private void sleep() {
         try {
            Thread.sleep(frequency);
         } catch (Exception e) {
            stop();
         }
      }      
      
      /**
       * This method is used to sweep all connected sessions to check
       * for sessions that need to be pinged or have expired. A session
       * will expire if it does not receive a pong within the expiry 
       * time. At any point if there is a I/O error the session will
       * be terminated and no more pings are sent to it.
       */
      private void process() {
         for(Session session : ready) {
            Long time = times.get(session);
            
            if(time != null) {
               expire(session, time);
            }  else {
               if(!waiting.contains(session)) {
                  ping(session);              
               } else {
                  close(session);
               }
            }               
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
      private void ping(Session session) {
         WebSocket socket = session.getSocket();
         Request request = session.getRequest();
         Channel channel = request.getChannel();
         Trace trace = channel.getTrace();
         long time = System.currentTimeMillis();
         
         try {
            trace.trace(WRITE_PING);
            times.put(session, time);              
            waiting.add(session); 
            socket.send(frame);           
         } catch (Exception cause) {              
            error(session, cause);
         }
      }

      /**
       * This method is used to check to see if a session has expired.
       * If the duration of the unacknowledged ping exceeds the expiry 
       * time the session is considered expired and is terminated.
       * 
       * @param session this is the session to check
       * @param sent this is the time the last ping was sent
       */
      private void expire(Session session, long sent) {
         WebSocket socket = session.getSocket();
         long time = System.currentTimeMillis();         

         try {
            if (time - sent > expiry) {
               ready.remove(session);
               waiting.remove(session);
               times.remove(session);
               socket.close();
            }
         } catch (Exception cause) {
            error(session, cause);
         }
      }
      
      /**
       * This method is used to terminate the session due to an error.
       * An attempt is made to terminate gracefully by sending a 
       * close control frame with a reason, if this fails the TCP
       * channel is closed and all references are removed.       
       * 
       * @param session this is the session to close
       * @param cause this is the cause of the failure
       */
      private void error(Session session, Exception cause) {
         WebSocket socket = session.getSocket();
         Request request = session.getRequest();
         Channel channel = request.getChannel();
         Trace trace = channel.getTrace();
         
         try {
            times.remove(session);
            waiting.remove(session);            
            trace.trace(ERROR, cause);
            socket.close(reason);
         } catch(Exception e) {
            channel.close();
         } finally {
            ready.remove(session);
         }
      }      
      
      /**
       * This is used to close the session and send a 1011 close code
       * to the client indicating an internal server error. Closing
       * of the session in this manner only occurs if there is an
       * expiry of the session or an I/O error, both of which are
       * unexpected and violate the behaviour as defined in RFC 6455.
       * 
       * @param session this is the session to be closed
       */ 
      private void close(Session session) {
         WebSocket socket = session.getSocket();
         
         try {
            times.remove(session);
            waiting.remove(session);
            socket.close(reason);
         } catch(Exception cause) {
            error(session, cause);
         } finally {
            ready.remove(session);            
         }
      }      
      
      /**
       * This used to reconcile the waiting sessions with the sessions
       * that are currently active. Reconciling in this manner is 
       * needed to ensure there are no memory leaks caused by session
       * references hanging around that will never receive a pong.
       */
      private void clean() throws Exception {
         for(Session session : waiting) {
            if(!ready.contains(session)) {
               close(session);
            }
         }
      }
   }
}
