package org.simpleframework.http.socket;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class WebSocketTestClient {

   public static void main(String[] list) throws Exception {
      Socket socket = new Socket("localhost", 80);
      OutputStream out = socket.getOutputStream();
      byte[] request = ("GET / HTTP/1.0\r\n\r\n").getBytes("ISO-8859-1");
      out.write(request);
      InputStream in = socket.getInputStream();
      byte[] chunk = new byte[1024];
      int count = 0;
      
      while((count = in.read(chunk)) != -1) {
         Thread.sleep(1000);
         System.err.write(chunk, 0, count);
      }
            
      
   }
}
