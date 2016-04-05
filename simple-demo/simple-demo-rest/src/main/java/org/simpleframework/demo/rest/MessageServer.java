package org.simpleframework.demo.rest;

import java.util.Collections;
import java.util.List;

public class MessageServer {
   
   public static final String SUBSCRIBE_PREFIX = "subscribe";
   public static final String RENEW_PREFIX = "renew";

   private final List<RequestRegistration> registrations;
   
   public MessageServer() {
      this(Collections.EMPTY_LIST);
   }
   
   public MessageServer(List<RequestRegistration> registrations) {
      this.registrations = registrations;
   }
   
   public MessagePublisher create(int port) throws Exception {
      SubscriptionManager manager = new LeaseSubscriptionManager();
      SubscribeRequestHandler subscriber = new SubscribeRequestHandler(manager);
      RenewRequestHandler renewer = new RenewRequestHandler(manager);
      RequestProcessor processor = new RequestProcessor(port);
      
      processor.register(new RequestRegistration<SubscribeRequest>(SUBSCRIBE_PREFIX, subscriber, SubscribeRequest.class));
      processor.register(new RequestRegistration<RenewRequest>(RENEW_PREFIX, renewer, RenewRequest.class));
      
      for(RequestRegistration registration : registrations) {
         processor.register(registration);
      }
      processor.start();
      return new SubscriptionMessagePublisher(manager);
   }
}
