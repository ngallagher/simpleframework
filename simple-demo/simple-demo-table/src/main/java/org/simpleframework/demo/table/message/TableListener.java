package org.simpleframework.demo.table.message;

import org.simpleframework.demo.table.telemetry.PerformanceAnalyzer;
import org.simpleframework.http.socket.Frame;
import org.simpleframework.http.socket.FrameListener;
import org.simpleframework.http.socket.FrameType;
import org.simpleframework.http.socket.Reason;
import org.simpleframework.http.socket.Session;

public class TableListener implements FrameListener {
   
   private final PerformanceAnalyzer service;
   private final TableUpdater updater;
   
   public TableListener(TableUpdater updater, PerformanceAnalyzer service) {
      this.service = service;
      this.updater = updater;
   }

   public void onFrame(Session socket, Frame frame) {      
      FrameType type = frame.getType();
      
      if(type != FrameType.PONG && type != FrameType.PING) {
        
         if(type == FrameType.TEXT) {
            String text = frame.getText();
            String[] command = text.split(":");
            String operation = command[0];
            String parameters = command[1];
            String[] values = parameters.split(",");
            
            if(values.length > 0) {
               String user = null;
               long roundTripTime = 0;
               long paintTime = 0;
               long rowChanges = 0;
               
               for(String value : values) {
                  String[] pair = value.split("=");
                  
                  if(operation.equals("refresh")) {
                     updater.refresh();
                  }else if(operation.equals("status")) {
                     if(pair.length > 1) {
                        if(pair[0].equals("sequence")) {
                           if(pair[1].indexOf("@") != -1) {
                              String time = pair[1].split("@")[1];
                              long sent = Long.parseLong(time);
                              roundTripTime = (System.currentTimeMillis() - sent);                           
                           }                        
                        }
                        if(pair[0].equals("duration")) {
                           paintTime = Long.parseLong(pair[1]);                        
                        }
                        if(pair[0].equals("change")) {
                           rowChanges = Long.parseLong(pair[1]);                        
                        }
                        if(pair[0].equals("user")) {
                           user = pair[1];                        
                        }                    
                     }
                  }
               }
               if(text != null && text.startsWith("status:")) {
                  service.update(user, paintTime, roundTripTime, rowChanges);
               }
            }
         }
        // System.err.println("onFrame(");
        // System.err.println(frame.getText());
       //  System.err.println(")");         
      }
   }

   public void onError(Session socket, Exception cause) {
      System.err.println("onError(");
      //cause.printStackTrace();
      System.err.println(")");
   }

   public void onOpen(Session socket) {
      System.err.println("onOpen(" + socket +")");
   }

   public void onClose(Session session, Reason reason) {
      System.err.println("onClose(" + reason +" reason="+reason.getText()+" code="+reason.getCode()+")"+Thread.currentThread().getName());
   }
}
