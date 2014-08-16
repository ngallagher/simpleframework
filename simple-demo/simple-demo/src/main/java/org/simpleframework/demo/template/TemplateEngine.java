package org.simpleframework.demo.template;

public interface TemplateEngine {
   String renderTemplate(TemplateContext context, String path) throws Exception;
   boolean validTemplate(String path) throws Exception;
}
