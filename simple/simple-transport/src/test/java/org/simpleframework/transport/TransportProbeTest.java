package org.simpleframework.transport;

import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.SSLContext;

import junit.framework.TestCase;

public class TransportProbeTest extends TestCase {

   public void testPlainProbe() throws Exception {
      TransportProbe probe = new TransportProbe(512);
      TransportType type = null;
      
      type = probe.update(ByteBuffer.wrap("GET".getBytes("UTF-8")));
      assertEquals(type, TransportType.UNKNOWN);
      
      type = probe.update(ByteBuffer.wrap(" ".getBytes("UTF-8")));
      assertEquals(type, TransportType.UNKNOWN);
      
      type = probe.update(ByteBuffer.wrap("/index.html".getBytes("UTF-8")));
      assertEquals(type, TransportType.PLAIN);
   }
   
   public void testSecureProbe() throws Exception {
      TransportProbe probe = new TransportProbe(512);
      byte[] header = new SSLHandshakePeek().peek();
      TransportType type = null;
      
      type = probe.update(ByteBuffer.wrap(header));
      assertEquals(type, TransportType.SECURE);
   }
   
   private static class SSLHandshakePeek implements Runnable {
      
      private final BlockingQueue<byte[]> queue;
      private final ServerSocket server;
      
      public SSLHandshakePeek() throws Exception {
         this.queue = new LinkedBlockingQueue<byte[]>();
         this.server = new ServerSocket(0);
      }
      
      public byte[] peek() throws Exception {
         try {
            Thread thread = new Thread(this);
            thread.start();
            int port = server.getLocalPort();
            java.net.Socket socket = SSLContext.getDefault().getSocketFactory().createSocket("localhost",  port);
            socket.getOutputStream().write("GET / HTTP/1.0".getBytes());
            return queue.take();
         } catch(Exception e) {
            e.printStackTrace();
         }
         return queue.take();
      }
      
      public void run() {
         try{
            java.net.Socket socket = server.accept();
            InputStream input = socket.getInputStream();
            byte[] data = new byte[10];
            
            input.read(data);
            queue.offer(data);
            socket.close();
            server.close();
         } catch(Exception e){
            e.printStackTrace();
         }
      }
      
   }
}
