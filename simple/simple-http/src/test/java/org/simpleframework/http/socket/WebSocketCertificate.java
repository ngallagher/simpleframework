package org.simpleframework.http.socket;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public class WebSocketCertificate {

   private final X509TrustManager trustManager;
   private final X509TrustManager[] trustManagers;
   private final KeyStoreReader keyStoreReader;
   private final SecureProtocol secureProtocol;

   public WebSocketCertificate(KeyStoreReader keyStoreReader, SecureProtocol secureProtocol) {
      this.trustManager = new AnonymousTrustManager();
      this.trustManagers = new X509TrustManager[] { trustManager };
      this.keyStoreReader = keyStoreReader;
      this.secureProtocol = secureProtocol;
   }

   public WebSocketCertificate(KeyStoreReader keyStoreReader, SecureProtocol secureProtocol, X509TrustManager trustManager) {
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
   
   public static enum SecureProtocol {
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
   
   public static enum KeyStoreType {
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
   
   public static class KeyStoreReader {

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

   public static class KeyStoreManager {

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
   
   public static class AnonymousTrustManager implements X509TrustManager {

      public boolean isClientTrusted(X509Certificate[] cert) {
         return true;
      }

      public boolean isServerTrusted(X509Certificate[] cert) {
         return true;
      }

      public X509Certificate[] getAcceptedIssuers() {
         return new X509Certificate[0];
      }

      public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}   

      public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
   }
}
