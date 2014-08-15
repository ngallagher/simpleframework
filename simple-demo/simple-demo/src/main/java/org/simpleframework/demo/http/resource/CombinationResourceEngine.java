package org.simpleframework.demo.http.resource;

import java.util.List;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

public class CombinationResourceEngine implements ResourceEngine {

   private final List<ResourceEngine> engines;

   public CombinationResourceEngine(List<ResourceEngine> engines) {
      this.engines = engines;
   }

   @Override
   public Resource resolve(Request request, Response response) throws Exception {
      for (ResourceEngine engine : engines) {
         Resource resource = engine.resolve(request, response);

         if (resource != null) {
            return resource;
         }
      }
      return null;
   }

}
