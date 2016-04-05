package org.simpleframework.demo.rest;

public class StatusResponse {

   private final String address;
   private final boolean success;
   
   public StatusResponse(String address, boolean success) {
      this.address = address;
      this.success = success;
   }

   public String getAddress() {
      return address;
   }

   public boolean isSuccess() {
      return success;
   }
}
