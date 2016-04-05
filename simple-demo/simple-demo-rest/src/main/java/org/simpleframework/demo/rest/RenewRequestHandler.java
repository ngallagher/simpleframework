package org.simpleframework.demo.rest;

import java.io.OutputStream;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

import com.google.gson.Gson;

public class RenewRequestHandler implements RequestHandler<RenewRequest> {
   
   private final SubscriptionManager manager;
   private final Gson gson;
   
   public RenewRequestHandler(SubscriptionManager manager) {
      this.gson = new Gson();
      this.manager = manager;
   }

   @Override
   public void handle(Request request, Response response, RenewRequest message) throws Exception {
      String key = message.getKey();
      StatusResponse status = new StatusResponse(key, true);
      manager.renew(message);
      String content = gson.toJson(status);
      OutputStream output = response.getOutputStream();
      byte[] data = content.getBytes("UTF-8");
      
      response.setContentType("text/json");
      response.setContentLength(data.length);
      output.write(data);
      output.flush();
      response.close();
      
      
   }

}
