package org.simpleframework.demo.table.js;

import static org.simpleframework.http.Protocol.CONTENT_TYPE;
import static org.simpleframework.http.Status.OK;

import java.io.InputStream;
import java.io.PrintStream;

import org.simpleframework.demo.http.resource.ContentTypeResolver;
import org.simpleframework.demo.http.resource.FileResolver;
import org.simpleframework.demo.http.resource.Resource;
import org.simpleframework.http.Path;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;

public class CompilerResource implements Resource {
   
   private final ContentTypeResolver typeResolver;
   private final FileResolver fileResolver;
   private final Status status;

   public CompilerResource(FileResolver fileResolver, ContentTypeResolver typeResolver) {
      this(fileResolver, typeResolver, OK);
   }

   public CompilerResource(FileResolver fileResolver, ContentTypeResolver typeResolver, Status status) {
      this.fileResolver = fileResolver;
      this.typeResolver = typeResolver;
      this.status = status;
   }

   @Override
   public void handle(Request request, Response response) throws Throwable {
      Path path = request.getPath();
      String target = path.getPath();
      String match = target.toLowerCase();
      String type = typeResolver.resolveType(match);
      InputStream stream = fileResolver.resolveStream(target);
      SourceFile source = SourceFile.fromInputStream(target, stream);
      SourceFile extern = SourceFile.fromCode("/blah", "");      
      CompilerOptions options = new CompilerOptions();
      Compiler compiler = new Compiler();
      Result result = compiler.compile(extern, source, options);
      PrintStream out = response.getPrintStream();
      String value = compiler.toSource();

      response.setCode(status.code);
      response.setDescription(status.description);
      response.setValue(CONTENT_TYPE, type);
      out.print(value);
      out.close();      
   }

}
