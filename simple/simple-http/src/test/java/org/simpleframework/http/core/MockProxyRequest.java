package org.simpleframework.http.core;

import java.util.List;

import org.simpleframework.http.ContentType;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Path;
import org.simpleframework.http.Query;
import org.simpleframework.http.RequestHeader;

public class MockProxyRequest extends MockRequest {
   
   private RequestHeader header;
   
   public MockProxyRequest(RequestHeader header) {
      this.header = header;
   }
   
   public long getContentLength() {
      return header.getContentLength();
   }

   public ContentType getContentType() {
      return header.getContentType();
   }

   public String getValue(String name) {
      return header.getValue(name);
   }

   public List<String> getValues(String name) {
      return header.getValues(name);
   }

   public int getMajor() {      
      return header.getMajor();
   }

   public String getMethod() {
      return header.getMethod();
   }

   public int getMinor() {
      return header.getMajor();
   }

   public Path getPath() {
      return header.getPath();
   }

   public Query getQuery() {     
      return header.getQuery();
   }

   public String getTarget() {     
      return header.getTarget();
   }

   
   public String getParameter(String name) {
      return header.getQuery().get(name);
   }
   
   public Cookie getCookie(String name) {
      return header.getCookie(name);
   }   
}
