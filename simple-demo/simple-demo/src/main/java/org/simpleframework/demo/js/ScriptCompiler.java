package org.simpleframework.demo.js;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;

public class ScriptCompiler {
   
   private final ScriptCompressor compressor;
   private final CompilationLevel level;
   
   public ScriptCompiler() {
      this(null);
   }
   
   public ScriptCompiler(CompilationLevel level) {
      this.compressor = new ScriptCompressor();
      this.level = level;
   }
   
   public ScriptResult compile(File file) throws IOException {
      CompilerOptions options = new CompilerOptions();
      Compiler compiler = new Compiler();
      
      if(level != null) {
         level.setOptionsForCompilationLevel(options);
      }
      SourceFile source = SourceFile.fromFile(file);
      List<SourceFile> externs = Collections.emptyList();
      List<SourceFile> resources = Collections.singletonList(source);         
      Result result = compiler.compile(externs, resources, options);
      String script = compiler.toSource();
      byte[] data = compressor.compress(script);
      
      return new ScriptResult(result, script, data);
   }

   
   
}
