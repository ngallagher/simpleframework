package org.simpleframework.http.validate;

import java.io.IOException;
import java.io.InputStream;

import org.simpleframework.common.buffer.Buffer;


class HeaderParser {    

   private Token name = new Token();
   private Token value = new Token();
   private byte[] buf;  
   private int count;
   private int off;  
   private int len;
   private boolean parsed = false;
   
   public HeaderParser(Buffer buffer) throws IOException {
      InputStream source = buffer.open();
      this.buf = new byte[source.available()];
      this.len = source.read(buf, 0, buf.length);     
   }   

   public Header next() {
      if(hasMore()) {
         parsed = false;
         return new Entry(name, value, buf);
      }
      return null;
   }

   public boolean hasMore() {
      if(parsed) return true;
      if(count + 1 < len) {  /* check for CRLF ending */
         byte a = buf[off];
         byte b = buf[off + 1];            
         if(a == 13 && b == 10) {
            return false;
         }
      }        
      parse();
      if(name.len > 0){
         parsed = true;
         return true;
      }
      return false;
   }    
     
   private void description() {  
      while(count < len) {
         if(buf[off] == 10){ /* cannot be folded see RFC 2616 pg35 */
            count++; /* skip past LF */
            off++;
            break;
         }
         count++;
         off++;            
      }    
   }

   private void parse() {        
      name();     /* Some-Header: */
      value();    /* some-value CRLF */
   }  

   private void name() {
      whitespace();  
      name.off = off;
      name.len = 0;
        
      while(count < len){
         if(buf[off] == ':') {
            off++;   /* skip past the colon */
            count++; 
            break;
         }
         name.len++;
         count++;
         off++;
      }                 
   }

   private void value() {
      whitespace();  
      value.off = off;
      value.len = 0;
        
      for(int mark= 0; count < len;){
         if(terminal(buf[off])) {  /* CR  or  LF */
            for(int i = 0; count < len; i++){
               if(buf[off] == 10) {
                  count++;  /* skip the LF */
                  off++;
                  if(space(buf[off])) {
                     value.len += i;  /* acount for bytes examined */
                     break; /* folding line */
                  } 
                  return; /* not a folding line */
               } 
               count++;
               off++;
            }       
         } else {
            if(!space(buf[off])){
               value.len= ++mark;
            } else {
               mark++;
            }
            count++;
            off++;   
         }
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

   private boolean space(byte b) {
      return b == ' ' || b == '\t';
   }

   private boolean terminal(byte b){
      return b == 13 || b == 10;
   }
  
   private class Entry implements Header{

      private Token name = new Token();
      private Token value = new Token();
      private Cache cache = new Cache();
      private String str;
      private byte[] buf;

      public Entry(Token name, Token value, byte[] buf) {        
         this.name.off = name.off;
         this.name.len = name.len;        
         this.value.off = value.off;
         this.value.len = value.len;        
         this.buf = buf;
      }  
      
      public String getName(){
         if(cache.name == null){   
            cache.name = name.toString();
         }
         return cache.name;
      }  
     
      public String getValue(){
         if(cache.val == null){
            cache.val = value.toString();
         }
         return cache.val;
      }
      
      public String toString(){
         if(str == null){
            str = getName()+ ": "+getValue();
         }  
         return str;
      }
      
      public boolean nameMatches(String name) {      
         if(name == null) return false;
         if(this.name.len != name.length()) {
            return false;
         }
         if(cache.name != null){
            return cache.name.equalsIgnoreCase(name);
         }
         for(int i = 0; i < this.name.len; i++) {
            byte a = toLower(buf[this.name.off + i]);            
            byte b = toLower(name.charAt(i));
            
            if(a != b) {            
               return false;
            }
         }
         return true;        
      } 
      
      private byte toLower(char c) {
         return toLower((byte)c);
      }

      private byte toLower(byte b) {
         if(b >= 'A' && b <= 'Z') {
            return (byte)((b - 'A') + 'a');
         }
         return b;                               
      }    

      private class Cache{
         public String name;
         public String val;
      }
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
