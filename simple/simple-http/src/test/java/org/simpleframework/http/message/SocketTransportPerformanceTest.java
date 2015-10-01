package org.simpleframework.http.message;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.DecimalFormat;

import junit.framework.TestCase;

import org.simpleframework.common.thread.ConcurrentExecutor;
import org.simpleframework.http.core.ThreadDumper;
import org.simpleframework.transport.SocketTransport;
import org.simpleframework.transport.SocketWrapper;
import org.simpleframework.transport.TransportCursor;
import org.simpleframework.transport.reactor.ExecutorReactor;
import org.simpleframework.transport.reactor.Reactor;
import org.simpleframework.transport.trace.Trace;

public class SocketTransportPerformanceTest extends TestCase {

   private static final int ITERATIONS = 1000000;
   
   private static final byte[] SOURCE_1 =
   ("POST /index.html HTTP/1.0\r\n"+
   "Content-Type: application/x-www-form-urlencoded\r\n"+
   "Content-Length: 42\r\n"+
   "Transfer-Encoding: chunked\r\n"+
   "Accept: image/gif;q=1.0,\r\n image/jpeg;q=0.8,\r\n"+
   "   \t\t   image/png;\t\r\n\t"+
   "   q=1.0,*;q=0.1\r\n"+
   "Accept-Language: fr;q=0.1, en-us;q=0.4, en-gb; q=0.8, en;q=0.7\r\n"+
   "Host:   some.host.com    \r\n"+
   "Cookie: $Version=1; UID=1234-5678; $Path=/; $Domain=.host.com\r\n"+
   "Cookie: $Version=1; NAME=\"Niall Gallagher\"; $path=\"/\"\r\n"+
   "\r\n").getBytes();
   
   private static final byte[] SOURCE_2 =
   ("GET /tmp/amazon_files/21lP7I1XB5L.jpg HTTP/1.1\r\n"+
   "Accept-Encoding: gzip, deflate\r\n"+
   "Connection: keep-alive\r\n"+
   "Referer: http://localhost:9090/tmp/amazon.htm\r\n"+
   "Cache-Control: max-age=0\r\n"+      
   "Host: localhost:9090\r\n"+
   "Accept-Language: en-US\r\n"+
   "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Version/3.1 Safari/525.13\r\n"+
   "Accept: */*\r\n" +
   "\r\n").getBytes();
   
   private static final byte[] SOURCE_3 = 
   ("GET /tmp/amazon_files/in-your-city-blue-large._V256095983_.gif HTTP/1.1Accept-Encoding: gzip, deflate\r\n"+
   "Connection: keep-alive\r\n"+
   "Referer: http://localhost:9090/tmp/amazon.htm\r\n"+
   "Cache-Control: max-age=0\r\n"+
   "Host: localhost:9090\r\n"+
   "Accept-Language: en-US\r\n"+
   "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Version/3.1 Safari/525.13\r\n"+
   "Accept: */*\r\n"+
   "\r\n").getBytes();

   private static final byte[] SOURCE_4 = 
   ("GET /tmp/amazon_files/narrowtimer_transparent._V47062518_.gif HTTP/1.1\r\n"+
   "Accept-Encoding: gzip, deflate\r\n"+
   "Connection: keep-alive\r\n"+
   "Referer: http://localhost:9090/tmp/amazon.htm\r\n"+
   "Cache-Control: max-age=0\r\n"+
   "Host: localhost:9090\r\n"+
   "Accept-Language: en-US\r\n"+
   "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Version/3.1 Safari/525.13\r\n"+
   "Accept: */*\r\n"+
   "\r\n").getBytes();

   public void testPerformance() throws Exception {
      SocketAddress address = new InetSocketAddress("localhost", 44455);
      ServerSocketChannel server = ServerSocketChannel.open();
      server.configureBlocking(false);
      server.bind(address);
      final SocketChannel writer = SocketChannel.open(address);
      writer.configureBlocking(true);
      writer.finishConnect();
      Thread.sleep(1000);
      SocketChannel reader = server.accept();
      while(!reader.finishConnect()) {
         Thread.sleep(1000);
      }
      while(!writer.finishConnect()) {
         Thread.sleep(1000);
      }
      DecimalFormat format = new DecimalFormat("###,###,###,###,###.###");
      ConcurrentExecutor executor = new ConcurrentExecutor(SocketTransport.class, 10);
      Reactor reactor = new ExecutorReactor(executor);
      Trace trace = new MockTrace();
      SocketWrapper wrapper = new SocketWrapper(reader, trace);
      SocketTransport transport = new SocketTransport(wrapper, reactor);
      TransportCursor cursor = new TransportCursor(transport);
      ThreadDumper dumper = new ThreadDumper();
      Thread thread = new Thread() {
         public void run() {
            try {
               for(int i = 0; i < ITERATIONS; i++) {
                  writer.write(ByteBuffer.wrap(SOURCE_1));
                  writer.write(ByteBuffer.wrap(SOURCE_2));
                  writer.write(ByteBuffer.wrap(SOURCE_3));
                  writer.write(ByteBuffer.wrap(SOURCE_4));
               }
               writer.close();
            } catch(Exception e){
               e.printStackTrace();
            }
         }
      };
      dumper.start();
      thread.start();
      long start = System.currentTimeMillis();
      int count = 0;
      
      while(true) {
         RequestConsumer header = new RequestConsumer();
      
         while(!header.isFinished()) {
            header.consume(cursor);
         }  
         header.getPath().getPath(); // parse address also
         int ready = cursor.ready();
         
         if(count++ % 50000 == 0) {
            System.err.println("Done: " +format.format(count) + " in " + (System.currentTimeMillis()-start) + " ms");
         }
         if(ready == -1) {
            break;
         }
      }
      long finish = System.currentTimeMillis();
      long duration = finish - start;
      
      System.err.printf("count=%s time=%s performance=%s (per/ms) performance=%s (per/sec)s%n", format.format(count), duration, format.format(count/duration), format.format(count/(duration/1000)));
      thread.join();
      executor.stop();
      reactor.stop(); 
      dumper.kill();
   }
   
   private static class MockTrace implements Trace {
      public void trace(Object event) {}
      public void trace(Object event, Object value) {}      
   }
   
   public static void main(String[] list) throws Exception {
      new SocketTransportPerformanceTest().testPerformance();
   }
}
