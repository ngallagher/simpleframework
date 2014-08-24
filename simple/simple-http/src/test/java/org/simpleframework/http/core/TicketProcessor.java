package org.simpleframework.http.core;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.Socket;

class TicketProcessor implements SocketProcessor {
   
   private SocketProcessor delegate;

   public TicketProcessor(SocketProcessor delegate) {
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