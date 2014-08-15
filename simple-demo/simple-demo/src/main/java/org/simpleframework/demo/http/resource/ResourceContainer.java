package org.simpleframework.demo.http.resource;

import static org.simpleframework.http.Protocol.DATE;
import static org.simpleframework.http.Status.OK;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;

public class ResourceContainer implements Container {

   private static final Logger LOG = Logger.getLogger(ResourceContainer.class);

   private final ResourceEngine engine;
   private final Resource failure;
   private final Status status;

   public ResourceContainer(ResourceEngine engine) {
      this(engine, OK);
   }

   public ResourceContainer(ResourceEngine engine, Status status) {
      this(engine, null, status);
   }

   public ResourceContainer(ResourceEngine engine, Resource failure) {
      this(engine, failure, OK);
   }

   public ResourceContainer(ResourceEngine engine, Resource failure, Status status) {
      this.failure = failure;
      this.engine = engine;
      this.status = status;
   }

   @Override
   public void handle(Request request, Response response) {
      long time = System.currentTimeMillis();

      try {
         Resource resource = engine.resolve(request, response);

         response.setDate(DATE, time);
         response.setCode(status.code);
         response.setDescription(status.description);
         resource.handle(request, response);
      } catch (Throwable cause) {
         LOG.info("Error handling resource", cause);

         try {
            if (failure != null) {
               response.reset();
               failure.handle(request, response);
            }
         } catch (Throwable fatal) {
            LOG.info("Could not send an error response", fatal);
         }
      } finally {
         try {
            response.close();
         } catch (IOException ignore) {
            LOG.info("Could not close response", ignore);
         }
      }
   }
}
