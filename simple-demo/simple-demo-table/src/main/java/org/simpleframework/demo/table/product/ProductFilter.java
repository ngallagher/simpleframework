package org.simpleframework.demo.table.product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simpleframework.demo.table.Query;

public class ProductFilter {

   private final Query client;
   
   public ProductFilter(Query client) {
      this.client = client;
   }
   
   public Product filterDepth(Depth depth) {
      String security = depth.getSecurity();
      List<String> products = client.getProducts();
      
      if(products.isEmpty() || products.contains(security)) {
         String company = client.getCompany();
         List<String> inclusive = client.getCompanies();
         List<String> exclusive = Collections.singletonList(company);
         Depth filteredDepth = filterDepth(depth, inclusive);
         Depth companyDepth = filterDepth(depth, exclusive);
         long version = depth.getVersion();
         
         return new Product(security, depth, filteredDepth, companyDepth, version);
      }
      return null;
   }
   
   private Depth filterDepth(Depth depth, List<String> include) {
      Map<PriceType, PriceSeries> bidPrices = new HashMap<PriceType, PriceSeries>();
      Map<PriceType, PriceSeries> offerPrices = new HashMap<PriceType,PriceSeries>();      
      
      for(PriceType type : PriceType.values()) {
         PriceSeries bid = depth.getBid(type);
         PriceSeries offer = depth.getOffer(type);
         PriceSeries bidDepth = filterDepth(bid, include);
         PriceSeries offerDepth = filterDepth(offer, include);
      
         bidPrices.put(type, bidDepth);
         offerPrices.put(type, offerDepth);
      }
      String security = depth.getSecurity();
      long version = depth.getVersion();
      
      return new Depth(security, bidPrices, offerPrices, version);
   }
   
   private PriceSeries filterDepth(PriceSeries prices, List<String> include) {
      List<Price> filtered = new ArrayList<Price>();
      
      if(!include.isEmpty()) {
         int remaining = include.size();     
         int size = prices.size();  
         
         for(int i = 0; i < size; i++) {
            Price price = prices.getAt(i);
            String company = price.getCompany();
            
            if(include.contains(company)) {
               filtered.add(price);
                  
               if(--remaining > 0) {
                  return new PriceSeries(filtered);
               }
            }
         }
         return new PriceSeries(filtered); 
      }     
      return prices;
   }
}
