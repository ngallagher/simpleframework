package org.simpleframework.demo.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileManager {

   private final File base;

   private FileManager(File base) {
      this.base = base;
   }

   public File createFile(String path) throws IOException {
      String normal = path.replace('/', File.separatorChar);
      String trimmed = normal.trim();

      return new File(base, trimmed);
   }

   public FileInputStream openInputStream(String path) throws IOException {
      File file = createFile(path);

      if (!file.exists()) {
         throw new FileNotFoundException("Could not find file " + file + " from " + path);
      }
      return openInputStream(file);
   }

   public FileInputStream openInputStream(File file) throws IOException {
      return new FileInputStream(file);
   }

   public FileReader openReader(String path) throws IOException {
      File file = createFile(path);

      if (!file.exists()) {
         throw new FileNotFoundException("Could not find file " + file + " from " + path);
      }
      return openReader(file);
   }

   public FileReader openReader(File file) throws IOException {
      return new FileReader(file);
   }

   public FileChannel openInputChannel(String path) throws IOException {
      File file = createFile(path);

      if (!file.exists()) {
         throw new FileNotFoundException("Could not find file " + file + " from " + path);
      }
      return openInputChannel(file);
   }

   public FileChannel openInputChannel(File file) throws IOException {
      return openInputStream(file).getChannel();
   }

   public FileOutputStream openOutputStream(String path) throws IOException {
      File file = createFile(path);
      File directory = file.getParentFile();

      if (!directory.exists()) {
         directory.mkdirs();
      }
      return openOutputStream(file);
   }

   public FileOutputStream openOutputStream(File file) throws IOException {
      File directory = file.getParentFile();

      if (!directory.exists()) {
         directory.mkdirs();
      }
      return new FileOutputStream(file);
   }

   public FileChannel openOutputChannel(String path) throws IOException {
      File file = createFile(path);
      File directory = file.getParentFile();

      if (!directory.exists()) {
         directory.mkdirs();
      }
      return openOutputChannel(file);
   }

   public FileChannel openOutputChannel(File file) throws IOException {
      return openOutputStream(file).getChannel();
   }
}
