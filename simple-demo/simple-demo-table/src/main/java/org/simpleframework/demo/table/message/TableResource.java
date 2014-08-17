package org.simpleframework.demo.table.message;

import org.simpleframework.demo.http.resource.Resource;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

public class TableResource implements Resource {
   
   private final Resource resource;
   private final String attribute;
   
   public TableResource(Resource resource, String attribute) {
      this.resource = resource;
      this.attribute = attribute;
   }

   @Override
   public void handle(Request request, Response response) throws Throwable {
      String value = request.getParameter(attribute);
      
      if(value != null) {
         response.setCookie(attribute, value);
      }
      resource.handle(request, response);
   }
}
