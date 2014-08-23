/*
 * ReasonExtractor.java February 2014
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

import static org.simpleframework.http.socket.CloseCode.NO_STATUS_CODE;

import org.simpleframework.http.socket.CloseCode;
import org.simpleframework.http.socket.DataConverter;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.Reason;

/**
 * The <code>ReasonExtractor</code> object is used to extract the close
 * reason from a frame payload. If their is no close reason this will
 * return a <code>Reason</code> with just the close code. Finally in 
 * the event of a botched frame being sent with no close code then the
 * close code 1005 is used to indicate no reason.
 * 
 * @author Niall Gallagher
 */
class ReasonExtractor {
   
   /**
    * This is the data converter object used to convert data.
    */
   private final DataConverter converter;
   
   /**
    * Constructor for the <code>ReasonExtractor</code> object. This 
    * is used to create an extractor for close code and the close 
    * reason descriptions. All descriptions are decoded using the
    * UTF-8 character encoding.
    */
   public ReasonExtractor() {
      this.converter = new DataConverter();
   }
   
   /**
    * This is used to extract a reason from the provided frame. The
    * close reason is taken from the first two bytes of the frame
    * payload and the UTF-8 string that follows is the description.
    * 
    * @param frame this is the frame to extract the reason from
    * 
    * @return a reason containing the close code and reason
    */
   public Reason extract(Frame frame) {
      byte[] data = frame.getBinary();
      
      if(data.length > 0) {
         CloseCode code = extractCode(data);
         String text = extractText(data);
         
         return new Reason(code, text);         
      }
      return new Reason(NO_STATUS_CODE);
   }
   
   /**
    * This method is used to extract the UTF-8 description from the
    * frame payload. If there are only two bytes within the payload
    * then this will return null for the reason.
    * 
    * @param data the frame payload to extract the description from
    * 
    * @return returns the description within the payload
    */
   private String extractText(byte[] data) {
      int length = data.length - 2;
      
      if(length > 0) {
         return converter.convert(data, 2, length);
      }
      return null;
   }
   
   /**
    * This method is used to extract the close code. The close code
    * is an two byte integer in network byte order at the start
    * of the close frame payload. This code is required by RFC 6455
    * however if not code is available code 1005 is returned.
    * 
    * @param data the frame payload to extract the description from
    * 
    * @return returns the description within the payload
    */
   private CloseCode extractCode(byte[] data) {
      int length = data.length;
      
      if(length > 0) {
         int high = data[0];
         int low = data[1];
      
         return CloseCode.resolveCode(high, low);
      }
      return NO_STATUS_CODE;
   }
}
