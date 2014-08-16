package org.simpleframework.demo.template;

public class Template {
   
   private final TemplateEngine engine;
   private final String path;
   
   public Template(TemplateEngine engine, String path) {
      this.engine = engine;
      this.path = path;
   }
   
   public String render(TemplateContext context) throws Exception {
      return engine.renderTemplate(context, path);
   }
   
   public boolean exists() throws Exception {
      return engine.validTemplate(path);
   }
}
