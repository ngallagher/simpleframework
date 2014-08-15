package org.simpleframework.http.socket.table;

public enum WebSocketTableUpdateType {
   SCHEMA('S'),
   HIGHLIGHT('H'),
   DELTA('D');
   
   public final char code;
   
   private WebSocketTableUpdateType(char code) {
      this.code = code;
   }
   
   public char getCode() {
      return code;
   }
}
