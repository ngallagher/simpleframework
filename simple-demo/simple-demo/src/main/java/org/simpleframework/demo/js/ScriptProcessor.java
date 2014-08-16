package org.simpleframework.demo.js;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.simpleframework.demo.collection.Cache;
import org.simpleframework.demo.collection.LeastRecentlyUsedCache;

public class ScriptProcessor {

   private final Cache<File, ScriptResult> cache;
   private final ScriptCompiler compiler;
   private final File base;

   public ScriptProcessor(ScriptCompiler compiler, File base) {
      this(compiler, base, 20);
   }
   
   public ScriptProcessor(ScriptCompiler compiler, File base, int capacity) {
      this.cache = new LeastRecentlyUsedCache<File, ScriptResult>(capacity);
      this.compiler = compiler;
      this.base = base;
   }
   
   public byte[] process(String file) throws IOException  {
      File script = new File(base, file);     
      
      if(!script.exists()) {
         throw new FileNotFoundException("Script file '" + script + "' could not be found");
      }
      ScriptResult result = compile(script);
      String message = result.getMessage();
      
      if(!result.isSuccess()) {
         throw new IOException("Could not compile script '" + file + "' " + message);
      }
      return result.getData();        
   }
   
   private ScriptResult compile(File script) throws IOException {
      ScriptResult current = cache.fetch(script);
      
      if(current != null) {
         long compileTime = current.getTimeStamp();
         long modificationTime = script.lastModified();
         
         if(compileTime > modificationTime) {
            return current;
         }
      }
      ScriptResult update = compiler.compile(script);
      
      if(update != null) {
         cache.cache(script, update);
      }
      return update;      
   }
}
