package org.simpleframework.demo.ssl;

import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

public class KeyStoreManager {

   private final KeyStoreType keyStoreType;

   public KeyStoreManager(KeyStoreType keyStoreType) {
      this.keyStoreType = keyStoreType;
   }

   public KeyManager[] getKeyManagers(InputStream keyStoreSource, String keyStorePassword, String keyManagerPassword) throws Exception {
      KeyStore keyStore = keyStoreType.getKeyStore();
      KeyManagerFactory keyManagerFactory = keyStoreType.getKeyManagerFactory();

      keyStore.load(keyStoreSource, keyManagerPassword.toCharArray());
      keyManagerFactory.init(keyStore, keyManagerPassword.toCharArray());

      return keyManagerFactory.getKeyManagers();
   }

}
