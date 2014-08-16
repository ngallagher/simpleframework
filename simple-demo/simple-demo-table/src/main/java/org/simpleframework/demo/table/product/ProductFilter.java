package org.simpleframework.demo.table.product;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductFilter {

   private final List<String> include;
   private final List<String> company;
   
   public ProductFilter(String company, List<String> include) {
      this.company = Collections.singletonList(company);
      this.include = include;
   }
   
   public Product filter(Depth depth) {
      Map<PriceType, PricePair> bestPrices = new HashMap<PriceType, PricePair>();
      Map<PriceType, PricePair> companyPrices = new HashMap<PriceType, PricePair>();      
      
      for(PriceType type : PriceType.values()) {
         PricePair bestPrice = filter(depth, type, include);
         PricePair companyPrice = filter(depth, type, company);
         
         bestPrices.put(type, bestPrice);
         companyPrices.put(type, companyPrice);
      }
      String security = depth.getSecurity();
      long version = depth.getVersion();
      
      return new Product(security, bestPrices, companyPrices, version);
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
