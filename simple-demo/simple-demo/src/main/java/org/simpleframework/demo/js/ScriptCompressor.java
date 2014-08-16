package org.simpleframework.demo.js;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.zip.GZIPOutputStream;

public class ScriptCompressor {

   private final int capacity;

   public ScriptCompressor() {
      this(8192);
   }
   
   public ScriptCompressor(int capacity) {
      this.capacity = capacity;
   }
   
   public byte[] compress(String script) throws IOException {
      if(script != null) {
         ByteArrayOutputStream buffer = new ByteArrayOutputStream();
         GZIPOutputStream compressor = new GZIPOutputStream(buffer, capacity);
         PrintStream output = new PrintStream(compressor);
         
         output.print(script);
         output.close();
         
         return buffer.toByteArray();
      }
      return null;
   }
}
