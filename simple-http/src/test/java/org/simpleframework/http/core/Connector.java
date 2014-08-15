package org.simpleframework.http.core;

import java.net.Socket;

public interface Connector {

   public Socket getSocket() throws Exception;
   
}
