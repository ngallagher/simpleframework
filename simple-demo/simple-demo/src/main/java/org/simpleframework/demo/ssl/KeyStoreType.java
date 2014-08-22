package org.simpleframework.demo.ssl;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManagerFactory;

public enum KeyStoreType {
   JKS("JKS", "SunX509"), 
   PKCS12("PKCS12", "SunX509");

   private final String algorithm;
   private final String type;

   private KeyStoreType(String type, String algorithm) {
      this.algorithm = algorithm;
      this.type = type;
   }

   public String getType() {
      return type;
   }

   public KeyStore getKeyStore() throws KeyStoreException {
      return KeyStore.getInstance(type);
   }

   public KeyManagerFactory getKeyManagerFactory() throws NoSuchAlgorithmException {
      return KeyManagerFactory.getInstance(algorithm);
   }
}
