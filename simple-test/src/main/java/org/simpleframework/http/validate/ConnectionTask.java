package org.simpleframework.http.validate;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Transient;
import org.simpleframework.xml.core.Commit;

@Root
public class ConnectionTask {
   
   @Element
   private RequestTask request;
   
   @Attribute
   private Protocol protocol;
   
   @Attribute
   private String host;
   
   @Attribute
   private int port;
   
   @Attribute
   private int repeat;
   
   @Attribute
   private double throttle;
   
   @Transient
   private byte[] pipeline;
   
   @Transient
   private InetSocketAddress address;
   
   @Transient
   private SocketConnector connector;
   
   @Commit
   private void commit() throws Exception {
      address = new InetSocketAddress(host, port);
      pipeline = request.getRequest(repeat);
      
      if(protocol == Protocol.HTTPS) {
         connector = new SecureSocketConnector();         
      } else {
         connector = new SocketConnector();
      }
   }
   
   public int getRepeat() {
      return repeat;
   }
   
   public Buffer execute(Client client) throws Exception {
      String host = address.getHostName();
      int port = address.getPort();
      Socket socket = connector.connect(host, port);
      
      return client.execute(socket, pipeline, throttle);
   }
}
