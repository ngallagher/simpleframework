package org.simpleframework.http.core;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;


public class Client {
   

   private static final byte[] CERTIFICATE = {
      (byte)254,(byte)237,(byte)254,(byte)237,(byte)0,  (byte)0,  (byte)0,  (byte)2,  (byte)0,  (byte)0,  
      (byte)0,  (byte)1,  (byte)0,  (byte)0,  (byte)0,  (byte)1,  (byte)0,  (byte)3,  (byte)107,(byte)101,
      (byte)121,(byte)0,  (byte)0,  (byte)1,  (byte)26, (byte)105,(byte)38, (byte)187,(byte)170,(byte)0,  
      (byte)0,  (byte)2,  (byte)187,(byte)48, (byte)130,(byte)2,  (byte)183,(byte)48, (byte)14, (byte)6,  
      (byte)10, (byte)43, (byte)6,  (byte)1,  (byte)4,  (byte)1,  (byte)42, (byte)2,  (byte)17, (byte)1,  
      (byte)1,  (byte)5,  (byte)0,  (byte)4,  (byte)130,(byte)2,  (byte)163,(byte)138,(byte)122,(byte)194,
      (byte)218,(byte)31, (byte)101,(byte)210,(byte)131,(byte)160,(byte)37, (byte)111,(byte)187,(byte)43, 
      (byte)192,(byte)67, (byte)244,(byte)136,(byte)120,(byte)166,(byte)171,(byte)204,(byte)87, (byte)156,
      (byte)50, (byte)58, (byte)153,(byte)37, (byte)180,(byte)248,(byte)60, (byte)73, (byte)16, (byte)110,
      (byte)176,(byte)84, (byte)239,(byte)247,(byte)113,(byte)133,(byte)193,(byte)239,(byte)94, (byte)107,
      (byte)126,(byte)141,(byte)199,(byte)243,(byte)243,(byte)25, (byte)179,(byte)181,(byte)201,(byte)100,
      (byte)194,(byte)146,(byte)114,(byte)162,(byte)124,(byte)96, (byte)198,(byte)248,(byte)232,(byte)162,
      (byte)143,(byte)170,(byte)120,(byte)106,(byte)171,(byte)128,(byte)32, (byte)18, (byte)134,(byte)69, 
      (byte)2,  (byte)230,(byte)204,(byte)18, (byte)191,(byte)212,(byte)236,(byte)130,(byte)76, (byte)24, 
      (byte)24, (byte)131,(byte)210,(byte)150,(byte)209,(byte)205,(byte)174,(byte)25, (byte)175,(byte)45, 
      (byte)39, (byte)223,(byte)17, (byte)57, (byte)129,(byte)6,  (byte)195,(byte)116,(byte)197,(byte)143,
      (byte)14, (byte)160,(byte)120,(byte)249,(byte)220,(byte)48, (byte)71, (byte)109,(byte)122,(byte)64, 
      (byte)195,(byte)139,(byte)61, (byte)206,(byte)83, (byte)159,(byte)78, (byte)137,(byte)160,(byte)88, 
      (byte)252,(byte)120,(byte)217,(byte)251,(byte)254,(byte)151,(byte)94, (byte)242,(byte)170,(byte)0,  
      (byte)247,(byte)170,(byte)53, (byte)197,(byte)34, (byte)253,(byte)96, (byte)47, (byte)248,(byte)194,
      (byte)230,(byte)62, (byte)121,(byte)117,(byte)163,(byte)35, (byte)80, (byte)15, (byte)113,(byte)203,
      (byte)71, (byte)202,(byte)36, (byte)187,(byte)163,(byte)78, (byte)228,(byte)31, (byte)3,  (byte)53, 
      (byte)214,(byte)149,(byte)170,(byte)214,(byte)161,(byte)180,(byte)53, (byte)207,(byte)158,(byte)150,
      (byte)161,(byte)37, (byte)59, (byte)150,(byte)107,(byte)161,(byte)9,  (byte)195,(byte)79, (byte)254,
      (byte)62, (byte)231,(byte)13, (byte)195,(byte)173,(byte)139,(byte)15, (byte)153,(byte)62, (byte)20, 
      (byte)204,(byte)111,(byte)64, (byte)89, (byte)180,(byte)201,(byte)58, (byte)64, (byte)15, (byte)195,
      (byte)18, (byte)29, (byte)29, (byte)44, (byte)5,  (byte)101,(byte)132,(byte)113,(byte)204,(byte)251,
      (byte)225,(byte)3,  (byte)82, (byte)52, (byte)62, (byte)86, (byte)142,(byte)43, (byte)240,(byte)201,
      (byte)26, (byte)226,(byte)143,(byte)162,(byte)9,  (byte)97, (byte)96, (byte)185,(byte)59, (byte)85, 
      (byte)54, (byte)115,(byte)135,(byte)199,(byte)26, (byte)58, (byte)185,(byte)100,(byte)118,(byte)48, 
      (byte)119,(byte)110,(byte)203,(byte)115,(byte)74, (byte)152,(byte)144,(byte)137,(byte)13, (byte)18, 
      (byte)192,(byte)82, (byte)101,(byte)163,(byte)8,  (byte)128,(byte)57, (byte)68, (byte)183,(byte)225,
      (byte)79, (byte)6,  (byte)143,(byte)94, (byte)203,(byte)203,(byte)121,(byte)52, (byte)128,(byte)94, 
      (byte)184,(byte)223,(byte)107,(byte)217,(byte)68, (byte)118,(byte)145,(byte)164,(byte)13, (byte)220,
      (byte)135,(byte)11, (byte)74, (byte)193,(byte)48, (byte)7,  (byte)95, (byte)190,(byte)17, (byte)0,  
      (byte)69, (byte)109,(byte)6,  (byte)64, (byte)86, (byte)80, (byte)93, (byte)82, (byte)20, (byte)106,
      (byte)191,(byte)201,(byte)13, (byte)91, (byte)132,(byte)102,(byte)47, (byte)188,(byte)123,(byte)79, 
      (byte)209,(byte)43, (byte)180,(byte)152,(byte)128,(byte)20, (byte)182,(byte)148,(byte)19, (byte)24, 
      (byte)230,(byte)249,(byte)42, (byte)51, (byte)197,(byte)176,(byte)113,(byte)44, (byte)100,(byte)95, 
      (byte)59, (byte)91, (byte)78, (byte)226,(byte)184,(byte)224,(byte)72, (byte)233,(byte)133,(byte)154,
      (byte)42, (byte)221,(byte)32, (byte)165,(byte)41, (byte)156,(byte)165,(byte)247,(byte)86, (byte)115,
      (byte)183,(byte)22, (byte)89, (byte)17, (byte)165,(byte)215,(byte)148,(byte)32, (byte)199,(byte)64, 
      (byte)139,(byte)171,(byte)236,(byte)43, (byte)5,  (byte)36, (byte)35, (byte)223,(byte)35, (byte)247,
      (byte)255,(byte)112,(byte)27, (byte)215,(byte)57, (byte)251,(byte)236,(byte)128,(byte)168,(byte)219,
      (byte)146,(byte)235,(byte)241,(byte)68, (byte)213,(byte)127,(byte)63, (byte)231,(byte)236,(byte)176,
      (byte)166,(byte)121,(byte)203,(byte)114,(byte)33, (byte)19, (byte)200,(byte)167,(byte)155,(byte)27, 
      (byte)38, (byte)109,(byte)133,(byte)1,  (byte)184,(byte)173,(byte)253,(byte)198,(byte)122,(byte)98, 
      (byte)196,(byte)43, (byte)145,(byte)86, (byte)182,(byte)208,(byte)78, (byte)246,(byte)234,(byte)249,
      (byte)229,(byte)202,(byte)75, (byte)66, (byte)108,(byte)134,(byte)81, (byte)134,(byte)90, (byte)251,
      (byte)137,(byte)155,(byte)209,(byte)11, (byte)249,(byte)87, (byte)164,(byte)98, (byte)242,(byte)51, 
      (byte)184,(byte)162,(byte)35, (byte)20, (byte)248,(byte)14, (byte)224,(byte)76, (byte)31, (byte)132,
      (byte)125,(byte)44, (byte)83, (byte)15, (byte)221,(byte)43, (byte)62, (byte)187,(byte)211,(byte)176,
      (byte)41, (byte)70, (byte)187,(byte)3,  (byte)48, (byte)150,(byte)206,(byte)54, (byte)38, (byte)33, 
      (byte)94, (byte)133,(byte)145,(byte)148,(byte)58, (byte)219,(byte)252,(byte)124,(byte)251,(byte)46, 
      (byte)72, (byte)35, (byte)244,(byte)33, (byte)97, (byte)50, (byte)21, (byte)207,(byte)163,(byte)3,  
      (byte)226,(byte)225,(byte)252,(byte)149,(byte)214,(byte)200,(byte)132,(byte)65, (byte)224,(byte)121,
      (byte)205,(byte)241,(byte)107,(byte)155,(byte)252,(byte)158,(byte)64, (byte)40, (byte)252,(byte)143,
      (byte)76, (byte)71, (byte)227,(byte)13, (byte)176,(byte)50, (byte)250,(byte)115,(byte)198,(byte)64, 
      (byte)174,(byte)146,(byte)108,(byte)106,(byte)66, (byte)98, (byte)78, (byte)196,(byte)126,(byte)118,
      (byte)51, (byte)65, (byte)251,(byte)8,  (byte)28, (byte)75, (byte)123,(byte)92, (byte)5,  (byte)125,
      (byte)16, (byte)127,(byte)250,(byte)65, (byte)178,(byte)54, (byte)169,(byte)109,(byte)94, (byte)171,
      (byte)97, (byte)154,(byte)232,(byte)24, (byte)196,(byte)91, (byte)103,(byte)90, (byte)217,(byte)75, 
      (byte)126,(byte)76, (byte)129,(byte)240,(byte)67, (byte)131,(byte)147,(byte)178,(byte)29, (byte)234,
      (byte)150,(byte)91, (byte)78, (byte)165,(byte)76, (byte)200,(byte)99, (byte)175,(byte)240,(byte)3,  
      (byte)76, (byte)151,(byte)111,(byte)167,(byte)220,(byte)162,(byte)7,  (byte)249,(byte)12, (byte)201,
      (byte)171,(byte)58, (byte)170,(byte)26, (byte)149,(byte)224,(byte)135,(byte)201,(byte)186,(byte)201,
      (byte)253,(byte)153,(byte)248,(byte)148,(byte)171,(byte)197,(byte)70, (byte)179,(byte)127,(byte)210,
      (byte)30, (byte)172,(byte)207,(byte)179,(byte)140,(byte)240,(byte)244,(byte)2,  (byte)24, (byte)156,
      (byte)116,(byte)6,  (byte)237,(byte)42, (byte)221,(byte)201,(byte)244,(byte)207,(byte)123,(byte)19, 
      (byte)189,(byte)58, (byte)189,(byte)107,(byte)223,(byte)44, (byte)230,(byte)114,(byte)115,(byte)194,
      (byte)189,(byte)163,(byte)189,(byte)224,(byte)161,(byte)221,(byte)40, (byte)29, (byte)73, (byte)244,
      (byte)231,(byte)213,(byte)139,(byte)178,(byte)248,(byte)84, (byte)137,(byte)65, (byte)124,(byte)98, 
      (byte)248,(byte)62, (byte)229,(byte)86, (byte)128,(byte)57, (byte)106,(byte)38, (byte)193,(byte)185,
      (byte)10, (byte)162,(byte)0,  (byte)0,  (byte)0,  (byte)1,  (byte)0,  (byte)5,  (byte)88, (byte)46, 
      (byte)53, (byte)48, (byte)57, (byte)0,  (byte)0,  (byte)2,  (byte)72, (byte)48, (byte)130,(byte)2,  
      (byte)68, (byte)48, (byte)130,(byte)1,  (byte)173,(byte)2,  (byte)4,  (byte)72, (byte)76, (byte)18, 
      (byte)25, (byte)48, (byte)13, (byte)6,  (byte)9,  (byte)42, (byte)134,(byte)72, (byte)134,(byte)247,
      (byte)13, (byte)1,  (byte)1,  (byte)4,  (byte)5,  (byte)0,  (byte)48, (byte)105,(byte)49, (byte)16, 
      (byte)48, (byte)14, (byte)6,  (byte)3,  (byte)85, (byte)4,  (byte)6,  (byte)19, (byte)7,  (byte)67, 
      (byte)111,(byte)117,(byte)110,(byte)116,(byte)114,(byte)121,(byte)49, (byte)17, (byte)48, (byte)15, 
      (byte)6,  (byte)3,  (byte)85, (byte)4,  (byte)7,  (byte)19, (byte)8,  (byte)76, (byte)111,(byte)99, 
      (byte)97, (byte)116,(byte)105,(byte)111,(byte)110,(byte)49, (byte)28, (byte)48, (byte)26, (byte)6,  
      (byte)3,  (byte)85, (byte)4,  (byte)11, (byte)19, (byte)19, (byte)79, (byte)114,(byte)103,(byte)97, 
      (byte)110,(byte)105,(byte)122,(byte)97, (byte)116,(byte)105,(byte)111,(byte)110,(byte)97, (byte)108,
      (byte)32, (byte)85, (byte)110,(byte)105,(byte)116,(byte)49, (byte)21, (byte)48, (byte)19, (byte)6,  
      (byte)3,  (byte)85, (byte)4,  (byte)10, (byte)19, (byte)12, (byte)79, (byte)114,(byte)103,(byte)97, 
      (byte)110,(byte)105,(byte)122,(byte)97, (byte)116,(byte)105,(byte)111,(byte)110,(byte)49, (byte)13, 
      (byte)48, (byte)11, (byte)6,  (byte)3,  (byte)85, (byte)4,  (byte)3,  (byte)19, (byte)4,  (byte)78, 
      (byte)97, (byte)109,(byte)101,(byte)48, (byte)30, (byte)23, (byte)13, (byte)48, (byte)56, (byte)48, 
      (byte)54, (byte)48, (byte)56, (byte)49, (byte)55, (byte)48, (byte)56, (byte)52, (byte)49, (byte)90, 
      (byte)23, (byte)13, (byte)48, (byte)57, (byte)48, (byte)54, (byte)48, (byte)56, (byte)49, (byte)55, 
      (byte)48, (byte)56, (byte)52, (byte)49, (byte)90, (byte)48, (byte)105,(byte)49, (byte)16, (byte)48, 
      (byte)14, (byte)6,  (byte)3,  (byte)85, (byte)4,  (byte)6,  (byte)19, (byte)7,  (byte)67, (byte)111,
      (byte)117,(byte)110,(byte)116,(byte)114,(byte)121,(byte)49, (byte)17, (byte)48, (byte)15, (byte)6,  
      (byte)3,  (byte)85, (byte)4,  (byte)7,  (byte)19, (byte)8,  (byte)76, (byte)111,(byte)99, (byte)97, 
      (byte)116,(byte)105,(byte)111,(byte)110,(byte)49, (byte)28, (byte)48, (byte)26, (byte)6,  (byte)3,  
      (byte)85, (byte)4,  (byte)11, (byte)19, (byte)19, (byte)79, (byte)114,(byte)103,(byte)97, (byte)110,
      (byte)105,(byte)122,(byte)97, (byte)116,(byte)105,(byte)111,(byte)110,(byte)97, (byte)108,(byte)32, 
      (byte)85, (byte)110,(byte)105,(byte)116,(byte)49, (byte)21, (byte)48, (byte)19, (byte)6,  (byte)3,  
      (byte)85, (byte)4,  (byte)10, (byte)19, (byte)12, (byte)79, (byte)114,(byte)103,(byte)97, (byte)110,
      (byte)105,(byte)122,(byte)97, (byte)116,(byte)105,(byte)111,(byte)110,(byte)49, (byte)13, (byte)48, 
      (byte)11, (byte)6,  (byte)3,  (byte)85, (byte)4,  (byte)3,  (byte)19, (byte)4,  (byte)78, (byte)97, 
      (byte)109,(byte)101,(byte)48, (byte)129,(byte)159,(byte)48, (byte)13, (byte)6,  (byte)9,  (byte)42, 
      (byte)134,(byte)72, (byte)134,(byte)247,(byte)13, (byte)1,  (byte)1,  (byte)1,  (byte)5,  (byte)0,  
      (byte)3,  (byte)129,(byte)141,(byte)0,  (byte)48, (byte)129,(byte)137,(byte)2,  (byte)129,(byte)129,
      (byte)0,  (byte)137,(byte)239,(byte)22, (byte)193,(byte)171,(byte)79, (byte)177,(byte)85, (byte)159,
      (byte)210,(byte)81, (byte)174,(byte)63, (byte)210,(byte)57, (byte)43, (byte)172,(byte)130,(byte)205,
      (byte)144,(byte)207,(byte)100,(byte)16, (byte)69, (byte)78, (byte)72, (byte)22, (byte)155,(byte)44, 
      (byte)146,(byte)252,(byte)202,(byte)119,(byte)199,(byte)69, (byte)38, (byte)48, (byte)38, (byte)39, 
      (byte)46, (byte)119,(byte)219,(byte)200,(byte)105,(byte)216,(byte)188,(byte)162,(byte)175,(byte)74, 
      (byte)43, (byte)175,(byte)6,  (byte)148,(byte)131,(byte)125,(byte)226,(byte)198,(byte)239,(byte)115,
      (byte)204,(byte)196,(byte)28, (byte)189,(byte)108,(byte)236,(byte)29, (byte)132,(byte)72, (byte)207,
      (byte)238,(byte)3,  (byte)97, (byte)223,(byte)227,(byte)82, (byte)115,(byte)202,(byte)134,(byte)43, 
      (byte)242,(byte)83, (byte)70, (byte)226,(byte)172,(byte)162,(byte)177,(byte)183,(byte)128,(byte)126,
      (byte)164,(byte)233,(byte)250,(byte)230,(byte)18, (byte)177,(byte)126,(byte)40, (byte)36, (byte)30, 
      (byte)169,(byte)124,(byte)126,(byte)203,(byte)23, (byte)252,(byte)38, (byte)55, (byte)250,(byte)181,
      (byte)232,(byte)168,(byte)84, (byte)232,(byte)140,(byte)85, (byte)119,(byte)163,(byte)255,(byte)117,
      (byte)133,(byte)174,(byte)51, (byte)195,(byte)8,  (byte)174,(byte)200,(byte)142,(byte)43, (byte)2,  
      (byte)3,  (byte)1,  (byte)0,  (byte)1,  (byte)48, (byte)13, (byte)6,  (byte)9,  (byte)42, (byte)134,
      (byte)72, (byte)134,(byte)247,(byte)13, (byte)1,  (byte)1,  (byte)4,  (byte)5,  (byte)0,  (byte)3,  
      (byte)129,(byte)129,(byte)0,  (byte)9,  (byte)240,(byte)8,  (byte)65, (byte)178,(byte)238,(byte)119,
      (byte)127,(byte)249,(byte)164,(byte)9,  (byte)159,(byte)110,(byte)132,(byte)177,(byte)76, (byte)239,
      (byte)164,(byte)27, (byte)130,(byte)174,(byte)97, (byte)100,(byte)2,  (byte)154,(byte)231,(byte)44, 
      (byte)217,(byte)30, (byte)210,(byte)42, (byte)221,(byte)225,(byte)114,(byte)205,(byte)165,(byte)152,
      (byte)188,(byte)232,(byte)1,  (byte)128,(byte)143,(byte)116,(byte)113,(byte)128,(byte)50, (byte)199,
      (byte)80, (byte)16, (byte)172,(byte)112,(byte)129,(byte)236,(byte)34, (byte)189,(byte)106,(byte)79, 
      (byte)152,(byte)67, (byte)233,(byte)61, (byte)114,(byte)137,(byte)40, (byte)157,(byte)233,(byte)83, 
      (byte)123,(byte)28, (byte)138,(byte)168,(byte)46, (byte)151,(byte)36, (byte)177,(byte)7,  (byte)22, 
      (byte)148,(byte)253,(byte)80, (byte)144,(byte)122,(byte)52, (byte)104,(byte)196,(byte)15, (byte)225,
      (byte)148,(byte)136,(byte)193,(byte)68, (byte)133,(byte)113,(byte)48, (byte)244,(byte)8,  (byte)64, 
      (byte)117,(byte)110,(byte)115,(byte)80, (byte)110,(byte)105,(byte)56, (byte)20, (byte)170,(byte)125,
      (byte)182,(byte)159,(byte)190,(byte)4,  (byte)173,(byte)193,(byte)200,(byte)153,(byte)246,(byte)155,
      (byte)249,(byte)33, (byte)180,(byte)233,(byte)48, (byte)109,(byte)55, (byte)208,(byte)209,(byte)196,
      (byte)16, (byte)23, (byte)172,(byte)125,(byte)207,(byte)94, (byte)238,(byte)23, (byte)38, (byte)60, 
      (byte)58, (byte)92, (byte)244,(byte)100,(byte)145,(byte)44, (byte)204,(byte)92, (byte)21, (byte)136,
      (byte)39, };

   
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

      public void checkClientTrusted(X509Certificate[] arg0, String arg1)
            throws CertificateException {
      }

      public void checkServerTrusted(X509Certificate[] arg0, String arg1)
            throws CertificateException {
      }
   }
   
   private static SSLContext sslContext;
   private static SocketFactory factory;
   
   static {
      try {
         KeyStore store = KeyStore.getInstance("JKS");      
         KeyManagerFactory keyFactory = KeyManagerFactory.getInstance("SunX509");
         sslContext = SSLContext.getInstance("TLS");//SSLv3");
         InputStream stream = new ByteArrayInputStream(CERTIFICATE);
         X509TrustManager trustManager = new AnonymousTrustManager();
         X509TrustManager[] trustManagers = new X509TrustManager[]{trustManager};
         
         store.load(stream, "password".toCharArray());
         keyFactory.init(store, "password".toCharArray());
         sslContext.init(keyFactory.getKeyManagers(), trustManagers, null);
         
         factory = sslContext.getSocketFactory();
      }catch(Exception e) {
         e.printStackTrace();
      }

   }

   
   public SSLContext getServerSSLContext() {
      return sslContext;
   }
   
   public SocketFactory getClientSocketFactory() {
      return factory;
   }
   
   public static void main(String[] list) throws Exception {
      FileOutputStream out = new FileOutputStream("c:\\client");
      final PrintStream console = System.out;
      OutputStream dup = new FilterOutputStream(out) {
         public void write(int off) throws IOException {
            console.write(off);
            out.write(off);
         }
        public void write(byte[] b, int off, int len) throws IOException {
           out.write(b, off, len);
           console.write(b, off, len);
        }
        public void flush() throws IOException {
           out.flush();
           console.flush();
        }
        public void close() throws IOException {
           out.close();
        }
      };
      PrintStream p = new PrintStream(dup, true);
      
      System.setOut(p);
      System.setErr(p);      
      Socket socket = factory.createSocket("localhost", 9091);
      OutputStream sockOut = socket.getOutputStream();
      sockOut.write("GET /tmp/amazon.htm HTTP/1.1\r\nConnection: keep-alive\r\n\r\n".getBytes("ISO-8859-1"));
      sockOut.flush();
      InputStream in = socket.getInputStream();
      byte[] buf = new byte[1024];
      int all = 0;
      int count = 0;
      while((count = in.read(buf)) != -1) {
         all += count;
         if(all >= 564325) {
            break;
         }
         System.out.write(buf, 0, count);
         System.out.flush();
      }
      console.println(">>>>>>>>>>>>>> ALL=["+all+"]");
      System.err.println("FINISHED READING");
      Thread.sleep(10000);
      
   }

}
