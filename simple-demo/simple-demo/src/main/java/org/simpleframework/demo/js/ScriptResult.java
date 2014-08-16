package org.simpleframework.demo.js;

import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.Result;

public class ScriptResult {
   
   private final Result result;
   private final String source;
   private final byte[] data;
   private final long time;

   public ScriptResult(Result result, String source, byte[] data) {
      this.time = System.currentTimeMillis();
      this.result = result;
      this.source = source;
      this.data = data;
   }
   
   public boolean isSuccess() {
      return result.success;
   }
   
   public long getTimeStamp() {
      return time;
   }
   
   public byte[] getData() {
      return data;
   }
   
   public String getSource() {
      return source;
   }
   
   public String getMessage() {
      StringBuilder builder = new StringBuilder();
      
      for(JSError error : result.errors) {
         builder.append(error.description);
      }
      return builder.toString();
   }
}
