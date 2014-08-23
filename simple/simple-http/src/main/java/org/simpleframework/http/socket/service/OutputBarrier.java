/*
 * OutputBarrier.java February 2014
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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.simpleframework.http.Request;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.ByteWriter;

/**
 * The <code>OutputBarrier</code> is used to ensure that control
 * frames and data frames do not get sent at the same time. Sending
 * both at the same time could lead to the status checking thread 
 * being blocked and this could eventually exhaust the thread pool.
 * 
 * @author Niall Gallagher
 */
class OutputBarrier {   
   
   /**
    * This is used to check if there is an operation in progress.
    */
   private final ReentrantLock lock;
   
   /**
    * This is the underlying sender used to send the frames.
    */
   private final ByteWriter writer;    
   
   /**
    * This is the TCP channel the frames are delivered over.
    */
   private final Channel channel;
   
   /**
    * This is the length of time to wait before failing to lock.
    */
   private final long duration;      
   
   /**
    * Constructor for the <code>OutputBarrier</code> object. This
    * is used to ensure that if there is currently a blocking write
    * in place that the <code>SessionChecker</code> will not end up
    * being blocked if it attempts to send a control frame.
    * 
    * @param request this is the request to get the TCP channel from
    * @param duration this is the length of time to wait for the lock
    */
   public OutputBarrier(Request request, long duration) {
      this.lock = new ReentrantLock();      
      this.channel = request.getChannel();
      this.writer = channel.getWriter();
      this.duration = duration;
   }
   
   /**
    * This method is used to send all frames. It is important that 
    * a lock is used to protect this so that if there is an attempt
    * to send out a control frame while the connection is blocked
    * there is an exception thrown.
    * 
    * @param frame this is the frame to send over the TCP channel
    */
   public void send(byte[] frame) throws IOException {
      try {        
         if(!lock.tryLock(duration, MILLISECONDS)) {
            throw new IOException("Transport lock could not be acquired");
         }
         try {
            writer.write(frame);
            writer.flush();  // less throughput, better latency          
         } finally {
            lock.unlock();                           
         }
      } catch(Exception e) {
         throw new IOException("Error writing to transport", e);
      }       
   }
}
