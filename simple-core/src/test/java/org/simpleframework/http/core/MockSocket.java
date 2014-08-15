package org.simpleframework.http.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class MockSocket extends Socket {
   
   private Socket socket;
   
   private OutputStream out;
   
   public MockSocket(Socket socket) {
      this(socket, System.err);      
   }
   
   public MockSocket(Socket socket, OutputStream out){
      this.socket = socket;
      this.out = out;
   }
   
   @Override
   public void setSoTimeout(int delay) throws SocketException {
      socket.setSoTimeout(delay);
   }
   
   @Override 
   public int getSoTimeout() throws SocketException {
      return socket.getSoTimeout();
   }
         
   
   public InputStream getInputStream() throws IOException {
      return socket.getInputStream();
   }
   
   public OutputStream getOutputStream() {
      return out;
   }
}
