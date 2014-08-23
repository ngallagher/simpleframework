package org.simpleframework.http.core;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.simpleframework.transport.SocketConnector;
import org.simpleframework.transport.Socket;

class TicketProcessor implements SocketConnector {
   
   private SocketConnector delegate;

   public TicketProcessor(SocketConnector delegate) {
      this.delegate = delegate;
   }

   public void connect(Socket pipe) throws IOException {
      SocketChannel channel = pipe.getChannel();
      int port = channel.socket().getPort();

      pipe.getAttributes().put(Ticket.KEY,new Ticket(port));
      delegate.connect(pipe);
   }

   public void stop() throws IOException {
      delegate.stop();
   }
}