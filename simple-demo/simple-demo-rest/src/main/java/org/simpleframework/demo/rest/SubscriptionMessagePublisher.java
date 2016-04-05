package org.simpleframework.demo.rest;

import java.util.List;

import org.simpleframework.http.Method;

import com.google.gson.Gson;

public class SubscriptionMessagePublisher implements MessagePublisher {
   
   private final SubscriptionManager manager;
   private final Gson gson;

   public SubscriptionMessagePublisher(SubscriptionManager manager) {
      this.gson = new Gson();
      this.manager = manager;
   }
   
   @Override
   public void publish(Object value) throws Exception {
      Class type = value.getClass();
      MatchRequest request = new MatchRequest(type);
      List<String> matches = manager.match(request);
      String data = gson.toJson(value);
      
      for(String address : matches) {
         RequestBuilder builder = new RequestBuilder(address);
         
         builder.setMethod(Method.POST);
         builder.setBody(data);
         builder.execute(String.class);
      }
   }

}
