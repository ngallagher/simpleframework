package org.simpleframework.demo.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.net.ssl.KeyManager;

/**
 * A key store reader is used to read a key store in order to create a list of
 * {@link javax.net.ssl.KeyManager} objects. These managers are then used to
 * create SSL sockets with the key store keys.
 * 
 * @author Niall Gallagher
 */
public class KeyStoreReader {

   private final KeyStoreManager keyStoreManager;
   private final String keyManagerPassword;
   private final String keyStorePassword;
   private final File keyStore;

   public KeyStoreReader(KeyStoreType keyStoreType, File keyStore, String keyStorePassword, String keyManagerPassword) {
      this.keyStoreManager = new KeyStoreManager(keyStoreType);
      this.keyManagerPassword = keyManagerPassword;
      this.keyStorePassword = keyStorePassword;
      this.keyStore = keyStore;
   }

   public KeyManager[] getKeyManagers() throws Exception {
      InputStream storeSource = new FileInputStream(keyStore);

      try {
         return keyStoreManager.getKeyManagers(storeSource, keyStorePassword, keyManagerPassword);
      } finally {
         storeSource.close();
      }
   }
}
