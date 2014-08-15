package org.simpleframework.demo.http.resource;

import java.util.Map;
import java.util.Set;

import org.simpleframework.demo.collection.LeastRecentlyUsedMap;
import org.simpleframework.http.Path;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

public class RegularExpressionEngine implements ResourceEngine {

   private final Map<String, Resource> resources;
   private final Map<String, Resource> cache;
   private final Resource fallback;

   public RegularExpressionEngine(Map<String, Resource> resources, Resource fallback) {
      this(resources, fallback, 10000);
   }

   public RegularExpressionEngine(Map<String, Resource> resources, Resource fallback, int capacity) {
      this.cache = new LeastRecentlyUsedMap<String, Resource>(capacity);
      this.resources = resources;
      this.fallback = fallback;
   }

   public synchronized Resource resolve(Request request, Response response) {
      Path path = request.getPath();
      String target = path.getPath();
      Resource resource = cache.get(target);

      if (resource == null) {
         resource = match(request, target);

         if (resource != null) {
            cache.put(target, resource);
         }
      }
      return resource;
   }

   private synchronized Resource match(Request request, String target) {
      Set<String> mappings = resources.keySet();

      for (String mapping : mappings) {
         Resource resource = resources.get(mapping);

         if (target.matches(mapping)) {
            return resource;
         }
      }
      return fallback;
   }
}
