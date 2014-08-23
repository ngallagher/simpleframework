package org.simpleframework.http.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.simpleframework.http.Part;
import org.simpleframework.http.message.PartData;


public class MockBody implements Body {
   
   protected PartData list;
   
   protected String body;
   
   public MockBody() {
      this("");
   }
   
   public MockBody(String body) {
      this.list = new PartData();
      this.body = body;
   }
   
   public List<Part> getParts() {
      return list.getParts();
   }
   
   public Part getPart(String name) {
      return list.getPart(name);
   }
   
   public String getContent(String charset) {
      return body;
   }
   
   public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(body.getBytes("UTF-8"));
   }

   public String getContent() throws IOException {
      return body;
   }

}
