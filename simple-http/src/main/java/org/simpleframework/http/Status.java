/*
 * Status.java February 2008
 *
 * Copyright (C) 2008, Niall Gallagher <niallg@users.sf.net>
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

/**
 * The <code>Status</code> enumeration is used to specify status codes
 * and the descriptions of those status codes. This is a convenience
 * enumeration that allows users to acquire the descriptions of codes
 * by simply providing the code. Also if the response state is known
 * the code and description can be provided to the client.
 * <p>
 * The official HTTP status codes are defined in RFC 2616 section 10.
 * Each set of status codes belongs to a specific family. Each family
 * describes a specific scenario. Although it is possible to use other
 * status codes it is recommended that servers restrict their status
 * code responses to those specified in this enumeration.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.StatusLine
 */
public enum Status {
   
   /**
    * This is used as an intermediate response to a request.
    */
   CONTINUE(100, "Continue"),
   
   /**
    * This represents a change in the protocol the client is using.
    */
   SWITCHING_PROTOCOLS(101, "Switching Protocols"),
   
   /**
    * This represents a successful response of a targeted request.
    */
   OK(200, "OK"),
   
   /**
    * This is used to signify that a resource was created successfully.
    */
   CREATED(201, "Created"),
   
   /**
    * This is used to signify that the request has been accepted.
    */
   ACCEPTED(202, "Accepted"),
   
   /**
    * This represents a response that contains no response content.
    */
   NO_CONTENT(204, "No Content"),
   
   /**
    * This is used to represent a response that resets the content.
    */
   RESET_CONTENT(205, "Reset Content"),
   
   /**
    * This is used to represent a response that has partial content.
    */
   PARTIAL_CONTENT(206, "Partial Content"),
   
   /**
    * This is used to represent a response where there are choices.
    */
   MULTIPLE_CHOICES(300, "Multiple Choices"),
   
   /**
    * This is used to represent a target resource that has moved.
    */
   MOVED_PERMANENTLY(301, "Moved Permanently"),
   
   /**
    * This is used to represent a resource that has been found.
    */
   FOUND(302, "Found"),
   
   /**
    * This is used to tell the client to see another HTTP resource.
    */
   SEE_OTHER(303, "See Other"),
   
   /**
    * This is used in response to a target that has not been modified.
    */
   NOT_MODIFIED(304, "Not Modified"),
   
   /**
    * This is used to tell the client that it should use a proxy.
    */
   USE_PROXY(305, "Use Proxy"),
   
   /**
    * This is used to redirect the client to a resource that has moved.
    */
   TEMPORARY_REDIRECT(307, "Temporary Redirect"),
   
   /**
    * This is used to tell the client they have send an invalid request.
    */
   BAD_REQUEST(400, "Bad Request"),
   
   /**
    * This is used to tell the client that authorization is required.
    */
   UNAUTHORIZED(401, "Unauthorized"),
   
   /**
    * This is used to tell the client that payment is required.
    */
   PAYMENT_REQUIRED(402, "Payment Required"),
   
   /**
    * This is used to tell the client that the resource is forbidden.
    */
   FORBIDDEN(403, "Forbidden"),
   
   /**
    * This is used to tell the client that the resource is not found.
    */
   NOT_FOUND(404, "Not Found"),
   
   /**
    * This is used to tell the client that the method is not allowed.
    */
   METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
   
   /**
    * This is used to tell the client the request is not acceptable.
    */
   NOT_ACCEPTABLE(406, "Not Acceptable"),
   
   /**
    * This is used to tell the client that authentication is required.
    */
   PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
   
   /**
    * This is used to tell the client that the request has timed out.
    */
   REQUEST_TIMEOUT(408, "Request Timeout"),
   
   /**
    * This is used to tell the client that there has been a conflict.
    */
   CONFLICT(409, "Conflict"),
   
   /**
    * This is used to tell the client that the resource has gone.
    */
   GONE(410, "Gone"),
   
   /**
    * This is used to tell the client that a request length is needed.
    */
   LENGTH_REQUIRED(411, "Length Required"),
   
   /**
    * This is used to tell the client that a precondition has failed.
    */
   PRECONDITION_FAILED(412, "Precondition Failed"),
   
   /**
    * This is used to tell the client that the request body is too big.
    */
   REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
   
   /**
    * This is used to tell the client that the request URI is too long.
    */
   REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
   
   /**
    * This is used to tell the client that the content type is invalid.
    */
   UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
   
   /**
    * This is used to tell the client that the range is invalid.
    */
   REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
   
   /**
    * This is used to tell the client that the expectation has failed.
    */
   EXPECTATION_FAILED(417, "Expectation Failed"),
   
   /**
    * This is sent when the request has caused an internal server error.
    */
   INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
   
   /**
    * This is used to tell the client the resource is not implemented.
    */
   NOT_IMPLEMENTED(501, "Not Implemented"),
   
   /**
    * This is used to tell the client that the gateway is invalid.
    */
   BAD_GATEWAY(502, "Bad Gateway"),
   
   /**
    * This is used to tell the client the resource is unavailable.
    */
   SERVICE_UNAVAILABLE(503, "Service Unavailable"),
   
   /**
    * This is used to tell the client there was a gateway timeout.
    */
   GATEWAY_TIMEOUT(504, "Gateway Timeout"),
   
   /**
    * This is used to tell the client the request version is invalid.
    */
   VERSION_NOT_SUPPORTED(505, "Version Not Supported");
   
   /**
    * This is the description of the status this instance represents.
    */
   public final String description;
   
   /**
    * This is the code for the status that this instance represents.
    */
   public final int code;
   
   /**
    * Constructor for the <code>Status</code> object. This will create
    * a status object that is used to represent a response state. It
    * contains a status code and a description of that code.
    * 
    * @param code this is the code that is used for this status
    * @param description this is the description used for the status
    */
   private Status(int code, String description) {
      this.description = description;
      this.code = code;
   }
   
   /**
    * This is used to acquire the code of the status object. This is
    * used in the HTTP response message to tell the client what kind
    * of response this represents. Typically this is used to get a
    * code for a known response state for convenience.
    * 
    * @return the code associated by this status instance
    */
   public int getCode() {
      return code;
   }
   
   /**
    * This is used to provide the status description. The description
    * is the textual description of the response state. It is used
    * so that the response can be interpreted and is a required part
    * of the HTTP response combined with the status code.
    * 
    * @return the description associated by this status instance
    */
   public String getDescription() {
      return description;
   }
   
   /**
    * This is used to provide the status description. The description
    * is the textual description of the response state. It is used
    * so that the response can be interpreted and is a required part
    * of the HTTP response combined with the status code.
    * 
    * @param code this is the code to resolve the description for
    * 
    * @return the description associated by this status code
    */
   public static String getDescription(int code) {
      Status[] list = values();
      
      for(Status status : list) {
         if(status.code == code)
            return status.description;
      }
      return "Unknown";
   }
   
   /**
    * This is used to provide the status value. If the specified 
    * code can not be matched this will return the default HTTP/1.1
    * status code of OK, which may not match the intended status.
    * 
    * @param code this is the code to resolve the status for
    * 
    * @return the status value associated by this status code
    */
   public static Status getStatus(int code) {
      Status[] list = values();
      
      for(Status status : list) {
         if(status.code == code)
            return status;
      }
      return OK;
   }
}
