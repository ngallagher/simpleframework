package org.simpleframework.demo.http.resource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContentTypeResolver {

   private final Map<String, String> expressions;
   private final Map<Pattern, String> patterns;

   public ContentTypeResolver(Map<String, String> expressions) {
      this.patterns = new LinkedHashMap<Pattern, String>();
      this.expressions = expressions;
   }

   public synchronized String resolveType(String path) {
      if (patterns.isEmpty()) {
         Set<String> keys = expressions.keySet();

         for (String expression : keys) {
            Pattern pattern = Pattern.compile(expression);
            String type = expressions.get(expression);

            patterns.put(pattern, type);
         }
      }
      return resolvePattern(path);
   }

   private synchronized String resolvePattern(String path) {
      Set<Pattern> keys = patterns.keySet();

      for (Pattern pattern : keys) {
         Matcher matcher = pattern.matcher(path);

         if (matcher.matches()) {
            return patterns.get(pattern);
         }
      }
      return "application/octetstream";
   }
}
