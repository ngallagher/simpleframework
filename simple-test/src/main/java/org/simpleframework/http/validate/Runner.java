package org.simpleframework.http.validate;

import java.io.File;

import org.simpleframework.xml.core.Persister;

public class Runner {
   
   public static void main(String[] list) throws Exception {
      if(list.length == 0) {
         throw new IllegalArgumentException("Must specify the test XML file");
      }
      ThreadDumper dumper = new ThreadDumper(10000);
      File configFile = new File(list[0]);
      File configDir = configFile.getParentFile();
      Persister persister = new Persister();
      Test test = persister.read(Test.class, configFile);
      
      dumper.start();
      test.execute(configDir);
   }
}
