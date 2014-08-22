package org.simpleframework.demo.jmx;

public enum WebColor {
   LOCAL("local", "#ffffff"), // white
   DEV("dev", "#bdecb6"), // green
   DEMO("demo", "#ffff66"), // yellow
   UAT("uat", "#87cefa"), // blue
   SIM("sim", "#cdcdc1"), // grey
   PROD("prod", "#ffc0cb"), // red
   WWW("www", "#ffffff"); // white

   public final String color;
   public final String name;

   private WebColor(String name, String color) {
      this.color = color;
      this.name = name;
   }

   public static String resolveColor(String token) {
      if (token != null) {
         for (WebColor color : values()) {
            if (color.name.equalsIgnoreCase(token)) {
               return color.color;
            }
         }
      }
      return token;
   }
}
