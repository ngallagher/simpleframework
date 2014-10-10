package org.simpleframework.http.socket.service;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.ArrayAllocator;
import org.simpleframework.common.thread.Daemon;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerTransportProcessor;
import org.simpleframework.http.core.StreamCursor;
import org.simpleframework.http.core.ThreadDumper;
import org.simpleframework.http.message.ReplyConsumer;
import org.simpleframework.http.socket.DataFrame;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameListener;
import org.simpleframework.http.socket.FrameType;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.FrameChannel;
import org.simpleframework.http.socket.WebSocketAnalyzer;
import org.simpleframework.transport.TransportProcessor;
import org.simpleframework.transport.TransportSocketProcessor;
import org.simpleframework.transport.SocketProcessor;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.transport.trace.TraceAnalyzer;
import org.simpleframework.transport.trace.Trace;

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
      
      private final Set<FrameChannel> sockets;
      private final MessageCounter listener;
      private final AtomicInteger counter;
      private final AtomicBoolean begin;

      public MessageGeneratorService() {
         this.sockets = new CopyOnWriteArraySet<FrameChannel>();
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
         FrameChannel socket = connection.getChannel();   
         
         try {
            sockets.add(socket);
            socket.register(listener);
         } catch(Exception e) {
            e.printStackTrace();
         }         
      }

      public void distribute(Frame frame) {
         try {         
            for(FrameChannel socket : sockets) {
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
      private final TransportProcessor processor;
      private final Router negotiator;
      private final SocketProcessor server;
      
      public MessageGeneratorContainer(MessageGeneratorService service, TraceAnalyzer agent, int port) throws Exception {
         this.negotiator = new DirectRouter(service);
         this.container = new RouterContainer(this, negotiator, 10, 100000);
         this.allocator = new ArrayAllocator();
         this.processor = new ContainerTransportProcessor(container, allocator, 10);
         this.server = new TransportSocketProcessor(processor, 10, 8192*10);
         this.connection = new SocketConnection(server, agent);
         this.address = new InetSocketAddress(port);
      }
      
      public void connect() throws Exception {    
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
               
               if(!cursor.isReady()) { // wait for it to fill                  
                  Thread.sleep(1);
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

   public static class ConsoleAnalyzer extends Daemon implements TraceAnalyzer {
      
      private final Queue<TraceRecord> queue;
      private final AtomicLong count;
      private final String filter;
      
      public ConsoleAnalyzer() {
         this(null);
      }
      
      public ConsoleAnalyzer(String filter) {
         this.queue = new ConcurrentLinkedQueue<TraceRecord>();
         this.count = new AtomicLong();
         this.filter = filter;
      } 
      
      public Trace attach(SelectableChannel channel) {     
         return new TraceFeeder(channel);
      }
      
      public void run() {
         try {
            while(isActive()) {
               Thread.sleep(1000);
               
               while(!queue.isEmpty()) {
                  TraceRecord record = queue.poll();
                  
                  if(filter != null) {
                     Object event = record.event;
                     Class type = event.getClass();
                     String name = type.getName();
                     
                     if(name.contains(filter)) {
                        System.err.println(record);
                     }
                  } else {               
                     System.err.println(record);
                  }
               }        
            }
         } catch(Exception e) {
            e.printStackTrace();
         }
         
      }
      
      private class TraceFeeder implements Trace {
         
         private final SelectableChannel channel;
         private final long sequence;
         
         public TraceFeeder(SelectableChannel channel) {
            this.sequence = count.getAndIncrement();
            this.channel = channel;
         }
         
         public void trace(Object event) {
            trace(event, null);
         }

         public void trace(Object event, Object value) {
            TraceRecord record = new TraceRecord(channel, event, value, sequence);
            
            if(isActive()) {
               queue.offer(record);
            }
         }
         
      }
      
      private class TraceRecord {
         
         private final SelectableChannel channel;
         private final String thread;
         private final Object event;
         private final Object value;
         private final long sequence;
         
         public TraceRecord(SelectableChannel channel, Object event, Object value, long sequence) {
            this.thread = Thread.currentThread().getName();
            this.sequence = sequence;
            this.channel = channel;
            this.event = event;
            this.value = value;
         }
         
         public String toString() {
            StringWriter builder = new StringWriter();
            PrintWriter writer = new PrintWriter(builder);
            
            writer.print(sequence);         
            writer.print(" ["); 
            writer.print(channel);
            writer.print("]");
            writer.print(" ");
            writer.print(thread);
            writer.print(": ");
            writer.print(event);
            
            if(value != null) {
               if(value instanceof Throwable) {
                  ((Throwable)value).printStackTrace(writer);
               } else {
                  writer.print(" -> ");
                  writer.print(value);
               }
            }
            writer.close();
            return builder.toString();
         }
      }

   }
   
   
   public static void main(String[] list) throws Exception {
      ThreadDumper dumper = new ThreadDumper();
      ConsoleAnalyzer agent = new ConsoleAnalyzer();
      AtomicLong duration = new AtomicLong();
      AtomicInteger counter = new AtomicInteger();
      MessageGeneratorService service = new MessageGeneratorService();
      MessageGeneratorContainer container = new MessageGeneratorContainer(service, agent, 7070);
      MessageTimer timer = new MessageTimer(counter, duration);
      
      //agent.start();
      dumper.start();
      timer.start();
      container.connect();
      
      for(int i = 0; i < 100; i++) {
         MessageGeneratorClient client = new MessageGeneratorClient(service, counter, duration, 7070, false);
         client.start();
      }
   }
}
 