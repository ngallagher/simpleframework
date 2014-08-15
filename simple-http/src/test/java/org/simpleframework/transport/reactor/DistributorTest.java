package org.simpleframework.transport.reactor;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;


public class DistributorTest extends TestCase {

   private static final String PAYLOAD =
   "POST /index.html HTTP/1.0\r\n"+
   "Content-Type: multipart/form-data; boundary=AaB03x\r\n"+
   "Accept: image/gif;q=1.0,\r\n image/jpeg;q=0.8,\r\n"+
   "   \t\t   image/png;\t\r\n\t"+
   "   q=1.0,*;q=0.1\r\n"+
   "Accept-Language: fr;q=0.1, en-us;q=0.4, en-gb; q=0.8, en;q=0.7\r\n"+
   "Host:   some.host.com    \r\n"+
   "Cookie: $Version=1; UID=1234-5678; $Path=/; $Domain=.host.com\r\n"+
   "Cookie: $Version=1; NAME=\"Niall Gallagher\"; $path=\"/\"\r\n"+
   "\r\n" +
   "--AaB03x\r\n"+
   "Content-Disposition: form-data; name='pics'; filename='file1.txt'\r\n"+
   "Content-Type: text/plain\r\n\r\n"+
   "example contents of file1.txt\r\n"+   
   "--AaB03x\r\n"+   
   "Content-Type: multipart/mixed; boundary=BbC04y\r\n\r\n"+
   "--BbC04y\r\n"+
   "Content-Disposition: form-data; name='pics'; filename='file2.txt'\r\n"+
   "Content-Type: text/plain\r\n\r\n"+
   "example contents of file3.txt ...\r\n"+
   "--BbC04y\r\n"+
   "Content-Disposition: form-data; name='pics'; filename='file3.txt'\r\n"+
   "Content-Type: text/plain\r\n\r\n"+
   "example contents of file4.txt ...\r\n"+
   "--BbC04y\r\n"+
   "Content-Disposition: form-data; name='pics'; filename='file4.txt'\r\n"+
   "Content-Type: text/plain\r\n\r\n"+
   "example contents of file4.txt ...\r\n"+ 
   "--BbC04y--\r\n"+
   "--AaB03x--\r\n";
   
   public class Client extends Thread {
      
      private CountDownLatch latch;
      private String message;
      private int requests;
      private int port;   
      
      public Client(CountDownLatch latch, String message, int port, int requests) throws Exception {
         this.message = message;
         this.requests = requests;
         this.port = port;
         this.latch = latch;
         this.start();
      }
      
      public void run() {
         try {        
            latch.await();
            
            Socket socket = new Socket("localhost", port);
            OutputStream out = socket.getOutputStream();
            byte[] payload = message.getBytes();            
            
            for(int i = 0; i < requests; i++){
               out.write(payload);
            }
            out.close();
         } catch(Exception e) {
            e.printStackTrace();
         }
      }     
   }
   
   public class Worker implements Operation {

      private BlockingQueue<Worker> done;
      private Reactor reactor;
      private SocketChannel channel;
      private ByteBuffer buffer;
      private String payload;
      private int accumulate;
      private long finish;
      private long start;
      private int id;
      
      public Worker(BlockingQueue<Worker> done, Reactor reactor, SocketChannel channel, String payload, int id) throws Exception {
         this.buffer = ByteBuffer.allocate(8192);    
         this.start = System.currentTimeMillis();
         this.finish = start + 60000;
         this.payload = payload;
         this.channel = channel;  
         this.reactor = reactor;
         this.done = done;
         this.id = id;
      }
      
      public long getExpiry(TimeUnit unit) {
         return unit.convert(finish - System.currentTimeMillis(), MILLISECONDS);
      }
      
      public int getAccumulate() {
         return accumulate;
      }
      
      // XXX should this be executed in a thread!!!!???? yes...
      public void cancel() {
         System.err.println("############################# Worker has been canceled");
      }
      
      public void run() {
         try {
            // N.B Fundamental to performance
            buffer.clear();            
            
            if(channel.isOpen()) {
               int count = channel.read(buffer);
               accumulate += count;
            
               System.err.println("Worker-"+id+" read ["+count +"] of payload sized ["+payload.length()+"] took ["+(System.currentTimeMillis() -start)+"]");
            
               if(count != -1) {               
                  reactor.process(this, SelectionKey.OP_READ);
               } else {
                  channel.close();
                  done.offer(this);
                  System.err.println("Worker-"+id+" Channel is closed after time ["+(System.currentTimeMillis() - start)+"] and read ["+accumulate+"]");
               }
            } else {
               System.err.println("Worker-"+id+" Channel is closed after time ["+(System.currentTimeMillis() - start)+"] and read ["+accumulate+"]");
               done.offer(this);
            }
         }catch(Exception e) {
            e.printStackTrace();
         }
      }
      
      public SocketChannel getChannel() {
         return channel;
      }
      
   }
   
   public class Server extends Thread {
      
      private BlockingQueue<SocketChannel> ready;
      private CountDownLatch latch;
      private ServerSocketChannel server;
      private Selector selector;
      private int port;
      
      public Server(CountDownLatch latch, BlockingQueue<SocketChannel> ready, int port) throws Exception {
         this.server = ServerSocketChannel.open();
         this.selector = Selector.open();
         this.latch = latch;
         this.port = port;         
         this.ready = ready;
         this.start();
      }
      
      private void configure() throws Exception {
         server.socket().bind(new InetSocketAddress(port));
         server.configureBlocking(false);
      }
      
      public void run() {
         try {
            configure();
            execute();
         } catch(Exception e) {
            e.printStackTrace();
         }
      }
      
      private void execute() throws Exception {        
         SelectionKey serverKey = server.register(selector, SelectionKey.OP_ACCEPT);
         
         latch.countDown();
         
         while(true){
            selector.select();
            Set keys = selector.selectedKeys();

            for(Iterator i = keys.iterator(); i.hasNext();){
               SelectionKey key = (SelectionKey) i.next();
               i.remove();

               if(key != serverKey) {
                  return;
               }
               if(key.isAcceptable()) {
                  SocketChannel channel = server.accept();
                  channel.configureBlocking(false);
                  ready.offer(channel);
               }               
            }
         }
      }     
   }   
   
   public static void main(String[] list) throws Exception {
      new DistributorTest().testReactor();
   }
   
   public void testReactor() throws Exception {      
      testReactor(PAYLOAD, 200, 100, 10, 8123);
   }
   
   private void testReactor(String payload, int clients, int requests,  int threads, int port) throws Exception {
      BlockingQueue<Worker> done = new LinkedBlockingQueue<Worker>();
      BlockingQueue<SocketChannel> ready = new LinkedBlockingQueue<SocketChannel>();
      CountDownLatch latch = new CountDownLatch(1);
      Server server = new Server(latch, ready, port);
      Executor executor = Executors.newFixedThreadPool(10);
      Reactor reactor = new ExecutorReactor(executor, 1);
      
      long start = System.currentTimeMillis();
      
      for(int i = 0; i < clients; i++) {
         new Client(latch, payload, port, requests);         
      }
      for(int i = 0; i < clients; i++) {
         SocketChannel channel = ready.take();
         Worker worker = new Worker(done, reactor, channel, payload, i);
         
         reactor.process(worker);         
      }
      int total = 0;
      
      for(int i = 0; i < clients; i++) {
         Worker worker = done.take();
         int accumulate = worker.getAccumulate();
         total += accumulate;
         System.err.println("Accumulated ["+accumulate+"] of ["+(requests*payload.length())+"] closed ["+worker.getChannel().socket().isClosed()+"]");
      }
      System.err.println("Accumulated ["+total+"] of ["+(clients*requests*payload.length())+"]");
      System.err.println("Total time to process ["+(clients*requests)+"] payloads from ["+clients+"] clients took ["+(System.currentTimeMillis() - start)+"]");
   }
   
   
   
   
}


