package org.simpleframework.http.validate;

import java.io.IOException;
import java.io.InputStream;

import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.http.Status;
import org.simpleframework.http.StatusLine;

class StatusParser implements StatusLine {

   private Token text = new Token();
   private int code;
   private int major;
   private int minor;
   private byte[] buf;
   private int count;
   private int off;  
   private int len;  

   public StatusParser(Buffer buffer) throws IOException {
      InputStream source = buffer.open();
      this.buf = new byte[source.available()];
      this.len = source.read(buf, 0, buf.length);    
      parse();
   }   

   public int getCode() {
      return code;
   }

   public void setCode(int code) {
      this.code = code;
   }

   public void setMajor(int major) {
      this.major = major;
   }

   public void setMinor(int minor) {
      this.minor = minor;
   }

   public void setDescription(String text) {
      this.text.text = text;
   } 
   
   public String getDescription(){   
      return text.toString();
   }   

   public Status getStatus() {
      return Status.getStatus(code);
   }
   
   public void setStatus(Status status) {
      this.code = status.getCode();
      this.text.text = status.getDescription();
   }  
  
   public int getMajor() {
      return major;
   }

   public int getMinor() {
      return minor;
   }      
   
   private void text() {
      whitespace();
      text.off = off;
      text.len = 0;
      
      while(count < len){
         if(terminal(buf[off])){            
            break;
         }
         text.len++;
         count++;
         off++;
      }       
   }
   
   private void version() {
      off += 5;   /* "HTTP/" */
      count+= 5;
      major();  /* "1" */
      off++;    /* "." */
      count++;
      minor();   /* "1" */
   }

   private void major() {      
      while(count < len){
         if(!digit(buf[off])){            
            break;
         }
         major *= 10;
         major += buf[off++];
         major -= '0';
         count++;
      }        
   }

   private void minor() {
      while(count < len){
         if(!digit(buf[off])){            
            break;
         }
         minor *= 10;
         minor += buf[off++];                  
         minor -= '0';
         count++;
      }           
   } 
   
   private void code() {
      whitespace();
      while(count < len){
         if(!digit(buf[off])){            
            break;
         }
         code *= 10;
         code += buf[off++];                  
         code -= '0';
         count++;
      }           
   } 

   private void whitespace() {
      while(count < len) {
         if(!space(buf[off])){
            break;
         }
         count++;
         off++;
      }
   }

   private boolean space(byte b){
      return b == ' ' || b == '\t';
   }
   
   private boolean terminal(byte b){
      return b == '\r' || b == '\n';
   }

   private boolean digit(byte b) {
      return b >= '0' && b <= '9';
   }

   private void parse() {            
      version();      
      code();
      text();
   }  
   
   public String toString() {
      return String.format("HTTP/%s.%s %s %s", major, minor, code, text);
   }

   private class Token {
      public String text;
      public int off;
      public int len;         
      
      public String toString() {
         try {
            if(text == null) {
               text = new String(buf, off, len, "ISO-8859-1");
            }
         } catch(IOException e) {
            return null;
         }        
         return text;
      }
   }
}
