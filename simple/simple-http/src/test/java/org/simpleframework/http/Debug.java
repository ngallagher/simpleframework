package org.simpleframework.http;

public class Debug {
   public void log(String text, Object... list) {
      System.out.printf(text, list);
   }

   public void logln(String text, Object... list) {
      System.out.printf(text + "%n", list);
   }
}
