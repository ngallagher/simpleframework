/*
 * AcceptToken.java February 2014
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

import static org.simpleframework.http.Protocol.SEC_WEBSOCKET_KEY;

import java.io.IOException;
import java.security.MessageDigest;

import org.simpleframework.common.encode.Base64Encoder;
import org.simpleframework.http.Request;

/**
 * The <code>AcceptToken</code> is used to create a unique token based
 * on a random key sent by the client. This is used to prove that the 
 * handshake was received, the server has to take two pieces of 
 * information and combine them to form a response.  The first piece 
 * of information comes from the <code>Sec-WebSocket-Key</code> header 
 * field in the client handshake, the second is the globally unique
 * identifier <code>258EAFA5-E914-47DA-95CA-C5AB0DC85B11</code>. Both
 * are concatenated and an SHA-1 has is generated and used in the
 * session initiating response.
 * 
 * @author Niall Gallagher
 */
class AcceptToken {

   /**
    * This is the globally unique identifier used in the handshake.
    */
   private static final byte[] MAGIC = {
   '2', '5', '8', 'E', 'A', 'F', 'A', '5', '-', 
   'E', '9', '1', '4', '-', '4', '7', 'D', 'A', 
   '-', '9', '5', 'C', 'A', '-', 'C', '5', 'A', 
   'B', '0', 'D', 'C', '8', '5', 'B', '1', '1' };

   /**
    * This is used to generate the SHA-1 has from the user key.
    */
   private final MessageDigest digest;
   
   /**
    * This is the original request used to initiate the session.
    */
   private final Request request;
   
   /**
    * This is the character encoding to decode the key with.
    */
   private final String charset;

   /**
    * Constructor for the <code>AcceptToken</code> object. This is
    * to create an object that can generate a token from the client
    * key available from the <code>Sec-WebSocket-Key</code> header.
    * 
    * @param request this is the session initiating request
    */
   public AcceptToken(Request request) throws Exception {
      this(request, "SHA-1");
   }

   /**
    * Constructor for the <code>AcceptToken</code> object. This is
    * to create an object that can generate a token from the client
    * key available from the <code>Sec-WebSocket-Key</code> header.
    * 
    * @param request this is the session initiating request
    * @param algorithm the algorithm used to create the token
    */
   public AcceptToken(Request request, String algorithm) throws Exception {
      this(request, algorithm, "UTF-8");
   }

   /**
    * Constructor for the <code>AcceptToken</code> object. This is
    * to create an object that can generate a token from the client
    * key available from the <code>Sec-WebSocket-Key</code> header.
    * 
    * @param request this is the session initiating request
    * @param algorithm the algorithm used to create the token
    * @param charset the encoding used to decode the client key
    */
   public AcceptToken(Request request, String algorithm, String charset) throws Exception {
      this.digest = MessageDigest.getInstance(algorithm);
      this.request = request;
      this.charset = charset;
   }

   /**
    * This is used to create the required accept token for the session
    * initiating response. The resulting token is a SHA-1 digest of
    * the <code>Sec-WebSocket-Key</code> a globally unique identifier
    * defined in RFC 6455 all encoded in base64.
    * 
    * @return the accept token for the session initiating response
    */
   public String create() throws IOException {
      String value = request.getValue(SEC_WEBSOCKET_KEY);
      byte[] data = value.getBytes(charset);

      if (data.length > 0) {         
         digest.update(data);
         digest.update(MAGIC);
      }
      byte[] digested = digest.digest();
      char[] text = Base64Encoder.encode(digested);

      return new String(text);
   }
}
