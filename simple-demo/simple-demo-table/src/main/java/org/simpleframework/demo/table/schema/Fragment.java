package org.simpleframework.demo.table.schema;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReference;

public class Fragment {
   
   private final AtomicReference<String> cache;
   private final File file;

   public Fragment(File file) {
      this.cache = new AtomicReference<String>();
      this.file = file;
   }
   
   public String getFragment() {
      String fragment = cache.get();
      
      if(fragment == null) {
         try {
            InputStream stream = new FileInputStream(file);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[1024];
            int count = 0;
            
            while((count = stream.read(chunk)) != -1) {
               buffer.write(chunk, 0, count);
            }
            fragment = buffer.toString();         
            cache.set(fragment);
            stream.close();
         } catch(IOException e) {
            throw new IllegalStateException("Could not load fragment '" + file + "'", e);
         }
      }
      return fragment;
   }
}
