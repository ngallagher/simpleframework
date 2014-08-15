package org.simpleframework.http.socket;

import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.cert.X509Certificate;

import org.simpleframework.http.Request;
import org.simpleframework.transport.Certificate;


public class WebSocketChatRoomListener implements FrameListener {
   
   private final CertificateUserExtractor extractor;
   private final WebSocketChatRoom room;
   
   public WebSocketChatRoomListener(WebSocketChatRoom room) {
      this.extractor = new CertificateUserExtractor(".*EMAILADDRESS=(.*?),.*");
      this.room = room;
   }

   public void onFrame(Session socket, Frame frame) {
      FrameType type = frame.getType();
      String text = frame.getText();
      
      if(type == FrameType.TEXT){
         try {
            Request request = socket.getRequest();
            String user = extractor.extractUser(request);
            
            text = text + "  (SSL=" + request.isSecure() + ", EMAILADDRESS=" + user + ")";
         } catch(Exception e) {
            e.printStackTrace();
         }
         Frame replay = new DataFrame(type, text);
         room.distribute(replay);
      }
   }

   public void onError(Session socket, Exception cause) {
      System.err.println("onError(");
      cause.printStackTrace();
      System.err.println(")");
   }

   public void onOpen(Session socket) {
      System.err.println("onOpen(" + socket +")");
   }

   public void onClose(Session session, Reason reason) {
      System.err.println("onClose(" + reason +")");
   }
   
   public static class CertificateUserExtractor {
      
      private final Map<String, String> cache;
      private final Pattern pattern;
      
      public CertificateUserExtractor(String pattern) {
         this.cache = new ConcurrentHashMap<String, String>();
         this.pattern = Pattern.compile(pattern);
      }  
      
      public String extractUser(Request request) throws Exception {
         try {
            Certificate certificate = request.getClientCertificate();
            
            if(certificate != null) {
               X509Certificate[] certificates = certificate.getChain();
               
               for(X509Certificate entry : certificates) {
                  String user = extractCertificateUser(entry);
                  
                  if(user != null) {
                     return user;
                  }
               }
            }
         } catch(Exception e) {
            e.printStackTrace();
         }
         return null;
      } 
      
      private String extractCertificateUser(X509Certificate certificate) throws Exception {
         Principal principal = certificate.getSubjectDN();
         String name = principal.getName();
         String user = cache.get(name);
         
         if(user == null) {
            if(!cache.containsKey(name)) {
               Matcher matcher = pattern.matcher(name);
            
               if(matcher.matches()) {
                  user = matcher.group(1);
               }
               cache.put(name, user);
            }
         }
         return user;
      }
   }
}
