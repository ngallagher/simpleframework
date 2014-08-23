package org.simpleframework.http.validate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.simpleframework.common.buffer.Allocator;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.common.buffer.FileAllocator;

public class Client {
   
   private static final AtomicInteger SEQUENCE = new AtomicInteger();
   private final ExecutorService executor;
   private final Measurement measurement;
   private final Test test;
   private final File configDir;
   private final boolean debug;
   private final int pipeline;
   private final int timeout;
   
   public Client(ExecutorService executor, Measurement measurement, File configDir, Test test, int count, int timeout, int pipeline, boolean debug) throws IOException {
      this.executor = executor;
      this.measurement = measurement;
      this.configDir = configDir;
      this.pipeline = pipeline;
      this.timeout = timeout;
      this.debug = debug;
      this.test = test;
   }
   
   public Buffer execute(Socket socket, byte[] request, double throttle) throws Exception {
      Allocator allocator = new FileAllocator();
      Buffer response = allocator.allocate();
      SendTask sender = new SendTask(socket, request);
      InputStream in = socket.getInputStream();
      byte[] buffer = new byte[2048];
      int byteCount = 0;
      int count = 0;
      
      if(debug) {
    	  System.out.println("New connection ["+socket.getLocalPort()+"]");
      }
      measurement.threadRunning();
      measurement.connectionEstablished();
      socket.setSoTimeout(timeout);
      executor.execute(sender);
      
      try { 
         while((count = in.read(buffer)) != -1){
            if(throttle > 0) {
               double random = Math.random() * 1000; // randomize throttle
               double delay = random % throttle;
               
               if(delay > 0) {
                  Thread.sleep((int)delay);
               }
            }
            byteCount += count; // bytes transferred
            
            if(debug) {
            	//System.out.println("Read ["+count+"] bytes from connected socket ["+socket.getLocalPort()+"]");
            }
            response.append(buffer, 0, count);
         }  
      } catch(Exception e) {
         e.printStackTrace();
         test.investigate(response, configDir);
         measurement.errorOccured();
      } finally {
    	  measurement.receivedResponse(pipeline);
    	  measurement.bytesTransferred(byteCount);
    	  measurement.connectionTerminated();
    	  measurement.threadWaiting();
        socket.close();
      }
      if(debug) {
         File outputFile = new File(configDir, "out-"+SEQUENCE.getAndIncrement());
         OutputStream debugFile = new FileOutputStream(outputFile);
         InputStream responseContent = response.open();
      
         debugFile.write(String.valueOf(count).getBytes());
         debugFile.write(13);
         debugFile.write(10);
         debugFile.write(request);
         
         while((count = responseContent.read(buffer)) != -1) {
            debugFile.write(buffer, 0, count);
         }
         debugFile.flush();
         debugFile.close();
      }
      return response;
   }
   
   private class SendTask implements Runnable {
      
      private final Socket socket;
      private final byte[] request;
      
      public SendTask(Socket socket, byte[] request) {
         this.socket = socket;
         this.request = request;  
      }     
      
      public void run() {
         try {
            OutputStream out = socket.getOutputStream();  
            int block = 1024;
            int count = 0;
            
            while(count < request.length) {
            	int size = Math.min(block, request.length - count);
            	
            	if(debug) {
            		System.out.println("Write write("+request.length+", "+count+", "+size+") to connected socket ["+socket.getLocalPort()+"]");
            	}
            	if(size > 0) {
            		out.write(request, count, size);
            	}
            	count += size;
            };                       
            measurement.sentRequest(pipeline);
         } catch(IOException e) {
            e.printStackTrace();
            measurement.errorOccured();
         }
      }
   }
}
