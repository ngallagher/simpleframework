/*
 * FileManager.java February 2008
 *
 * Copyright (C) 2008, Niall Gallagher <niallg@users.sf.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package org.simpleframework.util.buffer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.simpleframework.util.thread.Daemon;

/**
 * The <code>FileManager</code> object is used to create files that
 * are to be used for file buffers. All files created by this are
 * created in the <code>java.io.tmpdir</code> path. Temporary files
 * created in this directory last for five minutes before being
 * deleted. This ensures that if the server is running for a long
 * period of time the file system is not exhausted.
 * 
 * @author Niall Gallagher
 */
class FileManager extends Daemon implements FileFilter {
   
   /**
    * This is the prefix for the temporary files created.
    */
   private final String prefix;
   
   /**
    * This is the duration the files created will exist for.
    */
   private final long duration;
   
   /**
    * Constructor for the <code>FileManager</code> object. This will
    * create a thread that runs every five minutes and cleans up
    * files that have been created for buffers. Due to the period
    * of time polled, files could exist for up to ten minutes.
    * 
    * @param prefix this is the file name prefix for the files
    */
   public FileManager(String prefix) {
      this(prefix, 300000);  
   }
   
   /**
    * Constructor for the <code>FileManager</code> object. This will
    * create a thread that runs every five minutes and cleans up
    * files that have been created for buffers. Due to the period
    * of time polled, files could exist for up to ten minutes.
    * 
    * @param prefix this is the file name prefix for the files
    * @param duration this is the duration the files exist for
    */
   public FileManager(String prefix, long duration) {
      this.duration = duration;
      this.prefix = prefix;
      this.start();
   }
   
   /**
    * This will create a temporary file which can be used as a buffer
    * for <code>FileBuffer</code> objects. The file returned by this
    * method will be created before it is returned, which ensures it
    * can be used as a means to buffer bytes. All files are created
    * in the <code>java.io.tmpdir</code> location, which represents
    * the underlying file system temporary file destination.
    * 
    * @return this returns a created temporary file for buffers
    */
   public File create() throws IOException {
      return create(prefix);
   }
   
   /**
    * This will create a temporary file which can be used as a buffer
    * for <code>FileBuffer</code> objects. The file returned by this
    * method will be created before it is returned, which ensures it
    * can be used as a means to buffer bytes. All files are created
    * in the <code>java.io.tmpdir</code> location, which represents
    * the underlying file system temporary file destination.
    * 
    * @param prefix this is the prefix of the file to be created
    * 
    * @return this returns a created temporary file for buffers
    */
   private File create(String prefix) throws IOException {
      File file = File.createTempFile(prefix, null);
      
      if(!file.exists()) {
         file.createNewFile();
      }
      return file;
   }
   
   /**
    * This is the run method that will periodically poll the file
    * file system for temporary buffer files. If files matching the
    * pattern are found and have not been modified in the duration
    * period of time then they will be deleted to ensure that the
    * file system is not exhausted during a long server execution.
    */
   public void run() {
      while(isActive()) {
         try {
            Thread.sleep(duration);
            clean();
         } catch(Exception e) {
            continue;
         }
      }
   }
   
   /**
    * When this method is invoked the files that match the pattern
    * of the temporary files are evaluated for deletion. Only those
    * files that have not been modified in the duration period can
    * be deleted. This ensures the file system is not exhausted.
    */
   private void clean() throws IOException {
      File path = create();
      
      if(!path.isDirectory()) {
         path = path.getParentFile();
      }
      clean(path);  
   }
   
   /**
    * When this method is invoked the files that match the pattern
    * of the temporary files are evaluated for deletion. Only those
    * files that have not been modified in the duration period can
    * be deleted. This ensures the file system is not exhausted.
    * 
    * @param path this is the path of the file to be evaluated
    */
   private void clean(File path) throws IOException {
      File[] list = path.listFiles(this);
      
      for(File next : list) {
         next.delete();
      }
   }
   
   /**
    * This determines if the file provided is an acceptable file for
    * deletion. Acceptable files are those that match the pattern
    * of files created by this file system object. If the file is
    * a matching file then it is a candidate for deletion.
    * 
    * @param file this is the file to evaluate for deletion
    * 
    * @return this returns true if the file matches the pattern
    */
   public boolean accept(File file) {
      String name = file.getName();
  
      if(file.isDirectory()) {
         return false;
      }
      return accept(file, name);
   }
   
   /**
    * This determines if the file provided is an acceptable file for
    * deletion. Acceptable files are those that match the pattern
    * of files created by this file system object. If the file is
    * a matching file then it is a candidate for deletion.
    * 
    * @param file this is the file to evaluate for deletion
    * @param name this is the name of the file to be evaluated
    * 
    * @return this returns true if the file matches the pattern
    */
   private boolean accept(File file, String name) {
      long time = System.currentTimeMillis();
      long modified = file.lastModified();
      
      if(modified + duration > time) { // not yet expired
         return false;
      }
      return name.startsWith(prefix);
   }
   
   /**
    * This method is used to close the allocator so that resources
    * that are occupied by the allocator can be freed. This will
    * allow the allocator to be created and closed repeatedly in
    * a single process without holding on to resources such as
    * mapped file buffers or threads.
    */
   public void close() throws IOException {
      if(isActive()) {
         stop();
         interrupt();
         clean();
      }
   }
}
