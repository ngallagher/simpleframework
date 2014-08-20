package org.simpleframework.http.socket.service;

import static org.simpleframework.http.socket.CloseCode.INTERNAL_SERVER_ERROR;
import static org.simpleframework.http.socket.FrameType.PING;
import static org.simpleframework.http.socket.service.ServiceEvent.ERROR;
import static org.simpleframework.http.socket.service.ServiceEvent.PING_EXPIRED;
import static org.simpleframework.http.socket.service.ServiceEvent.PONG_RECEIVED;
import static org.simpleframework.http.socket.service.ServiceEvent.WRITE_PING;

import java.util.concurrent.atomic.AtomicLong;

import org.simpleframework.http.Request;
import org.simpleframework.http.socket.DataFrame;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.http.socket.WebSocket;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.trace.Trace;
import org.simpleframework.util.thread.ScheduledExecutor;

/**
 * The <code>StatusChecker</code> object is used to perform health 
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
class StatusChecker implements Runnable{
   
   /**
    * This is used to perform the monitoring of the sessions.
    */
   private final StatusResultListener listener;
   
   private final ScheduledExecutor scheduler;   

   
   /**
    * This is the last time a ping was sent by this.
    */
   private final AtomicLong counter;  
   
   
   /**
    * This is the WebSocket this this pinger will be monitoring.
    */
   private final WebSocket socket;      
   
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
   
   private final long frequency;

   /**
    * Constructor for the <code>ChannelPinger</code> object. This
    * is used to create a pinger that will send out ping frames at
    * a specified interval. If a session does not respond within 
    * the configured expiry time then it is terminated.
    * 
    * @param session this is the session to be pinged
    */
   public StatusChecker(ScheduledExecutor scheduler, WebSocket socket, Request request, long frequency) {
      this.listener = new StatusResultListener(this);
      this.reason = new Reason(INTERNAL_SERVER_ERROR);
      this.frame = new DataFrame(PING);
      this.counter = new AtomicLong();
      this.channel = request.getChannel();
      this.trace = channel.getTrace();
      this.scheduler = scheduler;
      this.frequency = frequency;
      this.socket = socket;
   }   
   
   public void refresh() {
      try {
         trace.trace(PONG_RECEIVED);
         counter.set(0);
      } catch(Exception cause) {
         trace.trace(ERROR, cause);
         channel.close();         
      }
   }   
   
   public void start() {
      try {            
         socket.register(listener);
         trace.trace(WRITE_PING);           
         socket.send(frame);    
         counter.getAndIncrement();         
         scheduler.execute(this);
      } catch(Exception cause) {
         trace.trace(ERROR, cause);
         channel.close();         
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

      try { 
         if(count < 3) {
            trace.trace(WRITE_PING);           
            socket.send(frame);
            counter.getAndIncrement();
            scheduler.execute(this, frequency); // schedule the next one
         } else {
            trace.trace(PING_EXPIRED);
            socket.close(reason);
         }
      } catch (Exception cause) {              
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
   public void expire() {
      try {
         socket.close(reason);
      } catch(Exception cause) {
         trace.trace(ERROR, cause);
         channel.close();         
      }
   }      
} 