package org.simpleframework.http.validate;

import java.io.IOException;
import java.net.Socket;

public class SocketConnector {

   public Socket connect(String host, int port) throws IOException {
      return new Socket(host, port);
   }
}
