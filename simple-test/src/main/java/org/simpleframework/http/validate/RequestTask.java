package org.simpleframework.http.validate;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;


/**
 * <request method='GET' target='/path.html'>
 *    <header name='Content-Type'>text/plain</header>
 *    <header name='Content-Length'>10</header>
 *    <body>name=value&a=b</body>
 * </request>
 *
 */
@Root
public class RequestTask {
   
   @Attribute
   private Method method;
   
   @Attribute
   private String target;
   
   @Element(data=true, required=false)
   private String body;
   
   @ElementList(inline=true)
   private List<Header> header;
   
   @Attribute(required=false)
   private boolean debug;

   public Method getMethod() {
      return method;
   }
   
   public String getTarget() {
      return target;
   }
   
   public byte[] getRequest() throws Exception {
      StringBuilder builder = new StringBuilder();      
      append(builder, null, false);
      String result = builder.toString();
      
      if(debug) {
         System.out.println(result);
      }
      return result.getBytes("ISO-8859-1");
   }
   
   public byte[] getRequest(int count) throws Exception {
      StringBuilder builder = new StringBuilder();
      
      for(int i = 0; i < count -1; i++) {
         append(builder, i + 1, false);         
      }
      append(builder, count, true);      
      String result = builder.toString();
      
      if(debug) {
         System.out.println(result);
      }
      return result.getBytes("ISO-8859-1");
   }
   
   private void append(StringBuilder builder, Integer sequence, boolean close) {
      builder.append(method.name());
      builder.append(" ").append(target);
      builder.append(" HTTP/1.1\r\n");
      
      for(Header entry : header){
         if(!entry.getName().equalsIgnoreCase("Content-Length") &&
            !entry.getName().equalsIgnoreCase("Connection")) 
         {
            entry.append(builder);
         }
      }
      int length = 0;
      
      if(body != null) {
         length = body.length();
      }
      builder.append("Content-Length: ").append(length);
      builder.append("\r\n");
      
      if(sequence != null) {
         builder.append("Sequence: "+sequence+"\r\n");              
      }
      if(close) {
         builder.append("Connection: close\r\n");
      } else {       
         builder.append("Connection: keep-alive\r\n");
      }      
      builder.append("\r\n");
            
      if(length > 0) {
         builder.append(body);
      }
   }
   
   @Root
   private static class Header {
      @Attribute
      private String name;
      @Text
      private String value;
      
      private String getName(){
         return name;
      }
      
      public void append(StringBuilder builder){
         builder.append(name).append(": ").append(value).append("\r\n");
      }
   }

}
