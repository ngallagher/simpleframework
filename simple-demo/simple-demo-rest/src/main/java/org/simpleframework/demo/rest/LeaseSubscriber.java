package org.simpleframework.demo.rest;

import java.util.concurrent.ThreadFactory;

import org.simpleframework.common.thread.DaemonFactory;
import org.simpleframework.http.Method;

import com.google.gson.Gson;

public class LeaseSubscriber {

   private final ThreadFactory factory;
   private final Gson gson;
   private final String remote;
   private final String local;
   private final String key;
   
   public LeaseSubscriber(String key, String remote, String local) {
      this.factory = new DaemonFactory(SubscriptionPublisher.class);
      this.gson = new Gson();
      this.key = key;
      this.local = local;
      this.remote = remote;
   }
   
   public void subscribe(String address, String filter) throws Exception {
      SubscribeRequest request = new SubscribeRequest(key, local, filter, 10000);
      RequestBuilder builder = new RequestBuilder(remote);
      SubscriptionPublisher publisher = new SubscriptionPublisher(remote);
      String value = gson.toJson(request);
      Thread thread = factory.newThread(publisher);
      
      builder.setPath("/" + MessageServer.SUBSCRIBE_PREFIX);
      builder.setMethod(Method.POST);
      builder.setBody(value);
      builder.execute(String.class);
      thread.start();
   }
   
   private class SubscriptionPublisher implements Runnable {
      
      private final String remote;
      
      public SubscriptionPublisher(String remote) {
         this.remote = remote;
      }
      
      @Override
      public void run() {
         while(true) {
            try {
               RenewRequest request = new RenewRequest(key, 10000);
               RequestBuilder builder = new RequestBuilder(remote);
               String value = gson.toJson(request);
               
               builder.setPath("/" + MessageServer.RENEW_PREFIX);
               builder.setMethod(Method.POST);
               builder.setBody(value);
               builder.execute(String.class);
               Thread.sleep(5000);
            } catch(Exception e) {
               e.printStackTrace();
            }
         }
      }
      
   }
}
