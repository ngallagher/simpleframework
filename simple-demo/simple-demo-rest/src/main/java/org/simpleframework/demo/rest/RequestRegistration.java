package org.simpleframework.demo.rest;

public class RequestRegistration<T> {

   private final RequestHandler<T> handler;
   private final Class<T> type;
   private final String prefix;
   
   public RequestRegistration(String prefix, RequestHandler<T> handler) {
      this(prefix, handler, null);
   }
   
   public RequestRegistration(String prefix, RequestHandler<T> handler, Class<T> type) {
      this.handler = handler;
      this.type = type;
      this.prefix = prefix;
   }

   public RequestHandler<T> getHandler() {
      return handler;
   }

   public Class<T> getType() {
      return type;
   }

   public String getPrefix() {
      return prefix;
   }
}
