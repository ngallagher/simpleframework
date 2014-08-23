/*
 * ChunkedConsumer.java February 2007
 *
 * Copyright (C) 2007, Niall Gallagher <niallg@users.sf.net>
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

package org.simpleframework.http.message;

import java.io.IOException;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.Buffer;

/**
 * The <code>ChunkedConsumer</code> is reads an decodes a stream
 * using the chunked transfer coding. This is used so that any data
 * sent in the chunked transfer coding can be decoded. All bytes are
 * appended to an internal buffer so that they can be read without
 * having to parse the encoding. 
 * <pre>
 *
 *    length := 0
 *    read chunk-size, chunk-extension (if any) and CRLF
 *    while (chunk-size &gt; 0) {
 *       read chunk-data and CRLF
 *       append chunk-data to entity-body
 *       length := length + chunk-size
 *       read chunk-size and CRLF
 *    }
 *    read entity-header
 *    while (entity-header not empty) {
 *       append entity-header to existing header fields
 *       read entity-header
 *    }
 *
 * </pre>
 * The above algorithm is taken from RFC 2616 section 19.4.6. This
 * coding scheme is used in HTTP pipelines so that dynamic content,
 * that is, content with which a length cannot be determined does
 * not require a connection close to delimit the message body. 
 *
 * @author Niall Gallagher
 */
public class ChunkedConsumer extends UpdateConsumer {
  
   /**
    * This is used to create the internal buffer for the body.
    */         
   private Allocator allocator;
   
   /**
    * This is the internal buffer used to capture the body read.
    */  
   private Buffer buffer;
   
   /**
    * This is used to determine whether a full chunk has been read.
    */  
   private boolean terminal;
   
   /**
    * This is used to determine if the zero length chunk was read.
    */  
   private boolean last;
 
   /**
    * This is used to accumulate the bytes of the chunk size line.
    */   
   private byte line[];
   
   /**
    * This is the number of bytes appended to the line buffer.
    */ 
   private int count;
   
   /**
    * This is the number of bytes left in the current chunk.
    */ 
   private int chunk;
   
   /**
    * Constructor for the <code>ChunkedConsumer</code> object. This 
    * is used to create a consumer that reads chunked encoded data and
    * appended that data in decoded form to an internal buffer so that
    * it can be read in a clean decoded fromat.
    *
    * @param allocator this is used to allocate the internal buffer
    */
   public ChunkedConsumer(Allocator allocator) {
      this(allocator, 1024);
   }
   
   /**
    * Constructor for the <code>ChunkedConsumer</code> object. This 
    * is used to create a consumer that reads chunked encoded data and
    * appended that data in decoded form to an internal buffer so that
    * it can be read in a clean decoded fromat.
    *
    * @param allocator this is used to allocate the internal buffer
    * @param chunk this is the maximum size line allowed
    */   
   private ChunkedConsumer(Allocator allocator, int chunk) {    
      this.line = new byte[chunk];
      this.allocator = allocator;  
   }   
   
   /**
    * This is used to acquire the body that has been consumed. This
    * will return a body which can be used to read the content of
    * the message, also if the request is multipart upload then all
    * of the parts are provided as <code>Attachment</code> objects. 
    * Each part can then be read as an individual message.
    *  
    * @return the body that has been consumed by this instance
    */
   public Body getBody() {
      return new BufferBody(buffer);
   }
   
   /** 
    * This method is used to append the contents of the array to the
    * internal buffer. The appended bytes can be acquired from the
    * internal buffer using an <code>InputStream</code>, or the text
    * of the appended bytes can be acquired by encoding the bytes.   
    *
    * @param array this is the array of bytes to be appended
    * @param off this is the start offset in the array to read from
    * @param len this is the number of bytes to write to the buffer  
    */
   private void append(byte[] array, int off, int len) throws IOException {
      if(buffer == null) {
         buffer = allocator.allocate();
      }
      buffer.append(array, off, len);
   }

   /**
    * This is used to process the bytes that have been read from the
    * cursor. This will keep reading bytes from the stream until such
    * time as the zero length chunk has been read from the stream. If
    * the zero length chunk is encountered then the overflow count is
    * returned so it can be used to reset the cursor.
    *
    * @param array this is a chunk read from the cursor
    * @param off this is the offset within the array the chunk starts
    * @param size this is the number of bytes within the array
    *
    * @return this returns the number of bytes overflow that is read
    */          
   @Override
   protected int update(byte[] array, int off, int size) throws IOException {
	  int mark = off + size;
      
      while(off < mark){
         if(terminal || last) {
            while(off < mark) {
               if(array[off++] == '\n') { // CR[LF]
                  if(last) { // 0; CRLFCR[LF]
                     finished = true;
                     return mark - off;
                  }
                  terminal = false;
                  break;
               }
            }
         } else if(chunk == 0) {
            while(chunk == 0) {
               if(off >= mark) {
                  break;
               } else if(array[off++] == '\n') { // CR[LF] 
                  parse();
                  
                  if(chunk == 0) { // 0; CR[LF]CRLF                	 
                     last = true;
                     break;
                  }
               } else {
                  line[count++] = array[off-1];
               }                      
            }            
         } else {
            int write = Math.min(mark - off, chunk);
            
            append(array, off, write); 
            chunk -= write;
            off += write;
            
            if(chunk == 0) { // []CRLF
               terminal = true;
            }
         }
      }
      return 0;
   }   
   
   /**
    * This method is used to convert the size in hexidecimal to a 
    * decimal <code>int</code>. This will use the specified number 
    * of bytes from the internal buffer and parse each character 
    * read as a hexidecimal character. This stops interpreting the
    * size line when a non-hexidecimal character is encountered.
    */
   private void parse() throws IOException {
      int off = 0;
      
      while(off < count) {
         int octet = toDecimal(line[off]);
            
         if(octet < 0){
            if(off < 1) {
               throw new IOException("Invalid chunk size line");
            }
            break;
         }
         chunk <<= 4; 
         chunk ^= octet;
         off++;
      }
      count = 0;
   }

   /**
    * This performs a conversion from a character to an integer. If
    * the character given, as a <code>byte</code>, is a hexidecimal
    * char this will convert it into its integer equivelant. So a 
    * char of <code>A</code> is converted into <code>10</code>.
    *
    * @param octet this is an ISO 8869-1 hexidecimal character
    *
    * @return returns the hex character into its decinal value
    */   
   private int toDecimal(byte octet){
      if(octet >= 'A' && octet <= 'Z') {
         return (octet - 'A') + 10;
      }
      if(octet >= '0' && octet <= '9') {
         return octet - '0';   
      } 
      if(octet >= 'a' && octet <= 'f') {
         return (octet - 'a') + 10;
      }
      return -1;
   }
}


