package org.simpleframework.demo.jmx;

public class WebConfiguration {

   private final String color;
   private final String password;
   private final String login;
   private final int port;

   public WebConfiguration(String color, String login, String password, int port) {
      this.color = WebColor.resolveColor(color);
      this.password = password;
      this.login = login;
      this.port = port;
   }

   public String getColor() {
      return color;
   }

   public String getPassword() {
      return password;
   }

   public String getLogin() {
      return login;
   }

   public int getPort() {
      return port;
   }
}
