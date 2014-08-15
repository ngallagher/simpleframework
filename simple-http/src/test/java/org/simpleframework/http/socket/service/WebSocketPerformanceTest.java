package org.simpleframework.http.socket.service;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerProcessor;
import org.simpleframework.http.core.StreamCursor;
import org.simpleframework.http.message.ReplyConsumer;
import org.simpleframework.http.socket.DataFrame;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameListener;
import org.simpleframework.http.socket.FrameType;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.WebSocket;
import org.simpleframework.http.socket.WebSocketAnalyzer;
import org.simpleframework.transport.Processor;
import org.simpleframework.transport.ProcessorServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.transport.trace.Analyzer;
import org.simpleframework.util.buffer.Allocator;
import org.simpleframework.util.buffer.ArrayAllocator;

public class WebSocketPerformanceTest {

   public static class MessageCounter implements FrameListener {
      
      private final AtomicInteger counter;
      
      public MessageCounter(AtomicInteger counter) {
         this.counter = counter;
      }

      public void onFrame(Session socket, Frame frame) {
         counter.getAndIncrement();
      }

      public void onError(Session socket, Exception cause) {
         System.err.println("onError(");
         cause.printStackTrace();
         System.err.println(")");
      }

      public void onOpen(Session socket) {
         System.err.println("onOpen(" + socket +")");
      }

      public void onClose(Session session, Reason reason) {
         System.err.println("onClose(" + reason +")");
      }
   }
   
   public static class MessageGeneratorService extends Thread implements Service {
      
      private static final String MESSAGE = 
      "{'product': {\r\n"+
      "  'id': '1234',\r\n"+
      "  'name': 'AU3TB00001256',\r\n"+
      "  'values': {\r\n"+
      "    'best': [\r\n"+
      "      {'bid': '13.344'},\r\n"+
      "      {'offer': '12.1'},\r\n"+
      "      {'volume': '100000'}\r\n"+
      "    ]\r\n"+
      "  }\r\n"+
      "}}";
      
      private final Set<WebSocket> sockets;
      private final MessageCounter listener;
      private final AtomicInteger counter;
      private final AtomicBoolean begin;

      public MessageGeneratorService() {
         this.sockets = new CopyOnWriteArraySet<WebSocket>();
         this.counter = new AtomicInteger();
         this.listener = new MessageCounter(counter);
         this.begin = new AtomicBoolean();
      }  
      
      public void begin() {
         if(begin.compareAndSet(false, true)) {
            start();
         }
      }
     
      public void connect(Session connection) {
         WebSocket socket = connection.getSocket();   
         
         try {
            sockets.add(socket);
            socket.register(listener);
         } catch(Exception e) {
            e.printStackTrace();
         }         
      }

      public void distribute(Frame frame) {
         try {         
            for(WebSocket socket : sockets) {
               try {                  
                  socket.send(frame);
               } catch(Exception e){
                  e.printStackTrace();
                  sockets.remove(socket);
                  socket.close();
               }
            }
         } catch(Exception e) {
            e.printStackTrace();
         }
      }
      
      public void run() {
         try {
            for(int i = 0; i < 10000000; i++) {
               distribute(new DataFrame(FrameType.TEXT, System.currentTimeMillis() + ":" + MESSAGE));
            }
         } catch(Exception e) {
            e.printStackTrace();
         }
      }
   }
   
   public static class MessageGeneratorContainer implements Container {   

      private final RouterContainer container;
      private final SocketAddress address;
      private final Connection connection;
      private final Allocator allocator;
      private final Processor processor;
      private final Router negotiator;
      private final Server server;
      
      public MessageGeneratorContainer(MessageGeneratorService service, Analyzer agent, int port) throws Exception {
         this.negotiator = new SingletonRouter(service);
         this.container = new RouterContainer(this, negotiator, 10, 1000, 100000);
         this.allocator = new ArrayAllocator();
         this.processor = new ContainerProcessor(container, allocator, 10);
         this.server = new ProcessorServer(processor, 10);
         this.connection = new SocketConnection(server, agent);
         this.address = new InetSocketAddress(port);
      }
      
      public void connect() throws Exception {
         container.start();      
         connection.connect(address);
      }
      
      public void handle(Request req, Response resp) {
         long time = System.currentTimeMillis();
         
         System.err.println(req);
            
         try {
            PrintStream out = resp.getPrintStream();
            
            resp.setDate("Date", time);
            resp.setValue("Server", "MessageGeneratorContainer/1.0");
            resp.setContentType("text/plain");            
            resp.setDate("Date", time);
            resp.setValue("Server", "MessageGeneratorContainer/1.0");
            resp.setContentType("text/html");            
    
            out.println("Your request is invalid as this is a websocket test!!");
            out.close();
         }catch(Exception e) {
            e.printStackTrace();
         }
      }
   }  
   
   public static class MessageGeneratorClient extends Thread {
      
      private final MessageGeneratorService service;
      private final AtomicInteger counter;
      private final AtomicLong duration;
      private final int port;
      private final boolean debug;
      
      public MessageGeneratorClient(MessageGeneratorService service, AtomicInteger counter, AtomicLong duration, int port, boolean debug) {
         this.duration = duration;
         this.counter = counter;
         this.service = service;
         this.debug = debug;
         this.port = port;
      }
      
      public void run() {
         try {
            Socket socket = new Socket("localhost", port);
            StreamCursor cursor = new StreamCursor(socket.getInputStream());
            FrameConsumer consumer = new FrameConsumer();
            ReplyConsumer response = new ReplyConsumer();
            
            byte[] request = ("GET /chat HTTP/1.1\r\n"+
               "Host: server.example.com\r\n"+
               "Upgrade: websocket\r\n"+
               "Connection: Upgrade\r\n"+
               "Sec-WebSocket-Key: x3JJHMbDL1EzLkh9GBhXDw==\r\n"+
               "Sec-WebSocket-Protocol: chat, superchat\r\n"+
               "Sec-WebSocket-Version: 13\r\n"+
               "Origin: http://example.com\r\n" +
               "\r\n").getBytes("ISO-8859-1");            
            
            socket.getOutputStream().write(request);
            
            while(cursor.isOpen()) {
               response.consume(cursor);
               
               if(response.isFinished()) {
                  System.err.println(response);
                  break;
               }
            }
            service.begin();            
            
            while(cursor.isOpen()) {
               consumer.consume(cursor);
               
               if(consumer.isFinished()) {
                  Frame frame = consumer.getFrame();
                  
                  if(frame != null) {
                     validate(frame);
                  }
                  consumer.clear();
               }
            }
         } catch(Exception e) {
            e.printStackTrace();
         }
      }
      
      public void validate(Frame frame) throws Exception {
         FrameType type = frame.getType();
         
         if(type == FrameType.TEXT) {
            String text = frame.getText();
            int index = text.indexOf(':');
            String time = text.substring(0, index);            
            long sendTime = Long.parseLong(time);
            long timeElapsed = System.currentTimeMillis() - sendTime;
            
            duration.getAndAdd(timeElapsed);
            counter.getAndIncrement();
            
            if(debug) {                                    
               System.err.println("count=" + counter + " text="+text + " time="+duration);            
            }
         }
      }
   }
   
   public static class MessageTimer extends Thread {
      
      private final AtomicLong duration;
      private final AtomicInteger counter;
      
      public MessageTimer(AtomicInteger counter, AtomicLong duration) {
         this.duration = duration;
         this.counter = counter;
      }
      
      public void run() {
         while(true) {
            try {
               Thread.sleep(1000);
               int count = counter.getAndSet(0);
               long total = duration.getAndSet(0);              
               long average = (total > 0 ? total : 1) / (count > 0 ? count : 1);               
               
               System.err.println("framesPerSecond="+count+" millisPerFrame="+average);
            } catch(Exception e) {
               e.printStackTrace();
            }
         }
      }
   }
   
   public static void main(String[] list) throws Exception {
      Analyzer agent = new WebSocketAnalyzer(false);
      AtomicLong duration = new AtomicLong();
      AtomicInteger counter = new AtomicInteger();
      MessageGeneratorService service = new MessageGeneratorService();
      MessageGeneratorContainer container = new MessageGeneratorContainer(service, agent, 7070);
      MessageTimer timer = new MessageTimer(counter, duration);
      
      timer.start();
      container.connect();
      
      for(int i = 0; i < 50; i++) {
         MessageGeneratorClient client = new MessageGeneratorClient(service, counter, duration, 7070, false);
         client.start();
      }
   }
}
 