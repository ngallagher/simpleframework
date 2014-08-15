package org.simpleframework.http.message;

import java.util.List;

import org.simpleframework.http.ContentDisposition;
import org.simpleframework.http.ContentType;
import org.simpleframework.http.message.MessageHeader;
import org.simpleframework.http.message.Segment;
import org.simpleframework.http.parse.ContentDispositionParser;
import org.simpleframework.http.parse.ContentTypeParser;

public class MockSegment implements Segment {
   
   private MessageHeader header;
   
   public MockSegment() {
      this.header = new MessageHeader();
   }
   
   public boolean isFile() {
      return false;
   }
   
   public ContentType getContentType() {
      String value = getValue("Content-Type");
      
      if(value == null) {
         return null; 
      }
      return new ContentTypeParser(value);
   }
   
   public long getContentLength() {
      String value = getValue("Content-Length");
      
      if(value != null) {
         return new Long(value);
      }
      return -1;
   }
   
   public String getTransferEncoding() {
      List<String> list = getValues("Transfer-Encoding");
      
      if(list.size() > 0) {
         return list.get(0);
      }
      return null;
   }
   
   public ContentDisposition getDisposition() {
      String value = getValue("Content-Disposition");
      
      if(value == null) {
         return null;
      }
      return new ContentDispositionParser(value);
   }
   
   public List<String> getValues(String name) {
      return header.getValues(name);
   }
   
   public String getValue(String name) {
      return header.getValue(name);
   }

   public String getValue(String name, int index) {
      return header.getValue(name, index);
   } 

   protected void add(String name, String value) {
      header.addValue(name, value);
   }

   public String getName() {
      return null;
   }

   public String getFileName() {
      return null;
   }      
}