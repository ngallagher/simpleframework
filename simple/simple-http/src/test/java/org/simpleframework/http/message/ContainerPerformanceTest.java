package org.simpleframework.http.message;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.http.core.ThreadDumper;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.transport.trace.Trace;
import org.simpleframework.transport.trace.TraceAnalyzer;

public class ContainerPerformanceTest extends TestCase {

   private static final int ITERATIONS = 100000;
   private static final int THREADS = 50;
   
   private static final byte[] SOURCE_1 =
   ("GET /index.html HTTP/1.1\r\n"+
   "Content-Type: application/x-www-form-urlencoded\r\n"+
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
   ("GET /tmp/amazon_files/in-your-city-blue-large._V256095983_.gif HTTP/1.1\r\n"+
   "Accept-Encoding: gzip, deflate\r\n"+
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
   
   private static final byte[] CLOSE = 
   ("GET /final_resource.gif HTTP/1.1\r\n"+
   "Accept-Encoding: gzip, deflate\r\n"+
   "Connection: keep-alive\r\n"+
   "Referer: http://localhost:9090/tmp/amazon.htm\r\n"+
   "Cache-Control: max-age=0\r\n"+
   "Host: localhost:9090\r\n"+
   "Accept-Language: en-US\r\n"+
   "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Version/3.1 Safari/525.13\r\n"+
   "Accept: */*\r\n"+
   "Conection: close\r\n"+
   "\r\n").getBytes();
   
   private static final byte[] RESPONSE_1 =
   ("{'product': {\r\n"+
   "  'id': '1234',\r\n"+
   "  'name': 'AU3TB00001256',\r\n"+
   "  'values': {\r\n"+
   "    'best': [\r\n"+
   "      {'bid': '13.344'},\r\n"+
   "      {'offer': '12.1'},\r\n"+
   "      {'volume': '100000'}\r\n"+
   "    ]\r\n"+
   "  }\r\n"+
   "}}").getBytes();

   // push through as many valid HTTP/1.1 requests as possible
   public void testPerformance() throws Exception {
      final AtomicInteger counter = new AtomicInteger(ITERATIONS * THREADS * 4);
      final CountDownLatch latch = new CountDownLatch(1);
      List<Thread> threads = new ArrayList<Thread>();
      ThreadDumper dumper = new ThreadDumper();
      //TraceAnalyzer analyzer = new DebugTraceAnalyzer(counter, true);
      TraceAnalyzer analyzer = new DebugTraceAnalyzer(counter, false);
      CounterContainer container = new CounterContainer(counter, latch);
      ContainerSocketProcessor processor = new ContainerSocketProcessor(container, 50, 1);
      Connection connection = new SocketConnection(processor, analyzer);
      InetSocketAddress address = (InetSocketAddress)connection.connect(null); // ephemeral port
      Thread.sleep(1000);

      for(int i = 0; i < THREADS; i++) {
         final SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", address.getPort()));
         client.configureBlocking(true);
         client.finishConnect();
         Thread.sleep(10);
         
         while(!client.finishConnect()) {
            Thread.sleep(10);
         }
         System.err.println("connected="+client.isConnected()+" blocking="+client.isBlocking());
         
         // read the HTTP/1.1 responses from the TCP stream so it does not fill the TCP window
         Thread readThread = new Thread() {
            public void run() {
               try {
                  byte[] data = new byte[8192];
                  
                  while(client.isConnected()) {
                     client.read(ByteBuffer.wrap(data));
                  }
               }catch(Exception e){
                  e.printStackTrace();
               }
            }
         };
         
         // write the HTTP/1.1 requests down the socket for the server to parse and dispatch
         Thread writeThread = new Thread() {
            public void run() {
               try {
                  for(int i = 0; i < ITERATIONS; i++) {
                     client.write(ByteBuffer.wrap(SOURCE_1));
                     client.write(ByteBuffer.wrap(SOURCE_2));
                     client.write(ByteBuffer.wrap(SOURCE_3));
                     client.write(ByteBuffer.wrap(SOURCE_4));
                     Thread.sleep(1);
                  }
                  client.write(ByteBuffer.wrap(CLOSE));
                  client.close();
               } catch(Exception e){
                  e.printStackTrace();
               }
            }
         };
         readThread.start();
         writeThread.start();
         threads.add(readThread);
         threads.add(writeThread);
      }
      dumper.start();
      
      // wait for all clients to finish
      for(Thread thread : threads){
         thread.join();
      }
      latch.await();
      connection.close(); 
      dumper.kill();
   }
   
   // This is a container that counts the callbacks/requests it gets and sends a valid HTTP/1.1 response
   private class CounterContainer implements Container {

      private final AtomicInteger counter;
      private final CountDownLatch latch;
      private final long start;
      private final int require;
      
      public CounterContainer(AtomicInteger counter, CountDownLatch latch) {
         this.start = System.currentTimeMillis();
         this.require = counter.get();
         this.counter = counter;
         this.latch = latch;
      }
      
      public void handle(Request req, Response resp) {
         try {
            OutputStream out = resp.getOutputStream();
            String target = req.getPath().getPath(); // parse the HTTP request URI
            
            resp.setValue("Content-Type", "application/json");
            resp.setValue("Connection", "keep-alive");
            resp.setValue("X-Request-URI", target);
            resp.setContentLength(RESPONSE_1.length);
            out.write(RESPONSE_1);
            out.close();
            
            int count = counter.decrementAndGet();
            int total = require - count;
            
            if(total % 100000 == 0) {
               long duration = System.currentTimeMillis() - start;
               DecimalFormat format = new DecimalFormat("###,###,###,###.##");
               
               System.err.println("Request: " + format.format(total) + " in " + format.format(duration) + " which is " + format.format(total / duration) + " per ms and "+format.format(total/(Math.max(duration,1.0)/1000.0))+" per second");
            }
            if(count == 0){
               latch.countDown();
            }
         }catch(Exception e){
            e.printStackTrace();
         }
      }
   }
   
   // This is just for debugging the I/O if needed
   public class DebugTraceAnalyzer implements TraceAnalyzer {

      private final AtomicInteger counter;
      private final boolean debug;
      
      public DebugTraceAnalyzer(AtomicInteger counter, boolean debug){
         this.counter = counter;
         this.debug = debug;
      }

      public Trace attach(SelectableChannel channel) {
         return new DebugTrace(channel);
      }

      public void stop() {}
      
      private class DebugTrace implements Trace {
         
         private final SelectableChannel channel;
      
         public DebugTrace(SelectableChannel channel) {
            this.channel = channel;
         }
         
         public void trace(Object event) {
            if(debug) {
               trace(event, "");
            }
         }
         
         public void trace(Object event, Object value) {
            if(debug) {
               if(value instanceof Throwable) {
                  StringWriter writer = new StringWriter();
                  PrintWriter out = new PrintWriter(writer);
                  ((Exception)value).printStackTrace(out);
                  out.flush();
                  value = writer.toString();
               }
               if(value != null && !String.valueOf(value).isEmpty()) {
                  System.err.printf("(%s) %s [%s] %s: %s%n", Thread.currentThread().getName(), channel, counter, event, value);
               } else {
                  System.err.printf("(%s) %s [%s] %s%n", Thread.currentThread().getName(), channel, counter, event);
               }          
            }
         } 
      }
   }
   
   public static void main(String[] list) throws Exception {
      new ContainerPerformanceTest().testPerformance();
   }
}
