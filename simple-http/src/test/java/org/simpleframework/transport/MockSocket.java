
package org.simpleframework.transport;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLEngine;

import org.simpleframework.transport.trace.MockTrace;
import org.simpleframework.transport.trace.Trace;

public class MockSocket implements Socket {

   private SocketChannel socket;
   private SSLEngine engine;
   private Map map;
  
   public MockSocket(SocketChannel socket) {
      this(socket, null);
   }
  
   public MockSocket(SocketChannel socket, SSLEngine engine) {
      this.map = new HashMap();
      this.engine = engine;
      this.socket = socket;
   } 
  
   public SSLEngine getEngine() {
      return engine;
   }
   
   public SocketChannel getChannel() {
      return socket;
   }
 
   public Map getAttributes() {
      return map;           
   }

   public Trace getTrace() {
      return new MockTrace();
   }
}

