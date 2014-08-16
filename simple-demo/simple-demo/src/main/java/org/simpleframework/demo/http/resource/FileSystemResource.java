package org.simpleframework.demo.http.resource;

import static org.simpleframework.http.Protocol.CONTENT_TYPE;
import static org.simpleframework.http.Status.OK;

import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import org.simpleframework.demo.io.FileResolver;
import org.simpleframework.http.Path;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

public class FileSystemResource implements Resource {

   private final ContentTypeResolver typeResolver;
   private final FileResolver fileResolver;
   private final Status status;

   public FileSystemResource(FileResolver fileResolver, ContentTypeResolver typeResolver) {
      this(fileResolver, typeResolver, OK);
   }

   public FileSystemResource(FileResolver fileResolver, ContentTypeResolver typeResolver, Status status) {
      this.fileResolver = fileResolver;
      this.typeResolver = typeResolver;
      this.status = status;
   }

   @Override
   public void handle(Request request, Response response) throws Exception {
      Path path = request.getPath();
      String target = path.getPath();
      String match = target.toLowerCase();
      String type = typeResolver.resolveType(match);
      FileChannel channel = fileResolver.resolveChannel(target);
      WritableByteChannel output = response.getByteChannel();
      long length = channel.size();

      response.setStatus(status);
      response.setValue(CONTENT_TYPE, type);
      response.setContentLength(length);
      channel.transferTo(0, length, output);
      channel.close();
      output.close();
   }
}
