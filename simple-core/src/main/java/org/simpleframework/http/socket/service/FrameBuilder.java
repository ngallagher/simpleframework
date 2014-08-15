/*
 * FrameBuilder.java February 2014
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

import java.util.Arrays;

import org.simpleframework.http.socket.DataConverter;
import org.simpleframework.http.socket.DataFrame;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameType;

/**
 * The <code>FrameBuilder</code> object is used to create an object 
 * that interprets a frame header to produce frame objects. For
 * efficiency this converts binary data to the native frame data
 * type, which avoids memory churn. 
 * 
 * @author Niall Gallagher
 *
 * @see org.simpleframework.http.socket.service.FrameConsumer
 */
class FrameBuilder {
   
   /**
    * This converts binary data to a UTF-8 string for text frames.
    */
   private final DataConverter converter;
   
   /**
    * This is used to determine the type of frames to create.
    */
   private final FrameHeader header;
   
   /**
    * Constructor for the <code>FrameBuilder</code> object. This acts
    * as a factory for frame objects by using the provided header to
    * determine the frame type to be created.
    * 
    * @param header the header used to determine the frame type
    */
   public FrameBuilder(FrameHeader header) {
      this.converter = new DataConverter();
      this.header = header;
   }

   /**
    * This is used to create a frame object to represent the data that
    * has been consumed. The frame created will contain either a copy of 
    * the provided byte buffer or a text string encoded in UTF-8. To 
    * avoid memory churn this method should be used sparingly.
    * 
    * @return this returns a frame created from the consumed bytes
    */
   public Frame create(byte[] data, int count) {
      FrameType type = header.getType();
      
      if(type.isText()) {
         return createText(data, count);
      }
      return createBinary(data, count);
   }   
   
   /**
    * This is used to create a frame object from the provided data. 
    * The resulting frame will contain a UTF-8 encoding of the data
    * to ensure that data conversion needs to be performed only once.
    * 
    * @param data this is the data to convert to a new frame
    * @param count this is the number of bytes in the frame
    * 
    * @return a new frame containing the text
    */
   private Frame createText(byte[] data, int count) {
      FrameType type = header.getType();
      String text = converter.convert(data, 0, count);
      
      if(header.isFinal()) {
         return new DataFrame(type, text, true);
      }
      return new DataFrame(type, text, false);
   }
   
   /**
    * This is used to create a frame object from the provided data. 
    * The resulting frame will contain a copy of the data to ensure 
    * that the frame is immutable.
    * 
    * @param data this is the data to convert to a new frame
    * @param count this is the number of bytes in the frame
    * 
    * @return a new frame containing a copy of the provided data
    */
   private Frame createBinary(byte[] data, int count) {
      FrameType type = header.getType();
      byte[] copy = Arrays.copyOf(data, count);
      
      if(header.isFinal()) {
         return new DataFrame(type, copy, true);
      }
      return new DataFrame(type, copy, false);
   }
}
