package org.simpleframework.demo.rest;

public class MatchRequest {

   private final Class type;
   
   public MatchRequest(Class type) {
      this.type = type;
   }
   
   public Class getType() {
      return type;
   }
}
