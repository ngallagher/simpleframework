package org.simpleframework.demo.js;

import static org.simpleframework.http.Protocol.CONTENT_ENCODING;
import static org.simpleframework.http.Protocol.CONTENT_TYPE;
import static org.simpleframework.http.Status.OK;

import java.io.OutputStream;

import org.simpleframework.demo.http.resource.Resource;
import org.simpleframework.http.Path;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

public class ScriptResource implements Resource {

   private final ScriptProcessor processor;
   private final String encoding;
   private final String type;
   
   public ScriptResource(ScriptProcessor processor, String type, String encoding) {
      this.processor = processor;
      this.encoding = encoding;
      this.type = type;
   }

   @Override
   public void handle(Request request, Response response) throws Throwable {
      Path path = request.getPath();
      String target = path.getPath();      
      OutputStream output = response.getOutputStream();    
      byte[] script = processor.process(target);
      int length = script.length;

      response.setContentLength(length);
      response.setStatus(OK);
      response.setValue(CONTENT_ENCODING, encoding); 
      response.setValue(CONTENT_TYPE, type);
      response.setContentLength(length);
      output.write(script);      
      output.close();
   }
}
