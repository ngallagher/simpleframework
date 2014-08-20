package org.simpleframework.http.validate.test;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

public class Encoder {
   
   public static String encode(Request request) {
      return encode(request.toString());
   }
   
   public static String encode(Response response) {
      return encode(response.toString());
   }
   
   public static String encode(String header) {
      int length = header.length();
      StringBuilder builder = new StringBuilder(length * 2);

      for(int i = 0; i < length; i++) {
         char ch = header.charAt(i);

         if(ch == '\r') {
            builder.append("[\\r]");
         } else if(ch == '\n') {
            builder.append("[\\n]\n");
         } else {
            builder.append(ch);
         }
      }
      return builder.toString(); 
   }

}
