/*
 * StatusLine.java February 2001
 *
 * Copyright (C) 2001, Niall Gallagher <niallg@users.sf.net>
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
 * The <code>StatusLine</code> is used to represent a HTTP status 
 * line. This provides several convenience methods that can be used 
 * to manipulate a HTTP status line. see the RFC (RFC 2616) for the 
 * syntax of a status line.
 *
 * @author Niall Gallagher
 */ 
public interface StatusLine {

   /**
    * This represents the status code of the HTTP response. 
    * The response code represents the type of message that is
    * being sent to the client. For a description of the codes
    * see RFC 2616 section 10, Status Code Definitions. 
    *
    * @return the status code that this HTTP response has
    */ 
   int getCode();
     
   /**
    * This method allows the status for the response to be 
    * changed. This MUST be reflected the the response content
    * given to the client. For a description of the codes see
    * RFC 2616 section 10, Status Code Definitions.
    *
    * @param code the new status code for the HTTP response
    */ 
   void setCode(int code);

   /**
    * This can be used to retrieve the text of a HTTP status
    * line. This is the text description for the status code.
    * This should match the status code specified by the RFC.
    *
    * @return the message description of the response
    */ 
   String getDescription();

   /**
    * This is used to set the text of the HTTP status line.
    * This should match the status code specified by the RFC.
    *
    * @param text the descriptive text message of the status
    */ 
   void setDescription(String text);
   
   /**
    * This is used to acquire the status from the response. 
    * The <code>Status</code> object returns represents the
    * code that has been set on the response, it does not
    * necessarily represent the description in the response.
    * 
    * @return this is the response for this status line
    */
   Status getStatus();
   
   /**
    * This is used to set the status code and description
    * for this response. Setting the code and description in
    * this manner provides a much more convenient way to set
    * the response status line details.
    * 
    * @param status this is the status to set on the response
    */
   void setStatus(Status status);

   /**
    * This can be used to get the major number from a HTTP
    * version. The major version corresponds to the major 
    * type that is the 1 of a HTTP/1.0 version string.
    *
    * @return the major version number for the response
    */ 
   int getMajor();

   /**
    * This can be used to specify the major version. This
    * should be the major version of the HTTP request.
    *
    * @param major this is the major number desired
    */ 
   void setMajor(int major);

   /**
    * This can be used to get the minor number from a HTTP
    * version. The major version corresponds to the minor
    * type that is the 0 of a HTTP/1.0 version string.
    *
    * @return the major version number for the response
    */ 
   int getMinor();
   
   /**
    * This can be used to specify the minor version. This
    * should not be set to zero if the HTTP request was 
    * for HTTP/1.1. The response must be equal or higher.
    *
    * @param minor this is the minor number desired
    */ 
   void setMinor(int minor);
}
