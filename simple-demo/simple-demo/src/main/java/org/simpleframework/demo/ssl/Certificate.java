package org.simpleframework.demo.ssl;

import javax.net.ssl.SSLContext;

public class Certificate {
   
   private final SecureSocketContext context;
   private final String[] suites;

   public Certificate(SecureSocketContext context) throws Exception {
      this(context, null);
   }
   
   public Certificate(SecureSocketContext context, String[] suites) throws Exception {
      this.context = context;
      this.suites = suites;
   }
   
   public SSLContext getContext() {
      try {
         return context.getContext();
      } catch(Exception e) {
         throw new IllegalStateException("Could not create context", e);         
      }
   }
   
   public String[] getCipherSuites() {
      return suites;
   }
}
