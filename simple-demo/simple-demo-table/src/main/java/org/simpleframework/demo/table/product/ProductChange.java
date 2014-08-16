package org.simpleframework.demo.table.product;

public interface ProductChange {
   String getSecurity();   
   Double getBestBidEFPPrice();
   Long getBestBidEFPVolume();   
   Double getBestOfferEFPPrice();
   Long getBestOfferEFPVolume();
   Double getBestBidOutrightPrice();
   Long getBestBidOutrightVolume();   
   Double getBestOfferOutrightPrice();
   Long getBestOfferOutrightVolume();   
   Double getCompanyBidEFPPrice();
   Long getCompanyBidEFPVolume();   
   Double getCompanyOfferEFPPrice();
   Long getCompanyOfferEFPVolume();
   Double getCompanyBidOutrightPrice();
   Long getCompanyBidOutrightVolume();   
   Double getCompanyOfferOutrightPrice();
   Long getCompanyOfferOutrightVolume();   
}
