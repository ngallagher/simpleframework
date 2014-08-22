package org.simpleframework.demo.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CertificateReader {

   private List<File> certificates;

   public CertificateReader() {
      this(Collections.EMPTY_LIST);
   }

   public CertificateReader(List<File> certificates) {
      this.certificates = certificates;
   }

   public X509Certificate[] getCertificates() throws Exception {
      List<X509Certificate> result = new LinkedList<X509Certificate>();
      X509Certificate[] array = new X509Certificate[] {};

      for (File certificateFile : certificates) {
         if (certificateFile.exists()) {
            Collection<? extends Certificate> list = getCertificates(certificateFile);

            for (Certificate entry : list) {
               if (entry instanceof X509Certificate) {
                  result.add((X509Certificate) entry);
               }
            }
         }
      }
      return result.toArray(array);
   }

   private Collection<? extends Certificate> getCertificates(File certificateFile) throws Exception {
      InputStream certificateStream = new FileInputStream(certificateFile);
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

      try {
         return certificateFactory.generateCertificates(certificateStream);
      } finally {
         certificateStream.close();
      }
   }
}
