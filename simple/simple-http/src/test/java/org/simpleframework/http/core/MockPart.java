package org.simpleframework.http.core;

import java.io.IOException;

import org.simpleframework.http.ContentDisposition;
import org.simpleframework.http.ContentType;
import org.simpleframework.http.Part;
import org.simpleframework.http.message.MockBody;

public class MockPart extends MockBody implements Part {

   private String name;
   private boolean file;
   
   public MockPart(String name, String body, boolean file) {
      super(body);    
      this.file = file;
      this.name = name;
   }

   public String getContent() throws IOException {      
      return body;
   }

   public ContentType getContentType() {     
      return null;
   }

   public ContentDisposition getDisposition() {      
      return null;
   }

   public String getHeader(String name) {
      return null;
   }

   public String getName() {     
      return name;
   }

   public boolean isFile() {     
      return file;
   }

   public String getFileName() {
      return null;
   }

}
