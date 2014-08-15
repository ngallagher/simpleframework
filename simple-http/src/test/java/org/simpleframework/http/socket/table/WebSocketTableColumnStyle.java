package org.simpleframework.http.socket.table;

public class WebSocketTableColumnStyle {

   private final String template;
   private final String caption;
   private final String name;
   private final boolean sortable;
   private final boolean resizable;
   
   public WebSocketTableColumnStyle(String name, String caption, String template, boolean resizable, boolean sortable) {
      this.name = name;
      this.caption = caption;
      this.template = template;
      this.resizable = resizable;
      this.sortable = sortable;
   }
   
   public String createStyle() {
      StringBuilder builder = new StringBuilder();
      WebSocketValueEncoder encoder = new WebSocketValueEncoder();
      
      builder.append(name);
      builder.append(",");
      builder.append(encoder.encode(caption));
      builder.append(",");      
      builder.append(encoder.encode(template));
      builder.append(",");
      builder.append(resizable);
      builder.append(",");
      builder.append(sortable);
      
      return builder.toString();
   }
}
