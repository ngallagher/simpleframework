package org.simpleframework.demo.http.resource;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

public class PauseResource implements Resource {

   private final Resource resource;
   private final long pause;

   public PauseResource(Resource resource, long pause) {
      this.resource = resource;
      this.pause = pause;
   }

   @Override
   public void handle(Request request, Response response) throws Throwable {
      Thread.sleep(pause);
      resource.handle(request, response);
   }
}
