package org.simpleframework.http.socket.table;

public class WebSocketTableCell {

   private final long timeStamp;
   private final String column;
   private final String value;
   
   public WebSocketTableCell(String column, String value) {
      this.timeStamp = System.currentTimeMillis();
      this.value = value;
      this.column = column;
   }
   
   public long getTimeStamp(){
      return timeStamp;
   }
   
   public String getValue(){
      return value;
   }
   
   public String getColumn(){
      return column;
   }
}
