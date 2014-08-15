package org.simpleframework.demo.log4j;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

public class LogConfigurer {

   private final long refreshInterval;
   private final File logSettings;

   public LogConfigurer(File logSettings) {
      this(logSettings, 5000);
   }

   public LogConfigurer(File logSettings, long refreshInterval) {
      this.refreshInterval = refreshInterval;
      this.logSettings = logSettings;
   }
   
   public void configure() throws IOException {
      String canonicalPath = logSettings.getCanonicalPath();

      if (canonicalPath.toLowerCase().endsWith(".xml")) {
         DOMConfigurator.configureAndWatch(canonicalPath, refreshInterval);
      } else {
         PropertyConfigurator.configureAndWatch(canonicalPath, refreshInterval);
      }
   }
}
