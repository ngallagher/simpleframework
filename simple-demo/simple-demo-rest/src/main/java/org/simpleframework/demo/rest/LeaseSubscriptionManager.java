package org.simpleframework.demo.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.simpleframework.common.lease.Cleaner;
import org.simpleframework.common.lease.Lease;
import org.simpleframework.common.lease.LeaseManager;

public class LeaseSubscriptionManager implements SubscriptionManager {

   private final Map<String, Subscription> subscriptions;
   private final LeaseManager<String> manager;
   private final SubscriptionCleaner cleaner;
   
   public LeaseSubscriptionManager() {
      this.subscriptions = new ConcurrentHashMap<String, Subscription>();
      this.cleaner = new SubscriptionCleaner();
      this.manager = new LeaseManager<String>(cleaner);
   }
   
   public List<String> match(MatchRequest request) {
      List<String> addresses = new ArrayList<String>();
      
      if(!subscriptions.isEmpty()) {
         Set<Entry<String, Subscription>> entries = subscriptions.entrySet();
         Class type = request.getType();
         String name = type.getName();
         
         for(Entry<String, Subscription> entry : entries) {
            Subscription subscription = entry.getValue();
            String filter = subscription.getFilter();
            String address = subscription.getAddress();
            
            if(filter.matches(name)) {
               addresses.add(address);
            }
         }
      }
      return addresses;   
   }
   
   public void subscribe(SubscribeRequest request) {
      String key = request.getKey();
      String address = request.getAddress();
      String filter = request.getFilter();
      long duration = request.getDuration();
      Lease<String> lease = manager.lease(key, duration, TimeUnit.MILLISECONDS);
      Subscription subscription = new Subscription(lease, address, filter);
      subscriptions.put(key, subscription);
   }
   
   public void renew(RenewRequest request) {
      String key = request.getKey();
      long duration = request.getDuration();
      Subscription subscription = subscriptions.get(key);
      
      if(subscription != null) {
         Lease<String> lease = subscription.getLease();
         lease.renew(duration, TimeUnit.MILLISECONDS);
      }
   }
   
   private class SubscriptionCleaner implements Cleaner<String> {
      
      public void clean(String key) {
         subscriptions.remove(key);
      }
   }
   
   private class Subscription {
      
      private final Lease<String> lease;
      private final String address;
      private final String filter;
      
      public Subscription(Lease<String> lease, String address, String filter) {
         this.lease = lease;
         this.address = address;
         this.filter = filter;
      }

      public Lease<String> getLease() {
         return lease;
      }

      public String getAddress() {
         return address;
      }

      public String getFilter() {
         return filter;
      }

   }
}
