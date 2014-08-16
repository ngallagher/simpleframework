package org.simpleframework.demo.table.message;

import java.io.IOException;
import java.util.Arrays;

import org.simpleframework.demo.table.extract.Client;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameListener;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.http.socket.WebSocket;

public class TableTester extends Thread implements WebSocket {
   
   private final TableUpdater updater;
   
   public TableTester(TableUpdater updater) {
      this.updater = updater;
   }
   
   public void run() {
      try {
         updater.subscribe(this, new Client("john@hsbc.com", "HSBC", Arrays.asList("DB", "ANZ", "HSBC")));
      } catch(Exception e){
         e.printStackTrace();
      }
   }

   @Override
   public void send(byte[] data) throws IOException {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void send(String text) throws IOException {
         System.err.println(text);
   }

   @Override
   public void send(Frame frame) throws IOException {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void register(FrameListener listener) throws IOException {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void remove(FrameListener listener) throws IOException {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void close(Reason reason) throws IOException {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void close() throws IOException {
      // TODO Auto-generated method stub
      
   }

}
