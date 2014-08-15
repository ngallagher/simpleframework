package org.simpleframework.demo.http.resource;

import static org.simpleframework.http.Protocol.LOCATION;
import static org.simpleframework.http.Status.FOUND;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

public class RedirectResource implements Resource {

   private final Resource resource;   
   private final String location;
   
   public RedirectResource(Resource resource, String location) {
      this.resource = resource;
      this.location = location;
   }
   
   public void handle(Request request, Response response) throws Throwable {
      response.setStatus(FOUND);
      response.setValue(LOCATION, location);
      resource.handle(request, response);
   }
}
