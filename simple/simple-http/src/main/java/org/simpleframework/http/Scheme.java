/*
* Scheme.java February 2014
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
 
package org.simpleframework.http;
 
import java.net.URI;
 
/**
* The <code>Scheme</code> represents a scheme used for a URI. Here
 * only schemes that directly relate to HTTP are provided, which
 * includes HTTP/1.1 schemes and WebSocket 1.0 schemes.
 *
 * @author Niall Gallagher
*/
public enum Scheme {
  
   /**
    * This represents the scheme for a plaintext HTTP connection.
    */
   HTTP("http", false),
  
   /**
    * This represents the scheme for a HTTP over TLS connection.
    */
   HTTPS("https", true),
  
   /**
    * This represents the scheme for a plaintext WebSocket connection.
    */
   WS("ws", false),
  
   /**
    * This represents the scheme for WebSocket over TLS connection.
    */
   WSS("wss", true);
  
   /**
    * This is the actual scheme token that is to be used in the URI.
    */
   public final String scheme;
  
   /**
    * This is used to determine if the connection is secure or not.
    */
   public final boolean secure;
  
   /**
    * Constructor for the <code>Scheme</code> object. This is used
    * create an entry using the specific scheme token and a boolean
    * indicating if the scheme is secure or not.
    *
    * @param scheme this is the scheme token to be used
    * @param secure this determines if the scheme is secure or not
    */
   private Scheme(String scheme, boolean secure) {
      this.scheme = scheme;
      this.secure = secure;
   }  
   
   /**
    * This is used to determine if the scheme is secure or not. In
    * general a secure scheme is one sent over a SSL/TLS connection.
    *
    * @return this returns true if the scheme is a secure one
    */
   public boolean isSecure() {
      return secure;
   }
  
   /**
    * This is used to acquire the scheme token for this. The scheme
    * token can be used to prefix a absolute fully qualified URI.
    *
    * @return the scheme token representing this scheme
    */
   public String getScheme() {
      return scheme;
   }
  
   /**
    * This is used to resolve the scheme given a token. If there is
    * no matching scheme for the provided token a default of HTTP
    * is provided.
    *
    * @param token this is the token used to determine the scheme
    *
    * @return this returns the match or HTTP if none matched
    */
   public static Scheme resolveScheme(String token) {
      if(token != null) {
         for(Scheme scheme : values()) {
            if(token.equalsIgnoreCase(scheme.scheme)) {
               return scheme;
            }
         }
      }
      return HTTP;
   }
  
   /**
    * This is used to resolve the scheme given a <code>URI</code>. If
    * there is no matching scheme for the provided instance then this
    * will return null.
    *
    * @param token this is the object to resolve a scheme for
    *
    * @return this returns the match or null if none matched
    */
   public static Scheme resolveScheme(URI target) {
      if(target != null) {
         String scheme = target.getScheme();
        
         for(Scheme option : values()) {
            if(option.scheme.equalsIgnoreCase(scheme)) {
               return option;
            }
         }
      }
      return null;
   }
}