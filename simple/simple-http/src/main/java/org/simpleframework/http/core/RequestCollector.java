/*
 * RequestCollector.java October 2002
 *
 * Copyright (C) 2002, Niall Gallagher <niallg@users.sf.net>
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
import static org.simpleframework.http.core.ContainerEvent.REQUEST_READY;
import static org.simpleframework.transport.TransportEvent.READ_WAIT;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.http.message.Body;
import org.simpleframework.http.message.EntityConsumer;
import org.simpleframework.http.message.Header;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.ByteCursor;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>RequestCollector</code> object is used to collect all of 
 * the data used to form a request entity. This will collect the data
 * fragment by fragment from the underlying transport. When all of 
 * the data is consumed and the entity is created and then it is sent
 * to the <code>Selector</code> object for processing. If the request
 * has completed the next request can be collected from the 
 * underlying transport using a new collector object.  
 * 
 * @author Niall Gallagher
 */
class RequestCollector implements Collector {
   
   /**
    * This is used to consume the request entity from the channel.
    */
   private final EntityConsumer entity;   
   
   /**
    * This is the cursor used to read and reset the data.
    */
   private final ByteCursor cursor;      
   
   /**
    * This is the channel used to acquire the underlying data.
    */
   private final Channel channel;
   
   /**
    * This is the trace used to listen for various collect events.
    */
   private final Trace trace;
   
   /**
    * This represents the time the request collection began at.
    */
   private final Timer timer;
   
   /**
    * The <code>RequestCollector</code> object used to collect the data 
    * from the underlying transport. In order to collect a body this 
    * must be given an <code>Allocator</code> which is used to create 
    * an internal buffer to store the consumed body.
    * 
    * @param allocator this is the allocator used to buffer data
    * @param tracker this is the tracker used to create sessions
    * @param channel this is the channel used to read the data
    */
   public RequestCollector(Allocator allocator, Channel channel) { 
      this.entity = new EntityConsumer(allocator, channel);
      this.timer = new Timer(MILLISECONDS);
      this.cursor = channel.getCursor();     
      this.trace = channel.getTrace();
      this.channel = channel;
   }

   /**
    * This is used to collect the data from a <code>Channel</code>
    * which is used to compose the entity. If at any stage there
    * are no ready bytes on the socket the controller provided can 
    * be used to queue the collector until such time as the socket 
    * is ready to read. Also, should the entity have completed reading
    * all required content it is handed to the controller as ready,
    * which processes the entity as a new client HTTP request.
    * 
    * @param controller this is the controller used to queue this
    */
   public void collect(Controller controller) throws IOException {
      while(cursor.isReady()) { 
         if(entity.isFinished()) {
             break;
         }  else {
            timer.set();
            entity.consume(cursor);               
         }      
      }     
      if(cursor.isOpen()) {
         if(entity.isFinished()) {
            trace.trace(REQUEST_READY);
            controller.ready(this);
         } else {
            trace.trace(READ_WAIT);
            controller.select(this);             
         }
      }
   }
   
   /**
    * This is the time in milliseconds when the request was first
    * read from the underlying channel. The time represented here
    * represents the time collection of this request began. This 
    * does not necessarily represent the time the bytes arrived on
    * the receive buffers as some data may have been buffered.
    * 
    * @return this represents the time the request was ready at
    */
   public long getTime() {
      return timer.get();
   }

   /**
    * This provides the HTTP request header for the entity. This is
    * always populated and provides the details sent by the client
    * such as the target URI and the query if specified. Also this
    * can be used to determine the method and protocol version used.
    * 
    * @return the header provided by the HTTP request message
    */   
   public Header getHeader() {
      return entity.getHeader();
   }
   
   /**
    * This is used to acquire the body for this HTTP entity. This
    * will return a body which can be used to read the content of
    * the message, also if the request is multipart upload then all
    * of the parts are provided as <code>Part</code> objects. Each
    * part can then be read as an individual message.
    *  
    * @return the body provided by the HTTP request message
    */   
   public Body getBody() {
      return entity.getBody();
   }
   
   /**
    * This provides the connected channel for the client. This is
    * used to send and receive bytes to and from an transport layer.
    * Each channel provided with an entity contains an attribute 
    * map which contains information about the connection.
    * 
    * @return the connected channel for this HTTP entity
    */   
   public Channel getChannel() {
      return channel;
   }  

   /**
    * This returns the socket channel that is used by the collector
    * to read content from. This is a selectable socket, in that
    * it can be registered with a Java NIO selector. This ensures
    * that the system can be notified when the socket is ready.
    * 
    * @return the socket channel used by this collector object
    */   
   public SocketChannel getSocket() {
      return channel.getSocket();
   }
}