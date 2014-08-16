package org.simpleframework.demo.http.resource;

import static org.simpleframework.http.Protocol.CONTENT_TYPE;
import static org.simpleframework.http.Status.OK;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import org.simpleframework.demo.io.FileManager;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;

public class FileResource implements Resource {

   private final FileManager manager;
   private final Status status;
   private final String type;
   private final File file;

   public FileResource(FileManager manager, File file, String type) {
      this(manager, file, type, OK);
   }

   public FileResource(FileManager manager, File file, String type, Status status) {
      this.manager = manager;
      this.status = status;
      this.type = type;
      this.file = file;
   }

   @Override
   public void handle(Request request, Response response) throws IOException {
      WritableByteChannel output = response.getByteChannel();
      FileChannel channel = manager.openInputChannel(file);
      long length = file.length();

      response.setCode(status.code);
      response.setDescription(status.description);
      response.setValue(CONTENT_TYPE, type);
      response.setContentLength(length);
      channel.transferTo(0, length, output);
      channel.close();
      output.close();
   }
}
