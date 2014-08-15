package org.simpleframework.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Test for fast case insensitive mapping for headers that have been taken
 * from the request HTTP header or added to the response HTTP header.
 * 
 * @author Niall Gallagher
 */
public class KeyTest extends TestCase {
   
   public class Index implements Name {
      
      private final String value;
      
      public Index(String value) {
         this.value = value.toLowerCase();
      }
      
      public int hashCode() {
         return value.hashCode();
      }
      
      public boolean equals(Object key) {
         if(key instanceof Name) {
            return key.equals(value);
         }
         if(key instanceof String) {
            return key.equals(value);
         }
         return false;
      }
   }
   
   public interface Name {
      
      public int hashCode();
      public boolean equals(Object value);
   }
   
   public class ArrayName implements Name {
      
      private String cache;
      private byte[] array;
      private int off;
      private int size;
      private int hash;      

      public ArrayName(byte[] array) {
         this(array, 0, array.length);
      }
      
      public ArrayName(byte[] array, int off, int size) {
         this.array = array;
         this.size = size;
         this.off = off;
      }
      
      public boolean equals(Object value) {
         if(value instanceof String) {
            String text = value.toString();
            
            return equals(text);
         }
         return false;
      }
      
      public boolean equals(String value) {
         int length = value.length();
         
         if(length != size) {
            return false;
         }
         for(int i = 0; i < size; i++) {
            int left = value.charAt(i);            
            int right = array[off + i];
            
            if(right >= 'A' && right <= 'Z') {
               right = (right - 'A') + 'a';
            }            
            if(left != right) {
               return false;
            }
         }
         return true;
      }
      
      public int hashCode() {
         int code = hash;
         
         if(code == 0) {
            int pos = off;

            for(int i = 0; i < size; i++) {
               int next = array[pos++];
               
               if(next >= 'A' && next <= 'Z') {
                  next = (next - 'A') + 'a';
               }
               code = 31*code + next;
            }
            hash = code;
         }
         return code;
      }
   } 
   
   public class StringName implements Name {
      
      private final String value;
      private final String key;
      
      public StringName(String value) {
         this.key = value.toLowerCase();
         this.value = value;
      }
      
      public int hashCode() {
         return key.hashCode();
      }

      public boolean equals(Object value) {
         return value.equals(key);
      }
   }
   
   public class NameTable<T> {
      
      private final Map<Name, T> map;
      
      public NameTable() {
         this.map = new HashMap<Name, T>();
      }
   
      public void put(Name key, T value) {
         map.put(key, value);
      }
      
      public void put(String text, T value) {
         Name key = new StringName(text);

         map.put(key, value);
      }

      public T get(String key) {     
         Index index = new Index(key);
    
         return map.get(index);
      }
      
      public T remove(String key) {
         Index index = new Index(key);

         return map.remove(index);
      }
   }
   
   public void testName() {
      Name contentLength = new ArrayName("Content-Length".getBytes());
      Name contentType = new ArrayName("Content-Type".getBytes());
      Name transferEncoding = new ArrayName("Transfer-Encoding".getBytes());
      Name userAgent = new ArrayName("User-Agent".getBytes());      
      NameTable<String> map = new NameTable<String>();

      assertEquals(contentLength.hashCode(), "Content-Length".toLowerCase().hashCode());
      assertEquals(contentType.hashCode(), "Content-Type".toLowerCase().hashCode());
      assertEquals(transferEncoding.hashCode(), "Transfer-Encoding".toLowerCase().hashCode());
      assertEquals(userAgent.hashCode(), "User-Agent".toLowerCase().hashCode());
      
      map.put(contentLength, "1024");
      map.put(contentType, "text/html");
      map.put(transferEncoding, "chunked");
      map.put(userAgent, "Mozilla/4.0");
      map.put("Date", "18/11/1977");
      map.put("Accept", "text/plain, text/html, image/gif");
      
      assertEquals(map.get("Content-Length"), "1024");
      assertEquals(map.get("CONTENT-LENGTH"), "1024");
      assertEquals(map.get("content-length"), "1024");
      assertEquals(map.get("Content-length"), "1024");
      assertEquals(map.get("Content-Type"), "text/html");
      assertEquals(map.get("Transfer-Encoding"), "chunked");
      assertEquals(map.get("USER-AGENT"), "Mozilla/4.0");
      assertEquals(map.get("Accept"), "text/plain, text/html, image/gif");
      assertEquals(map.get("ACCEPT"), "text/plain, text/html, image/gif");
      assertEquals(map.get("accept"), "text/plain, text/html, image/gif");      
      assertEquals(map.get("DATE"), "18/11/1977");
      assertEquals(map.get("Date"), "18/11/1977");
      assertEquals(map.get("date"), "18/11/1977");
   }
}
