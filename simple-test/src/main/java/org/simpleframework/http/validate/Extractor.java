package org.simpleframework.http.validate;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.HashMap;
import java.util.Map;

import org.simpleframework.common.buffer.ArrayBuffer;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.http.StatusLine;

public class Extractor {
   
   private static final String CONTENT_LENGTH = "Content-Length";
   private static final byte[] LINE = new byte[] {13,10};
   private static final byte[] REQUEST = new byte[] {13,10,13,10};
   
   private final Buffer buffer;
   private final byte[] block;
   private final boolean debug;
   
   public Extractor(boolean debug) {
      this.buffer = new ArrayBuffer(512, 2048);
      this.block = new byte[1024];
      this.debug = debug;
   }
   
   public Result extractResponse(PushbackInputStream in) throws IOException {
      StatusLine statusLine = extractStatus(in);
      
      if(statusLine == null) {
         return null;
      }
      Map<String, String> header = extractHeader(in);
      Buffer body = extractBody(header, in);
      
      return new Result(statusLine, header, body);
   }
   
   public StatusLine extractStatus(PushbackInputStream in) throws IOException {
      Buffer buffer = extractLine(in);
      
      if(buffer == null) {
         return null;
      }
      return new StatusParser(buffer);      
   }
   
   public Buffer extractLine(PushbackInputStream in) throws IOException {
      int byteCount = 0;
      int count = 0;
      int pos = 0;

      read: {
         buffer.clear();
         
         while((count = in.read(block)) != -1) {
            int seek = 0;
            
            while(seek < count) {
               if(block[seek++] != LINE[pos++]) {
                  pos = 0;
               }  
               if(pos == LINE.length) {
                  buffer.append(block, 0, seek);
                  in.unread(block, seek, count - seek);
                  break read;
               }
               byteCount++;
            }
            try {
               buffer.append(block, 0, seek);
            } catch(Exception e) {
               throw new IOException("Append failed with "+buffer.encode());
            }
         }
      }
      if(byteCount <= 0) {
         return null;
      }
      return buffer;      
   }
   
   
   public Map<String, String> extractHeader(PushbackInputStream in) throws IOException {
      Map<String, String> map = new HashMap<String, String>();
      int count = 0;
      int pos = 0;

      read: {
         buffer.clear();
         
         while((count = in.read(block)) != -1) {
            int seek = 0;
            
            while(seek < count) {
               if(block[seek++] != REQUEST[pos++]) {
                  pos = 0;
               }  
               if(pos == REQUEST.length) {
                  buffer.append(block, 0, seek);
                  in.unread(block, seek, count - seek);
                  break read;
               }
            }
            buffer.append(block, 0, seek);
         }
      }    
      HeaderParser parser = new HeaderParser(buffer);
      
      while(parser.hasMore()) {
         Header header = parser.next();
         String name = header.getName();
         String value = header.getValue();
         
         map.put(name, value);
      }
      return map;      
   }
   
   public Buffer extractBody(Map<String, String> map, PushbackInputStream in) throws IOException {
      String contentLength = map.get(CONTENT_LENGTH);
      int length = Integer.parseInt(contentLength);
      Buffer body = new ArrayBuffer(length, length);

      while(length > 0) {
         int remaining = Math.min(length, block.length);
         int count = in.read(block, 0, remaining);
         
         if(count == -1) {
            break;
         } else {
        	 if(debug) {
       		//System.out.println("Reading body ["+length+"] remains");
        	 }
         }
         body.append(block, 0, count);
         length -= count;
      }     
      return body;      
   }
   
   public Buffer extractAll(InputStream in) throws IOException {
      Buffer buffer = new ArrayBuffer(2048, 1048576);
      int count = 0;
      
      while((count = in.read(block)) != -1) {
         buffer.append(block, 0, count);
      }   
      return buffer;
   }
}
