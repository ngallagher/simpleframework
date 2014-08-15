package org.simpleframework.demo.http.resource;

import static org.simpleframework.http.Protocol.CONTENT_TYPE;
import static org.simpleframework.http.Status.OK;

import java.io.OutputStream;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

public class StringResource implements Resource {

   private final String encoding;
   private final Status status;
   private final String content;
   private final String type;

   public StringResource(String content, String type, String encoding) {
      this(content, type, encoding, OK);
   }

   public StringResource(String content, String type, String encoding, Status status) {
      this.encoding = encoding;
      this.content = content;
      this.status = status;
      this.type = type;
   }

   @Override
   public void handle(Request request, Response response) throws Exception {
      OutputStream output = response.getOutputStream();
      long length = content.length();
      byte[] data = content.getBytes(encoding);

      response.setCode(status.code);
      response.setDescription(status.description);
      response.setValue(CONTENT_TYPE, type);
      response.setContentLength(length);
      output.write(data);
      output.close();
   }

}
