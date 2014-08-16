package org.simpleframework.demo.template.velocity;

import java.io.File;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.simpleframework.demo.io.FileResolver;
import org.simpleframework.demo.template.TemplateContext;
import org.simpleframework.demo.template.TemplateEngine;

public class VelocityTemplateEngine implements TemplateEngine {

   private final VelocityEngine engine;
   private final FileResolver resolver;
   private final String suffix;
   private final String prefix;

   public VelocityTemplateEngine(FileResolver resolver, String prefix, String suffix) {
      this(resolver, new Properties(), prefix, suffix);
   }

   public VelocityTemplateEngine(FileResolver resolver, Properties properties, String prefix, String suffix) {
      this.engine = new VelocityEngine();
      this.resolver = resolver;
      this.suffix = suffix;
      this.prefix = prefix;
      this.engine.init(properties);
   }

   @Override
   public String renderTemplate(TemplateContext context, String path) throws Exception {
      VelocityContext internal = createContext(context);
      Reader source = resolveTemplate(path);

      if (source != null) {
         StringWriter writer = new StringWriter();

         try {
            engine.evaluate(internal, writer, path, source);
         } finally {
            source.close();
         }
         return writer.toString();
      }
      return null;
   }   

   private VelocityContext createContext(TemplateContext context) throws Exception {
      VelocityContext internal = new VelocityContext();

      if (context != null) {
         Set<String> keys = context.keySet();

         for (String key : keys) {
            Object value = context.get(key);

            if (value != null) {
               internal.put(key, value);
            } 
         }
      }
      return internal;
   }   

   @Override
   public boolean validTemplate(String path) throws Exception {
      File file = resolveFile(path);

      if (file.exists()) {
         return !file.isDirectory();
      }
      return false;
   }
   
   private Reader resolveTemplate(String path) throws Exception {
      String realPath = resolvePath(path);
 
      if (realPath != null) {
         return resolver.resolveReader(realPath);
      }
      return null;
   }

   private File resolveFile(String path) throws Exception {
      String realPath = resolvePath(path);

      if (realPath != null) {
         return resolver.resolveFile(realPath);
      }
      return null;
   }

   private String resolvePath(String path) throws Exception {
      if (prefix != null) {
         if (path.startsWith("/")) {
            path = path.substring(1);
         }
         if (!path.startsWith(prefix)) {
            path = prefix + path;
         }
      }
      if (suffix != null) {
         if (!path.endsWith(suffix)) {
            path = path + suffix;
         }
      }
      return path;
   }
}
