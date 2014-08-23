/*
 * FrameEncoder.java February 2014
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

import static org.simpleframework.http.socket.FrameType.BINARY;
import static org.simpleframework.http.socket.FrameType.CLOSE;
import static org.simpleframework.http.socket.FrameType.TEXT;
import static org.simpleframework.http.socket.service.ServiceEvent.WRITE_FRAME;

import java.io.IOException;

import org.simpleframework.http.Request;
import org.simpleframework.http.socket.CloseCode;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameType;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.transport.Channel;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>FrameEncoder</code> is used to encode data as frames as
 * defined by RFC 6455. This can encode binary, and text frames as
 * well as control frames. All frames generated are written to the 
 * underlying channel but are not flushed so that multiple frames
 * can be buffered before the final flush is made.
 * 
 * @author Niall Gallagher
 * 
 * @see org.simpleframework.http.socket.service.FrameConnection
 */
class FrameEncoder {

   /**
    * This is the underlying sender used to send the frames.
    */
   private final OutputBarrier barrier;   
   
   /**
    * This is the TCP channel the frames are delivered over.
    */
   private final Channel channel;
   
   /**
    * This is used to trace the traffic on the channel.
    */
   private final Trace trace;      

   /**
    * This is the charset used to encode the text frames with.
    */
   private final String charset;

   /**
    * Constructor for the <code>FrameEncoder</code> object. This is 
    * used to create an encoder to sending frames over the provided
    * channel. Frames send remain unflushed so they can be batched
    * on a single output buffer.
    * 
    * @param request contains the opening handshake information
    */
   public FrameEncoder(Request request) {
      this(request, "UTF-8");
   }
   
   /**
    * Constructor for the <code>FrameEncoder</code> object. This is 
    * used to create an encoder to sending frames over the provided
    * channel. Frames send remain unflushed so they can be batched
    * on a single output buffer.
    * 
    * @param request contains the opening handshake information
    * @param charset this is the character encoding to encode with
    */
   public FrameEncoder(Request request, String charset) {
      this.barrier = new OutputBarrier(request, 5000);
      this.channel = request.getChannel();
      this.trace = channel.getTrace();      
      this.charset = charset;
   }
   
   /**
    * This is used to encode the provided data as a WebSocket frame as
    * of RFC 6455. The encoded data is written to the underlying socket
    * and the number of bytes generated is returned.
    * 
    * @param text this is the data used to encode the frame
    * 
    * @return the size of the generated frame including the header
    */
   public int encode(String text) throws IOException {
      byte[] data = text.getBytes(charset);
      return encode(TEXT, data, true);      
   }
   
   /**
    * This is used to encode the provided data as a WebSocket frame as
    * of RFC 6455. The encoded data is written to the underlying socket
    * and the number of bytes generated is returned.
    * 
    * @param data this is the data used to encode the frame
    * 
    * @return the size of the generated frame including the header
    */
   public int encode(byte[] data) throws IOException {
      return encode(BINARY, data, true);
   }
   
   /**
    * This is used to encode the provided data as a WebSocket frame as
    * of RFC 6455. The encoded data is written to the underlying socket
    * and the number of bytes generated is returned. A close frame with
    * a reason is similar to a text frame with the exception that the
    * first two bytes of the frame payload contains the close code as
    * a two byte integer in network byte order. The body of the close 
    * frame may contain UTF-8 encoded data with a reason, the
    * interpretation of which is not defined by RFC 6455.     
    * 
    * @param reason this is the data used to encode the frame
    * 
    * @return the size of the generated frame including the header
    */
   public int encode(Reason reason) throws IOException {
      CloseCode code = reason.getCode();
      String text = reason.getText();
      byte[] header = code.getData();
      
      if(text != null) {
         byte[] data = text.getBytes(charset);
         byte[] message = new byte[data.length + 2]; 
         
         message[0] = header[0];
         message[1] = header[1];
         
         for(int i = 0; i < data.length; i++) {
            message[i + 2] = data[i];
         }
         return encode(CLOSE, message, true);
      }
      return encode(CLOSE, header, true);      
   }
   
   /**
    * This is used to encode the provided frame as a WebSocket frame as
    * of RFC 6455. The encoded data is written to the underlying socket
    * and the number of bytes generated is returned.
    * 
    * @param frame this is frame that is to be send over the channel
    * 
    * @return the size of the generated frame including the header
    */
   public int encode(Frame frame) throws IOException {
      FrameType code = frame.getType();
      byte[] data = frame.getBinary();
      boolean last = frame.isFinal();
      
      return encode(code, data, last);
   }

   /**
    * This is used to encode the provided frame as a WebSocket frame as
    * of RFC 6455. The encoded data is written to the underlying socket
    * and the number of bytes generated is returned.
    * 
    * @param type this is the type of frame that is to be encoded
    * @param data this is the data used to create the frame
    * @param last determines if the is the last frame in a sequence    
    * 
    * @return the size of the generated frame including the header
    */
   private int encode(FrameType type, byte[] data, boolean last) throws IOException {
      byte[] header = new byte[10];      
      long length = data.length;   
      int count = 0;
      
      if (last) {
         header[0] |= 1 << 7;
      }
      header[0] |= type.code % 128;   

      if (length <= 125) {
         header[1] = (byte) length;
         count = 2;
      } else if (length >= 126 && length <= 65535) {
         header[1] = (byte) 126;
         header[2] = (byte) ((length >>> 8) & 0xff);
         header[3] = (byte) (length & 0xff);
         count = 4;
      } else {
         header[1] = (byte) 127;
         header[2] = (byte) ((length >>> 56) & 0xff);
         header[3] = (byte) ((length >>> 48) & 0xff);
         header[4] = (byte) ((length >>> 40) & 0xff);
         header[5] = (byte) ((length >>> 32) & 0xff);
         header[6] = (byte) ((length >>> 24) & 0xff);
         header[7] = (byte) ((length >>> 16) & 0xff);
         header[8] = (byte) ((length >>> 8) & 0xff);
         header[9] = (byte) (length & 0xff);
         count = 10;
      }
      byte[] reply = new byte[count + data.length];
      
      for (int i = 0; i < count; i++) {
         reply[i] = header[i];
      }
      for (int i = 0; i < length; i++) {
         reply[i + count] = data[i];
      }
      trace.trace(WRITE_FRAME, type);
      barrier.send(reply);
      
      return reply.length;
   }
}
