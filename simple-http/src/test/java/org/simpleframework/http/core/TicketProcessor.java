package org.simpleframework.http.core;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.simpleframework.transport.Server;
import org.simpleframework.transport.Socket;

class TicketProcessor implements Server {
   
   private Server delegate;

   public TicketProcessor(Server delegate) {
      this.delegate = delegate;
   }

   public void process(Socket pipe) throws IOException {
      SocketChannel channel = pipe.getChannel();
      int port = channel.socket().getPort();

      pipe.getAttributes().put(Ticket.KEY,new Ticket(port));
      delegate.process(pipe);
   }

   public void stop() throws IOException {
      delegate.stop();
   }
}