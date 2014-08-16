package org.simpleframework.demo.table.product;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DepthProcessor implements PriceListener {

   private final Map<String, DepthMerger> mergers; 
   private final DepthListener listener;  
   
   public DepthProcessor(DepthListener listener) {
      this.mergers = new ConcurrentHashMap<String, DepthMerger>();
      this.listener = listener;
   }

   @Override
   public void update(Price price) {
      String security = price.getSecurity();
      DepthMerger merger = merge(security);
      Depth depth = merger.merge(price);
      
      if(depth != null) {
         listener.update(depth);
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
