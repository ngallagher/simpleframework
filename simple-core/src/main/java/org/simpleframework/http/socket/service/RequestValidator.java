/*
 * RequestValidator.java February 2014
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

import static org.simpleframework.http.Protocol.CONNECTION;
import static org.simpleframework.http.Protocol.SEC_WEBSOCKET_KEY;
import static org.simpleframework.http.Protocol.SEC_WEBSOCKET_VERSION;
import static org.simpleframework.http.Protocol.UPGRADE;
import static org.simpleframework.http.Protocol.WEBSOCKET;

import java.util.List;

import org.simpleframework.http.Request;

/**
 * The <code>RequestValidator</code> object is used to ensure requests
 * for confirm to RFC 6455 section 4.2.1. The client opening handshake
 * must consist of several parts, including a version of 13 referring 
 * to RFC 6455, a WebSocket key, and the required HTTP connection
 * details. If any of these are missing the server is obliged to 
 * respond with a HTTP 400 response indicating a bad request.
 * 
 * @author Niall Gallagher
 */
class RequestValidator {
   
   /**
    * This is the request forming the client part of the handshake.
    */
   private final Request request;
   
   /**
    * This is the version referring to the required client version.
    */
   private final String version;
   
   /**
    * Constructor for the <code>RequestValidator</code> object. This 
    * is used to create a plain vanilla validator that uses version
    * 13 as dictated by RFC 6455 section 4.2.1.
    * 
    * @param request this is the handshake request from the client
    */
   public RequestValidator(Request request) {
      this(request, "13");
   }
   
   /**
    * Constructor for the <code>RequestValidator</code> object. This 
    * is used to create a plain vanilla validator that uses version
    * 13 as dictated by RFC 6455 section 4.2.1.
    * 
    * @param request this is the handshake request from the client
    * @param version a version other than 13 if desired
    */   
   public RequestValidator(Request request, String version) {
      this.request = request;
      this.version = version;
   }
   
   /**
    * This is used to determine if the client handshake request had
    * all the required headers as dictated by RFC 6455 section 4.2.1.
    * If the request does not contain any of these parts then this
    * will return false, indicating a HTTP 400 response should be
    * sent to the client. 
    * 
    * @return true if the request was a valid handshake
    */
   public boolean isValid() {
      if(!isProtocol()) {
         return false;
      }
      if(!isUpgrade()) {
         return false;
      }
      return true;
   }
   
   /**
    * This is used to determine if the request is a valid WebSocket
    * handshake of the correct version. This also checks to see if 
    * the request contained the required handshake token.
    * 
    * @return this returns true if the request is a valid handshake
    */
   private boolean isProtocol() {
      String protocol = request.getValue(SEC_WEBSOCKET_VERSION);
      String token = request.getValue(SEC_WEBSOCKET_KEY);
      
      if(token != null) {
         return version.equals(protocol);
      }
      return false;
   }
   
   /**
    * Here we check to ensure that there is a HTTP connection header
    * with the required upgrade token. The upgrade token may be 
    * one of many, so all must be checked. Finally to ensure that
    * the upgrade is for a WebSocket the upgrade header is checked.
    * 
    * @return this returns true if the request is an upgrade
    */
   private boolean isUpgrade() {
      List<String> tokens = request.getValues(CONNECTION); 
      
      for(String token : tokens) {
         if(token.equalsIgnoreCase(UPGRADE)) {
            String upgrade = request.getValue(UPGRADE);
            
            if(upgrade != null) {
               return upgrade.equalsIgnoreCase(WEBSOCKET);
            }
            return false;
         }
      }
      return false;
   }

}
