package org.simpleframework.http;

import java.net.URI;

public enum Scheme {
   HTTP("http", false),
   HTTPS("https", true),
   WS("ws", false),
   WSS("wss", true);
   
   public final String scheme;
   public final boolean secure;
   
   private Scheme(String scheme, boolean secure) {
      this.scheme = scheme;
      this.secure = secure;
   }   
   
   public boolean isSecure() {
      return secure;
   }
   
   public String getScheme() {
      return scheme;
   }
   
   public static Scheme resolveScheme(String token) {
      if(token != null) {
         for(Scheme scheme : values()) {
            if(token.equalsIgnoreCase(scheme.scheme)) {
               return scheme;
            }
         }
      }
      return HTTP;
   }
   
   public static Scheme resolveScheme(URI target) {
      if(target != null) {
         String scheme = target.getScheme();
         
         for(Scheme option : values()) {
            if(option.scheme.equalsIgnoreCase(scheme)) {
               return option;
            }
         }
      }
      return null;
   }
}
