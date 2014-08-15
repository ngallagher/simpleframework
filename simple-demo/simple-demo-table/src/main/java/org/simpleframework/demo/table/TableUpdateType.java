package org.simpleframework.demo.table;

public enum TableUpdateType {
   SCHEMA('S'),
   HIGHLIGHT('H'),
   DELTA('D');
   
   public final char code;
   
   private TableUpdateType(char code) {
      this.code = code;
   }
   
   public char getCode() {
      return code;
   }
}
