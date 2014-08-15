package org.simpleframework.demo.spring;

import static org.springframework.beans.factory.config.PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_FALLBACK;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.Resource;

public class ConfigurationLoader {

   private final FileSystemXmlApplicationContext appContext;
   private final Resource[] propertyFiles;

   public ConfigurationLoader(Resource configFile, Resource... propertyFiles) throws IOException {
      this(configFile.getFile(), propertyFiles);
   }

   public ConfigurationLoader(File configFile, Resource... propertyFiles) throws IOException {
      this(configFile.getPath(), propertyFiles);
   }

   public ConfigurationLoader(String configFile, Resource... propertyFiles) throws IOException {
      this.appContext = new FileSystemXmlApplicationContext(new String[] { configFile }, false);
      this.propertyFiles = propertyFiles;
   }

   public void start() throws Exception {
      PropertyPlaceholderConfigurer propertyConfigurer = new PropertyPlaceholderConfigurer();
      propertyConfigurer.setLocations(propertyFiles);
      propertyConfigurer.setSystemPropertiesMode(SYSTEM_PROPERTIES_MODE_FALLBACK);
      appContext.addBeanFactoryPostProcessor(propertyConfigurer);
      appContext.refresh();
   }

   public <T> T get(Class<T> type) {
      return appContext.getBean(type);
   }
   
   public ApplicationContext getAppContext() {
      return appContext;
   }
   
}
