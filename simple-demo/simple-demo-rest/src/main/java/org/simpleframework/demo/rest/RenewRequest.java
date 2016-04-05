package org.simpleframework.demo.rest;

public class RenewRequest {

   private final String key;
   private final long duration;
   
   public RenewRequest(String key, long duration){
      this.duration = duration;
      this.key = key;
   }

   public String getKey() {
      return key;
   }

   public long getDuration() {
      return duration;
   }
}