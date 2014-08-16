package org.simpleframework.demo.table.product;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simpleframework.demo.table.extract.Client;

public class ProductFilter {

   private final Client client;
   
   public ProductFilter(Client client) {
      this.client = client;
   }
   
   public Product filter(Depth depth) {
      String company = client.getCompany();
      List<String> inclusive = client.getPartners();
      List<String> exclusive = Collections.singletonList(company);
      
      if(!inclusive.isEmpty()) {
         Map<PriceType, PricePair> bestPrices = new HashMap<PriceType, PricePair>();
         Map<PriceType, PricePair> companyPrices = new HashMap<PriceType, PricePair>();      
         
         for(PriceType type : PriceType.values()) {
            PricePair bestPrice = filter(depth, type, inclusive);
            PricePair companyPrice = filter(depth, type, exclusive);
            
            bestPrices.put(type, bestPrice);
            companyPrices.put(type, companyPrice);
         }
         String security = depth.getSecurity();
         long version = depth.getVersion();
         
         return new Product(security, bestPrices, companyPrices, version);
      }
      return null;
   }
   
   private PricePair filter(Depth depth, PriceType type, List<String> include) {
      String security = depth.getSecurity();
      List<Price> bid = depth.getBid(type);
      List<Price> offer = depth.getOffer(type);
      Price bestBid = filter(bid, include);
      Price bestOffer = filter(offer, include);
      
      return new PricePair(security, type, bestBid, bestOffer);      
   }
   
   private Price filter(List<Price> prices, List<String> include) {
      for(Price price : prices) {
         String company = price.getCompany();
         
         if(include.contains(company)) {
            return price;
         }
      }
      return null;      
   }
}
