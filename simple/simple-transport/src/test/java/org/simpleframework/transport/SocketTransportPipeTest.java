package org.simpleframework.transport;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.TestCase;

import org.simpleframework.common.thread.ConcurrentExecutor;
import org.simpleframework.transport.reactor.ExecutorReactor;
import org.simpleframework.transport.reactor.Reactor;
import org.simpleframework.transport.trace.MockTrace;
import org.simpleframework.transport.trace.Trace;

public class SocketTransportPipeTest extends TestCase {
   
   private static final int ITERATIONS = 100000;

   public void testPipe() throws Exception {
      ServerSocket server = new ServerSocket(0);
      SocketAddress address = new InetSocketAddress("localhost", server.getLocalPort());
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
      java.net.Socket socket = server.accept();
      final InputStream read = socket.getInputStream();
      final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      final LinkedBlockingQueue<String> sent = new LinkedBlockingQueue<String>();
      final LinkedBlockingQueue<String> received = new LinkedBlockingQueue<String>();
      Thread thread = new Thread(new Runnable() {
         public void run(){
            try {
               byte[] token = new byte[]{'\r','\n'};
               int pos = 0;
               int count = 0;
               while((count = read.read()) != -1){
                  if(count != token[pos++]){
                     pos = 0;
                  }
                  if(pos == token.length) {
                     String value = buffer.toString().trim();
                     String expect = sent.take();
                     
                     if(!value.equals(expect)) {
                        throw new Exception("Out of sequence expected " + expect + " but got " + value);
                     }
                     received.offer(value);
                     buffer.reset();
                     pos = 0;
                  } else {
                     buffer.write(count);
                     System.err.write(count);
                     System.err.flush();
                  }
                     
               }
            }catch(Exception e){
               e.printStackTrace();
            }
         }
      });
      thread.start();
      for(int i = 0; i < ITERATIONS; i++) {
         String message = "message-"+i;
         transport.write(ByteBuffer.wrap((message+"\r\n").getBytes()));
         sent.offer(message);         
      }
      transport.flush();
      transport.close();
      
      for(int i = 0; i < ITERATIONS; i++) {
         assertEquals(received.take(), "message-"+i);         
      }
      assertTrue(sent.isEmpty());
      assertTrue(received.isEmpty());

   }
}
