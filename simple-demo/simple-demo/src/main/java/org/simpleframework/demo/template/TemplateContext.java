package org.simpleframework.demo.template;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TemplateContext {
   
   private final Map<String, Object> context;
   
   public TemplateContext() {
      this(Collections.EMPTY_MAP);
   }
   
   public TemplateContext(Map<String, Object> values) {
      this.context = new HashMap<String, Object>(values);
   }

   public Set<String> keySet() {
      return context.keySet();
   }

   public void put(String name, Object value) {
      context.put(name, value);
   }

   public Object get(String name) {
      return context.get(name);
   }
}
