package org.simpleframework.transport.reactor;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import org.simpleframework.transport.trace.Trace;

public class AcceptTest extends TestCase {

//   public void testAcceptWithDistributor() throws Exception {
//      Executor executor = new ConcurrentExecutor(Acceptor.class);
//      ActionDistributor distributor = new ActionDistributor(executor);
//      InetSocketAddress address = new InetSocketAddress(4457);
//      CountDownLatch latch = new CountDownLatch(1);
//      Acceptor acceptor = new Acceptor(createChannel(address), latch);
//      
//      distributor.process(acceptor, SelectionKey.OP_ACCEPT);
//      latch.await();
//   }
   
//   public void testAcceptWithActionSelector() throws Exception {
//      ActionSelector selector = new ActionSelector();
//      InetSocketAddress address = new InetSocketAddress(4457);
//      SelectableChannel channel = createChannel(address);
//      
//      selector.register(channel, SelectionKey.OP_ACCEPT);
//      while(true) {
//         selector.select(5000);
//         List<ActionSet> sets = selector.selectedSets();
//         if(!sets.isEmpty()) {
//            System.err.println("ACCEPTED");
//         }
//      }
//   }
   
//   public void testAcceptWithSelector() throws Exception {
//      Selector selector = Selector.open();
//      InetSocketAddress address = new InetSocketAddress(4457);
//      SelectableChannel channel = createChannel(address);
//      
//      channel.register(selector, SelectionKey.OP_ACCEPT);
//      while(true) {
//         selector.select();
//         System.err.println("SELECTED");
//         Set<SelectionKey> sets = selector.selectedKeys();
//         if(!sets.isEmpty()) {
//            System.err.println("ACCEPTED");
//         }
//      }
//   }
   
//   public void testAcceptWithServerSocket() throws Exception {
//      ServerSocket socket = new ServerSocket(4457);
//      socket.accept();
//      System.err.println("ACCEPTED");
//   }
   
   public void testAcceptWithSelectorLoop() throws Exception {
      Selector selector = Selector.open();
      ServerSocketChannel server = ServerSocketChannel.open();
      ServerSocket socket = server.socket();
      InetSocketAddress address = new InetSocketAddress(4460);

      server.configureBlocking(false);
      socket.setReuseAddress(true);
      Thread.sleep(100);
      socket.bind(address, 100);
      server.register(selector, SelectionKey.OP_ACCEPT);

      for(int i = 0; i < 1000; i++){
         System.err.println("selecting="+address+ " i="+i);
         selector.select(5000);
         
         Set<SelectionKey> selectedKeys = selector.selectedKeys();
         Iterator<SelectionKey> iter = selectedKeys.iterator();
         
         while (iter.hasNext()) {
            SelectionKey key = iter.next();
         
            if (key.isAcceptable()) {
               SocketChannel client = server.accept();
               client.configureBlocking(false);
               System.err.println("channel="+client);
            }else{
               System.err.println("key="+key);
            }
         }
      }
   }
   
   private SelectableChannel createChannel(InetSocketAddress address) throws Exception {
      ServerSocketChannel listener = ServerSocketChannel.open();
      ServerSocket socket = listener.socket();
      listener.configureBlocking(false);
      socket.setReuseAddress(true);
      socket.bind(address, 100);
      return listener;
   }
   
   public static class Acceptor implements Operation {
      
      private final SelectableChannel channel;
      private final CountDownLatch latch;
      
      public Acceptor(SelectableChannel channel, CountDownLatch latch) {
         this.channel = channel;
         this.latch = latch;
      }

      public void run() {
         System.err.println("ACCEPTED");
         latch.countDown();
      }

      public Trace getTrace() {
         return new Trace(){

            public void trace(Object event) {
               trace(event, null);
            }

            public void trace(Object event, Object value) {
               System.err.println("event="+event+" value="+value);
            }
         };
      }

      public SelectableChannel getChannel() {
         return channel;
      }

      public void cancel() {
         System.err.println("CANCEL");
      }
      
   }
}
