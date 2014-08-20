package org.simpleframework.demo.table.service;

public enum ChangeType {
   SCHEMA("S"),
   TABLE("T");
   
   public final String code;
   
   private ChangeType(String code) {
      this.code = code;
   }
   
   public String getCode() {
      return code;
   }
}
