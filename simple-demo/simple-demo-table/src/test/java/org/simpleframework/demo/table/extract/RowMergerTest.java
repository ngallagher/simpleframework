package org.simpleframework.demo.table.extract;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.simpleframework.demo.table.product.Price;
import org.simpleframework.demo.table.product.PriceType;
import org.simpleframework.demo.table.product.Side;

public class RowMergerTest extends TestCase {
   
   private static class ExampleProduct {
      
      private final Price bestBid;
      private final Price bestOffer;
      private final Price bid;
      private final Price offer;
      private final String security;
      
      public ExampleProduct(String security, Price bid, Price offer, Price bestBid, Price bestOffer) {
         this.security = security;
         this.bid = bid;
         this.offer = offer;
         this.bestBid = bestBid;
         this.bestOffer = bestOffer;
      }

      public Price getBestBid() {
         return bestBid;
      }

      public Price getBestOffer() {
         return bestOffer;
      }

      public Price getBid() {
         return bid;
      }

      public Price getOffer() {
         return offer;
      }

      public String getSecurity() {
         return security;
      }
   }
   
   public void testRowExtractor() {
     ExampleProduct product1 = new ExampleProduct(
           "X",
           new Price("X", PriceType.EFP, Side.BID, "HSBC", 11.1, 100000L),
           new Price("X", PriceType.EFP, Side.OFFER, "HSBC", 13.1, 100000L),
           new Price("X", PriceType.EFP, Side.BID, "HSBC", 11.1, 100000L),
           new Price("X", PriceType.EFP, Side.OFFER, "ANZ", 10.1, 100000L)); 
     
     ExampleProduct product2 = new ExampleProduct(
           "X",
           new Price("X", PriceType.EFP, Side.BID, "HSBC", 9.1, 100000L),
           new Price("X", PriceType.EFP, Side.OFFER, "HSBC", 13.1, 100000L),
           new Price("X", PriceType.EFP, Side.BID, "DB", 11.1, 100000L),
           new Price("X", PriceType.EFP, Side.OFFER, "ANZ", 10.1, 100000L));
     
     Map<String, CellExtractor> extractors = new HashMap<String, CellExtractor>();     
           
     extractors.put("bidPrice", new PropertyCellExtractor("bid.price", ExampleProduct.class));
     extractors.put("offerPrice", new PropertyCellExtractor("offer.price", ExampleProduct.class));     
     extractors.put("bestBidPrice", new PropertyCellExtractor("bestBid.price", ExampleProduct.class));
     extractors.put("bestOfferPrice", new PropertyCellExtractor("bestOffer.price", ExampleProduct.class));     
     extractors.put("bestBidCompany", new PropertyCellExtractor("bestBid.company", ExampleProduct.class));
     extractors.put("bestOfferCompany", new PropertyCellExtractor("bestOffer.company", ExampleProduct.class));
     extractors.put("bidPriceBackground", new PredicateCellExtractor("bid.company == bestBid.company", "highlightBest", "highlightNormal"));
     extractors.put("offerPriceBackground", new PredicateCellExtractor("offer.company == bestOffer.company", "highlightBest", "highlightNormal"));
     
     Set<String> schema = new HashSet<String>();     
     RowMerger merger = new RowMerger(schema);
     RowExtractor extractor = new RowExtractor(extractors);
     
     schema.add("bidPrice");
     schema.add("offerPrice");
     schema.add("bestBidPrice");
     schema.add("bestOfferPrice");
     schema.add("bestBidCompany");     
     schema.add("bestOfferCompany");
     schema.add("bidPriceBackground");
     schema.add("offerPriceBackground");     
     
     Map<String, Object> row1 = extractor.extract(product1);
    
     assertEquals(row1.get("bidPrice"), 11.1);
     assertEquals(row1.get("offerPrice"), 13.1);
     assertEquals(row1.get("bestBidPrice"), 11.1);
     assertEquals(row1.get("bestOfferPrice"), 10.1);     
     assertEquals(row1.get("bestBidCompany"), "HSBC");
     assertEquals(row1.get("bestOfferCompany"), "ANZ");     
     assertEquals(row1.get("bidPriceBackground"), "highlightBest");
     assertEquals(row1.get("offerPriceBackground"), "highlightNormal");
     
     Map<String, Object> row2 = extractor.extract(product2);
     
     assertEquals(row2.get("bidPrice"), 9.1);
     assertEquals(row2.get("offerPrice"), 13.1);
     assertEquals(row2.get("bestBidPrice"), 11.1);
     assertEquals(row2.get("bestOfferPrice"), 10.1);     
     assertEquals(row2.get("bestBidCompany"), "DB");
     assertEquals(row2.get("bestOfferCompany"), "ANZ");     
     assertEquals(row2.get("bidPriceBackground"), "highlightNormal");
     assertEquals(row2.get("offerPriceBackground"), "highlightNormal");
     
     Map<String, Object> merge1 = merger.merge(row1, 1);
     
     assertEquals(merge1.get("bidPrice"), 11.1);
     assertEquals(merge1.get("offerPrice"), 13.1);
     assertEquals(merge1.get("bestBidPrice"), 11.1);
     assertEquals(merge1.get("bestOfferPrice"), 10.1);     
     assertEquals(merge1.get("bestBidCompany"), "HSBC");
     assertEquals(merge1.get("bestOfferCompany"), "ANZ");     
     assertEquals(merge1.get("bidPriceBackground"), "highlightBest");
     assertEquals(merge1.get("offerPriceBackground"), "highlightNormal");
     
     Map<String, Object> merge2 = merger.merge(row2, 2);
     
     assertEquals(merge2.get("bidPrice"), 9.1);
     assertEquals(merge2.get("offerPrice"), null);
     assertEquals(merge2.get("bestBidPrice"), null);
     assertEquals(merge2.get("bestOfferPrice"), null);     
     assertEquals(merge2.get("bestBidCompany"), "DB");
     assertEquals(merge2.get("bestOfferCompany"), null);     
     assertEquals(merge2.get("bidPriceBackground"), "highlightNormal");
     assertEquals(merge2.get("offerPriceBackground"), null);
   }

}
