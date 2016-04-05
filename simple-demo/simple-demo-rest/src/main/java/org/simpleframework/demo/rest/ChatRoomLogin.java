package org.simpleframework.demo.rest;

import org.simpleframework.demo.http.resource.Resource;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

public class ChatRoomLogin implements Resource {
   
   private final Resource resource;
   
   public ChatRoomLogin(Resource resource) {
      this.resource = resource;
   }

   @Override
   public void handle(Request request, Response response) throws Throwable {
      String name = request.getParameter("user");
      
      response.setCookie("user", name);
      resource.handle(request, response);
   }

}
