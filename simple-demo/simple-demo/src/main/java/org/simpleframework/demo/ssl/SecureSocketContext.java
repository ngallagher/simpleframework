package org.simpleframework.demo.ssl;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public class SecureSocketContext {

   private final X509TrustManager trustManager;
   private final X509TrustManager[] trustManagers;
   private final KeyStoreReader keyStoreReader;
   private final SecureProtocol secureProtocol;

   public SecureSocketContext(KeyStoreReader keyStoreReader, SecureProtocol secureProtocol) {
      this.trustManager = new AnonymousTrustManager();
      this.trustManagers = new X509TrustManager[] { trustManager };
      this.keyStoreReader = keyStoreReader;
      this.secureProtocol = secureProtocol;
   }

   public SecureSocketContext(KeyStoreReader keyStoreReader, SecureProtocol secureProtocol, X509TrustManager trustManager) {
      this.trustManagers = new X509TrustManager[] { trustManager };
      this.keyStoreReader = keyStoreReader;
      this.secureProtocol = secureProtocol;
      this.trustManager = trustManager;
   }

   public SSLContext getContext() throws Exception {
      KeyManager[] keyManagers = keyStoreReader.getKeyManagers();
      SSLContext secureContext = secureProtocol.getContext();

      secureContext.init(keyManagers, trustManagers, null);

      return secureContext;
   }

   public SSLSocketFactory getSocketFactory() throws Exception {
      KeyManager[] keyManagers = keyStoreReader.getKeyManagers();
      SSLContext secureContext = secureProtocol.getContext();

      secureContext.init(keyManagers, trustManagers, null);

      return secureContext.getSocketFactory();
   }

   public SSLServerSocketFactory getServerSocketFactory() throws Exception {
      KeyManager[] keyManagers = keyStoreReader.getKeyManagers();
      SSLContext secureContext = secureProtocol.getContext();

      secureContext.init(keyManagers, trustManagers, null);

      return secureContext.getServerSocketFactory();
   }
}
