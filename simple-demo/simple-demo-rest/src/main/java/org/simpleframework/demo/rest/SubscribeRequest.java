package org.simpleframework.demo.rest;

public class SubscribeRequest {

   private final String key;
   private final String filter;
   private final String address;
   private final long duration;
   
   public SubscribeRequest(String key, String filter, String address, long duration){
      this.filter = filter;
      this.address = address;
      this.duration = duration;
      this.key = key;
   }

   public String getKey() {
      return key;
   }

   public String getFilter() {
      return filter;
   }

   public String getAddress() {
      return address;
   }

   public long getDuration() {
      return duration;
   }
}
