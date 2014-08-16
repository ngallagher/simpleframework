package org.simpleframework.demo.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;


public class FileResolver {

   private final FileManager manager;
   private final String missing;
   private final String index;

   public FileResolver(FileManager manager, String index) {
      this(manager, index, null);
   }

   public FileResolver(FileManager manager, String index, String missing) {
      this.missing = missing;
      this.manager = manager;
      this.index = index;
   }

   public File resolveFile(String path) throws IOException {
      if (path.equals("/") || path.endsWith("/")) {
         return manager.createFile(index);
      }
      File file = manager.createFile(path); 

      if (missing != null && !file.exists()) {
         return manager.createFile(missing);
      }
      return file;
   }

   public FileReader resolveReader(String path) throws IOException {
      File file = resolveFile(path);

      if (file == null || !file.exists()) {
         throw new FileNotFoundException("Path '" + path + "' resolved to file '" + file + "' which does not exist");
      }
      return manager.openReader(file);
   }

   public FileInputStream resolveStream(String path) throws IOException {
      File file = resolveFile(path);

      if (file == null || !file.exists()) {
         throw new FileNotFoundException("Path '" + path + "' resolved to file '" + file + "' which does not exist");
      }
      return manager.openInputStream(file);
   }

   public FileChannel resolveChannel(String path) throws IOException {
      File file = resolveFile(path);

      if (file == null || !file.exists()) {
         throw new FileNotFoundException("Path '" + path + "' resolved to file '" + file + "' which does not exist");
      }
      return manager.openInputChannel(file);
   }
}
