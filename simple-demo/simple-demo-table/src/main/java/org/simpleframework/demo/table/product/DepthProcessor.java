package org.simpleframework.demo.table.product;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DepthProcessor implements PriceListener {

   private final Map<String, DepthMerger> mergers; 
   private final List<DepthListener> listeners;  

   public DepthProcessor(DepthListener listener) {
      this(Arrays.asList(listener));
   }
   
   public DepthProcessor(List<DepthListener> listeners) {
      this.mergers = new ConcurrentHashMap<String, DepthMerger>();
      this.listeners = listeners;
   }

   @Override
   public void update(Price price) {
      String security = price.getSecurity();
      DepthMerger merger = merge(security);
      Depth depth = merger.merge(price);
      
      if(depth != null) {
         for(DepthListener listener : listeners) {
            listener.update(depth);
         }
      }
   }
   
   private DepthMerger merge(String security) {
      DepthMerger merger = mergers.get(security);
      
      if(merger == null) {
         merger = new DepthMerger(security);
         mergers.put(security, merger);
      }
      return merger;
   }
}
