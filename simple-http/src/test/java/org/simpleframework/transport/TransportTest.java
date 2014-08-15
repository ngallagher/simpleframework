package org.simpleframework.transport;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.TestCase;

import org.simpleframework.transport.reactor.ExecutorReactor;
import org.simpleframework.transport.reactor.Reactor;

/**
 * Measure the performance of the transports to ensure that the perform 
 * well and that they send the correct sequence of bytes and that the
 * blocks sent are in the correct order. This also performs a comparison
 * with direct socket output streams to ensure there is a reasonable
 * performance difference.  
 * 
 * @author Niall Gallagher 
 */
public class TransportTest extends TestCase {

   private static final int REPEAT = 1000;

   public void testTransport() throws Exception {
      testTransport(REPEAT);
   }

   public void testTransport(int repeat) throws Exception {    
      for(int i = 1; i < 7; i++) { // just do some random sizes 
         testTransport(i, 100);
      }
      for(int i = 4092; i < 4102; i++) {
         testTransport(i, 100);
      }
      for(int i = 8190; i < 8200; i++) {
         testTransport(i, 100);
      }
      for(int i = 11282; i < 11284; i++) {
         testTransport(i, 1000);
      }
      for(int i = 204800; i < 204805; i++) {
         testTransport(i, 1000);
      }
      testTransport(16, repeat);
      testTransport(64, repeat);
      testTransport(256, repeat);
      testTransport(1024, repeat);
      testTransport(2048, repeat);
      testTransport(4096, repeat); 
      testTransport(4098, repeat);
      testTransport(8192, repeat);
      testTransport(8197, repeat);
   }

   // Test blocking transport        
   private void testTransport(int size, int repeat) throws Exception {
     // ThreadDumper dumper = new ThreadDumper();
      SocketConsumer consumer = new SocketConsumer(size, repeat);
      SocketAddress address = new InetSocketAddress("localhost", consumer.getPort());
      SocketChannel channel = SocketChannel.open();
      channel.configureBlocking(false); // underlying socket must be non-blocking
      channel.connect(address);

      while(!channel.finishConnect()) { // wait to finish connection
         Thread.sleep(10);
      };
      ExecutorService executor = Executors.newFixedThreadPool(20);
      Reactor reactor = new ExecutorReactor(executor);
     // Transport transport = new SocketTransport(channel, reactor, 2, 3);//XXX bug
      MockSocket pipeline = new MockSocket(channel);
      Transport transport = new SocketTransport(pipeline, reactor, 8192);
      OutputStream out = new TransportOutputStream(transport);

     // dumper.start();
      testOutputStream(consumer, out, size, repeat);

      out.close();
      executor.shutdown();
      channel.close();
      reactor.stop();
    //  dumper.kill();
      Thread.sleep(100);
   }

   public void s_testSocket() throws Exception {
      s_testSocket(REPEAT);
   }

   public void s_testSocket(int repeat) throws Exception {
      testSocket(16, repeat);
      testSocket(64, repeat);
      testSocket(256, repeat);
      testSocket(1024, repeat);
      testSocket(2048, repeat);
      testSocket(4098, repeat);
      testSocket(8192, repeat);
   }

   // Test blocking socket
   private void testSocket(int size, int repeat) throws Exception {
     // ThreadDumper dumper = new ThreadDumper();
      SocketConsumer consumer = new SocketConsumer(size, repeat);
      Socket socket = new Socket("localhost", consumer.getPort());
      OutputStream out = socket.getOutputStream();

      //dumper.start();
      testOutputStream(consumer, out, size, repeat);

      out.close();
      socket.close();
      //dumper.kill();
      Thread.sleep(100);
   }

   private class AlpahbetIterator {

      private byte[] alphabet = "abcdefghijklmnopqstuvwxyz".getBytes();

      private int off;

      public byte next() {
         if(off == alphabet.length) {
            off = 0;
         }
         return alphabet[off++];
      }

      public void reset() {
         off = 0;
      }
   }

   private void testOutputStream(SocketConsumer consumer, OutputStream out, int size, int repeat) throws Exception {
      byte[] block = new byte[size]; // write size
      AlpahbetIterator it = new AlpahbetIterator(); // write known data   

      for(int i = 1; i < block.length; i++) {
         block[i] = it.next();
      }
      AtomicLong count = new AtomicLong();
      PerformanceMonitor monitor = new PerformanceMonitor(consumer, count, out.getClass().getSimpleName(), size);

      for(int i = 0; i < repeat; i++) {
         block[0] = (byte) i; // mark the first byte in the block to be sure we get blocks in sequence
         //System.err.println("["+i+"]"+new String(block,"ISO-8859-1"));
         out.write(block);  // manipulation of the underlying buffer is taking place when the compact is invoked, this is causing major problems as the next packet will be out of sequence
         count.addAndGet(block.length);
      }
      Thread.sleep(2000); // wait for all bytes to flush through to consumer
      monitor.kill();
   }

   private class PerformanceMonitor extends Thread {
      private AtomicLong count;

      private volatile boolean dead;
      
      private SocketConsumer consumer;

      private String name;

      private int size;

      public PerformanceMonitor(SocketConsumer consumer, AtomicLong count, String name, int size) {
         this.consumer = consumer;
         this.count = count;
         this.name = name;
         this.size = size;
         this.start();
      }

      public void run() {
         int second = 0;
         while(!dead) {
            try {
               long octets = count.longValue();
               System.out.printf("%s,%s,%s,%s,%s%n", name, size, second++, octets, consumer.getWindow());
               Thread.sleep(1000);
            } catch(Exception e) {
               e.printStackTrace();
            }
         }
      }

      public void kill() throws Exception {
         dead = true;
      }
   }

   private class SocketConsumer extends Thread {

      private ServerSocket server;

      private Window window;
      
      private long repeat;

      private long size;

      public SocketConsumer(int size, int repeat) throws Exception {
         this.window = new Window(20);
         this.server = getSocket();
         this.repeat = repeat;
         this.size = size;
         this.start();
      }
      
      public int getPort() {
         return server.getLocalPort();
      }
      
      public String getWindow() {
         return window.toString();
      }
      
      private ServerSocket getSocket() throws Exception {
         // Scan the ephemeral port range
         for(int i = 2000; i < 10000; i++) { // keep trying to grab the socket 
            try {
               ServerSocket socket = new ServerSocket(i);
               System.out.println("port=["+socket.getLocalPort()+"]");
               return socket;
            } catch(Exception e) {
               Thread.sleep(200);
            }
         }
         // Scan a second time for good measure, maybe something got freed up
         for(int i = 2000; i < 10000; i++) { // keep trying to grab the socket 
            try {
               ServerSocket socket = new ServerSocket(i);
               System.out.println("port=["+socket.getLocalPort()+"]");
               return socket;
            } catch(Exception e) {
               Thread.sleep(200);
            }
         }
         throw new IOException("Could not create a client socket");
      }

      public void run() {
         long count = 0;
         int windowOctet = 0;
         int expectWindowOctet = 0;

         try {
            Socket socket = server.accept();
            InputStream in = socket.getInputStream();
            InputStream source = new BufferedInputStream(in);
            AlpahbetIterator it = new AlpahbetIterator();

            scan: for(int i = 0; i < repeat; i++) {
               int octet = source.read(); // check first byte in the block to make sure its correct in sequence
               
               if(octet == -1) {
                  break scan;
               }
               count++; // we have read another byte
               windowOctet = octet & 0x000000ff;
               expectWindowOctet = i & 0x000000ff;
               
               if((byte) octet != (byte) i) {
                  throw new Exception("Wrong sequence of blocks sent, was "
                        + (byte)octet + " should have been " + (byte)i + " count is "+count+" window is "+window+" compare "+explore(it, source, 5));
               }
               window.recieved(octet);
               
               for(int j = 1, k = 0; j < size; j++, k++) {
                  octet = source.read();

                  if(octet == -1) {
                     break scan;
                  }
                  byte next = it.next();
                   
                  if((byte) octet != next) {
                     throw new Exception("Invalid data received expected "+((byte)octet)+"("+((char)octet)+
                           ") but was "+next+"("+((char)next)+") total count is "+count+" block count is "+k+" window is expected "+
                           expectWindowOctet+"("+((char)expectWindowOctet)+")("+((byte)expectWindowOctet)+") got "+windowOctet+"("+
                           ((char)windowOctet)+")("+((byte)windowOctet)+") "+window+" compare "+explore(it, source, 5));
                  }
                  count++;
               }
               it.reset();
            }
         } catch(Throwable e) {
            e.printStackTrace();
         }
         if(count != size * repeat) {
            new Exception("Invalid number of bytes read, was " + count
                  + " should have been " + (size * repeat)).printStackTrace();
         }      
         try {
           // server.close();         
         }catch(Exception e) {
            e.printStackTrace();
         }      
      }
      
      private String explore(AlpahbetIterator it, InputStream source, int count) throws IOException {
         StringBuffer buf = new StringBuffer();
         buf.append("expected (");
         for(int i = 0; i < count; i++) {
            buf.append( (char)it.next() );
         }
         buf.append(") is (");
         for(int i = 0; i < count; i++) {
            buf.append(  (char)source.read()  );
         }
         buf.append(")");
         return buf.toString();
      }
   }
   

   private static class TransportOutputStream extends OutputStream {

      private Transport transport;

      public TransportOutputStream(Transport transport) {
         this.transport = transport;
      }

      public void write(int octet) throws IOException {
         byte[] data = new byte[] { (byte) octet };
         write(data);
      }

      public void write(byte[] data, int off, int len) throws IOException {
         try {
            ByteBuffer buffer = ByteBuffer.wrap(data, off, len);
            ByteBuffer safe = buffer.asReadOnlyBuffer();
            
            if(len > 0) {
               transport.write(safe);
            }
         } catch(Exception e) {
            e.printStackTrace();
            throw new IOException("Write failed");
         }
      }

      public void flush() throws IOException {
         try {
            transport.flush();
         } catch(Exception e) {
            e.printStackTrace();
            throw new IOException("Flush failed");
         }
      }

      public void close() throws IOException {
         try {
            transport.close();
         } catch(Exception e) {
            e.printStackTrace();
            throw new IOException("Close failed");
         }
      }

   }
   
   private static class Window {
      
      private final LinkedList<String> window;
      private final int size;
      
      public Window(int size) {
         this.window = new LinkedList<String>();
         this.size = size;
      }
      
      public synchronized void recieved(int sequence) {
         window.addLast(String.valueOf(sequence));
         
         if(window.size() > size) {
            window.removeFirst();
         }
      }
      
      public synchronized String toString() {
         StringBuilder builder = new StringBuilder("[");
         String delim = "";
         for(String b : window) {
            builder.append(delim).append(b);
            delim=", ";
         }
         builder.append("]");
         return builder.toString();
      }
   }

}
