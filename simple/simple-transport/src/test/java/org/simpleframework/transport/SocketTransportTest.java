package org.simpleframework.transport;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;

import junit.framework.TestCase;

import org.simpleframework.common.thread.ConcurrentExecutor;
import org.simpleframework.transport.reactor.ExecutorReactor;
import org.simpleframework.transport.reactor.Reactor;
import org.simpleframework.transport.trace.MockTrace;
import org.simpleframework.transport.trace.Trace;

public class SocketTransportTest extends TestCase {
   
   
   public void testBulkWrite() throws Exception {
      ServerBuffer reader = new ServerBuffer();
      SocketAddress address = new InetSocketAddress("localhost", reader.getPort());
      SocketChannel channel = SocketChannel.open();
      channel.configureBlocking(false); // underlying socket must be non-blocking
      channel.connect(address);

      while(!channel.finishConnect()) { // wait to finish connection
         Thread.sleep(10);
      };
      Trace trace = new MockTrace();
      SocketWrapper wrapper = new SocketWrapper(channel,  trace);
      Executor executor = new ConcurrentExecutor(Runnable.class);
      Reactor reactor = new ExecutorReactor(executor);
      SocketTransport transport = new SocketTransport(wrapper,reactor);
      for(int i = 0; i < 10000; i++){
         transport.write(ByteBuffer.wrap(("message-"+i+"\n").getBytes()));
      }
      transport.close();
      reader.awaitClose();
      
      String data = reader.getBuffer().toString();
      String[] list = data.split("\\n");
      
      for(int i = 0; i < 10000; i++){
         if(!list[i].equals("message-"+i)) {
            System.err.println(list[i]);
         }
         assertEquals("At index " + i + " value="+list[i] +" expect message-"+i, list[i], "message-"+i);
      }
   }
}
