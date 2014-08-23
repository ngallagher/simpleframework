/*
 * ResponseBuilder.java February 2014
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

import static org.simpleframework.http.Protocol.CLOSE;
import static org.simpleframework.http.Protocol.CONNECTION;
import static org.simpleframework.http.Protocol.DATE;
import static org.simpleframework.http.Protocol.SEC_WEBSOCKET_ACCEPT;
import static org.simpleframework.http.Protocol.UPGRADE;
import static org.simpleframework.http.Protocol.WEBSOCKET;
import static org.simpleframework.http.socket.service.ServiceEvent.WRITE_HEADER;

import java.io.IOException;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.ByteWriter;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>ResponseBuilder</code> object is used to build a response
 * to a WebSocket handshake. In order for a successful handshake to
 * complete a HTTP request must have a version of 13 referring 
 * to RFC 6455, a WebSocket key, and the required HTTP connection
 * details. If any of these are missing the server is obliged to 
 * respond with a HTTP 400 response indicating a bad request.
 * 
 * @author Niall Gallagher
 */
class ResponseBuilder  {
   
   /**
    * This is used to validate the initiating WebSocket request.
    */
   private final RequestValidator validator;
   
   /**
    * This is the accept token generated for the request.
    */
   private final AcceptToken token;
   
   /**
    * This is the sender used to send the WebSocket response.
    */
   private final ByteWriter writer;   
   
   /**
    * This is the response to the WebSocket handshake.
    */
   private final Response response;
   
   /**
    * This is the underlying TCP channel for the request.
    */
   private final Channel channel;
   
   /**
    * This is used to trace the activity for the handshake.
    */
   private final Trace trace;

   /**
    * Constructor for the <code>ResponseBuilder</code> object. In order
    * to process the WebSocket handshake this requires the original 
    * request and the response as well as the underlying TCP channel
    * which forms the basis of the WebSocket connection.
    * 
    * @param request this is the request that initiated the handshake
    * @param response this is the response for the handshake
    */
   public ResponseBuilder(Request request, Response response) throws Exception {
      this.validator = new RequestValidator(request);
      this.token = new AcceptToken(request); 
      this.channel = request.getChannel();
      this.writer = channel.getWriter();
      this.trace = channel.getTrace();
      this.response = response;
   }

   /**
    * This is used to determine if the client handshake request had
    * all the required headers as dictated by RFC 6455 section 4.2.1.
    * If the request does not contain any of these parts then this
    * will return false, indicating a HTTP 400 response is sent to
    * the client, otherwise a HTTP 101 response is sent.
    */
   public void commit() throws IOException { 
      if(validator.isValid()) {
         accept();
      } else {
         reject();
      }
   }
   
   /**
    * This is used to respond to the client with a HTTP 400 response
    * indicating the WebSocket handshake failed. No response body is
    * sent with the rejection message and the underlying TCP channel
    * is closed to prevent further use of the connection.
    */
   private void reject() throws IOException {
      long time = System.currentTimeMillis();
      
      response.setStatus(Status.BAD_REQUEST);
      response.setValue(CONNECTION, CLOSE);
      response.setDate(DATE, time);
              
      String header = response.toString();         
      byte[] message = header.getBytes("UTF-8");
      
      trace.trace(WRITE_HEADER, header);
      writer.write(message);
      writer.flush();
      writer.close();
   }
   
   /**
    * This is used to respond to the client with a HTTP 101 response
    * to indicate that the WebSocket handshake succeeeded. Once this
    * response has been sent all traffic between the client and 
    * server will be with WebSocket frames as defined by RFC 6455. 
    */
   private void accept() throws IOException {
      long time = System.currentTimeMillis();
      String accept = token.create();
      
      response.setStatus(Status.SWITCHING_PROTOCOLS);
      response.setDescription(UPGRADE);
      response.setValue(CONNECTION, UPGRADE);
      response.setDate(DATE, time);
      response.setValue(SEC_WEBSOCKET_ACCEPT, accept);
      response.setValue(UPGRADE, WEBSOCKET); 
              
      String header = response.toString();         
      byte[] message = header.getBytes("UTF-8");
      
      trace.trace(WRITE_HEADER, header);
      writer.write(message);
      writer.flush();      
   }
}
