package org.simpleframework.http.validate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.http.StatusLine;

public class Result {
   
   private final StatusLine statusLine;
   private final Map<String, String> header;
   private final Buffer body;
   
   public Result(StatusLine statusLine, Map<String, String> header, Buffer body) {
      this.statusLine = statusLine;
      this.header = header;
      this.body = body;
   }
   
   public String getContent() throws IOException {
      return body.encode("ISO-8859-1");
   }
   
   public InputStream getInputStream() throws IOException {
      return body.open();
   }
   
   public Map<String, String> getHeader() {
      return header;
   }
   
   public StatusLine getStatusLine() {
      return statusLine;
   }
   
   public Buffer getBody() {
      return body;
   }
   
   public int getInteger(String name) {
      String value = getValue(name);

      if(value != null) {
         return Integer.parseInt(value);
      }
      return -1;
   }
   
   public String getValue(String name) {
      return header.get(name);
   }
   
   public String toString() {
      StringBuilder head = new StringBuilder();
      
      try {
         String content = body.encode();
         head.append(statusLine);
         head.append("\r\n");
         
         for(String name : header.keySet()) {
            String value = header.get(name);
            
            head.append(name);
            head.append(": ");
            head.append(value);
            head.append("\r\n");
         }   
         head.append("\r\n");
         head.append(content); 
      } catch(Exception e) {
         throw new RuntimeException(e);
      }
      return head.toString();
   }
}
