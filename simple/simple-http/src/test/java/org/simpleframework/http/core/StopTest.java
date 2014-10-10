package org.simpleframework.http.core;

import java.io.Closeable;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import junit.framework.TestCase;

import org.simpleframework.common.thread.ConcurrentExecutor;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

public class StopTest extends TestCase {
   
   private static final int ITERATIONS = 20;
   
   public void testStop() throws Exception {
      ThreadDumper dumper = new ThreadDumper();
      
      dumper.start();
      dumper.waitUntilStarted();
      
      ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
      int initialThreads = threadBean.getThreadCount();
      
      for(int i = 0; i < ITERATIONS; i++) {
         try {
            ServerCriteria criteria = createServer();
            InetSocketAddress address = criteria.getAddress();
            Connection connection = criteria.getConnection();
            Client client = createClient(address, String.format("[%s of %s]", i, ITERATIONS));
            
            Thread.sleep(2000); // allow some requests to execute
            connection.close();
            Thread.sleep(100); // ensure client keeps executing
            client.close();
            Thread.sleep(1000); // wait for threads to terminate
         }catch(Exception e) {
            e.printStackTrace();
         }         
         //assertEquals(initialThreads, threadBean.getThreadCount());
      }
      dumper.kill();
   }
  
   public static Client createClient(InetSocketAddress address, String tag) throws Exception {
      ConcurrentExecutor executor = new ConcurrentExecutor(Runnable.class, 20);
      int port = address.getPort();
      Client client = new Client(executor, port, tag);

      client.start();
      return client;
   }
   
   public static ServerCriteria createServer() throws Exception {
      Container container = new Container() {
         public void handle(Request request, Response response) {
            try {
               PrintStream out = response.getPrintStream();
               response.setValue("Content-Type", "text/plain");
               response.setValue("Connection", "close");
               
               out.print("TEST " + new Date());
               response.close();
            }catch(Exception e) {
               e.printStackTrace();
               try {
                  response.close();
               }catch(Exception ex) {
                  ex.printStackTrace();
               }
            }
         }
      };
      ContainerSocketProcessor server = new ContainerSocketProcessor(container);
      Connection connection = new SocketConnection(server);
      InetSocketAddress address = (InetSocketAddress)connection.connect(null); // ephemeral port
      
      return new ServerCriteria(connection, address);
   }
   
   private static class Client extends Thread implements Closeable {
      
      private ConcurrentExecutor executor;
      private RequestTask task;
      private volatile boolean dead;
      
      public Client(ConcurrentExecutor executor, int port, String tag) {
         this.task = new RequestTask(this, port, tag);
         this.executor = executor;
      }
      
      public boolean isDead() {
         return dead;
      }
      
      public void run() {
         try {
            while(!dead) {
               executor.execute(task);
               Thread.sleep(100);
            }
         }catch(Exception e) {
            e.printStackTrace();
         }
      }
      
      public void close() {
         dead = true;
         executor.stop();
      }
   }
   
   private static class RequestTask implements Runnable {
      
      private Client client;
      private String tag;
      private int port;
      
      public RequestTask(Client client, int port, String tag) {
         this.client = client;
         this.port = port;
         this.tag = tag;
      }
      
      public void run() {
         try {
            if(!client.isDead()) {
               URL target = new URL("http://localhost:"+port+"/");
               URLConnection connection = target.openConnection();
               
               // set a timeout
               connection.setConnectTimeout(10000);
               connection.setReadTimeout(10000);
               
               InputStream stream = connection.getInputStream();
               StringBuilder builder = new StringBuilder();
               int octet = 0;
               
               while((octet = stream.read()) != -1) {
                  builder.append((char)octet);
               }
               stream.close();
               System.out.println(tag + " " + Thread.currentThread() + ": " + builder);
            }
         }catch(Exception e) {
            e.printStackTrace();
         } 
      }
   }
   
   private static class ServerCriteria {

      private Connection connection;
      private InetSocketAddress address;
      
      public ServerCriteria(Connection connection, InetSocketAddress address){
         this.connection = connection;
         this.address = address;
      }
      public Connection getConnection() {
         return connection;
      }
      public InetSocketAddress getAddress() {
         return address;
      }
   }
}
