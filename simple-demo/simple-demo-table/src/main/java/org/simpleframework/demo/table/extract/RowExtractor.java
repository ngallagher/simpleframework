package org.simpleframework.demo.table.extract;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class RowExtractor {
   
   private final Map<String, CellExtractor> extractors;
   
   public RowExtractor(Map<String, CellExtractor> extractors) {
      this.extractors = extractors;
   }
   
   public Map<String, Object> extract(Object value) {
      Map<String, Object> row = new LinkedHashMap<String, Object>();
      
      if(!extractors.isEmpty()) {
         Set<String> names = extractors.keySet();
         
         for(String name : names) {
            CellExtractor extractor = extractors.get(name);
            Object cell = extractor.extract(value);
            
            row.put(name, cell);
         }
      }
      return row;
   }
}
