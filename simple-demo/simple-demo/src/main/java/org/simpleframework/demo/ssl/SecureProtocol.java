package org.simpleframework.demo.ssl;

import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

public enum SecureProtocol {
   DEFAULT("Default"), 
   SSL("SSL"), 
   TLS("TLS");

   private final String protocol;

   private SecureProtocol(String protocol) {
      this.protocol = protocol;
   }

   public SSLContext getContext() throws NoSuchAlgorithmException {
      return SSLContext.getInstance(protocol);
   }
}
