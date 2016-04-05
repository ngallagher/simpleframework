package org.simpleframework.demo.rest;

import java.io.OutputStream;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

import com.google.gson.Gson;

public class SubscribeRequestHandler implements RequestHandler<SubscribeRequest> {
   
   private final SubscriptionManager manager;
   private final Gson gson;
   
   public SubscribeRequestHandler(SubscriptionManager manager) {
      this.gson = new Gson();
      this.manager = manager;
   }

   @Override
   public void handle(Request request, Response response, SubscribeRequest message) throws Exception {
      String key = message.getKey();
      StatusResponse status = new StatusResponse(key, true);
      manager.subscribe(message);
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
