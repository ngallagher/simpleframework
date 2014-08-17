package org.simpleframework.demo.table.extract;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.simpleframework.demo.table.product.Price;
import org.simpleframework.demo.table.product.PriceType;
import org.simpleframework.demo.table.product.Side;

public class RowExtractorTest extends TestCase {
   
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
   
   public void testRowExtractor() throws Exception {
     Map<String, CellExtractor> extractors = new HashMap<String, CellExtractor>();
     ExampleProduct product = new ExampleProduct(
           "X",
           new Price("X", PriceType.EFP, Side.BID, "HSBC", 11.1, 100000L),
           new Price("X", PriceType.EFP, Side.OFFER, "HSBC", 13.1, 100000L),
           new Price("X", PriceType.EFP, Side.BID, "HSBC", 11.1, 100000L),
           new Price("X", PriceType.EFP, Side.OFFER, "ANZ", 10.1, 100000L));           
           
     extractors.put("bidPrice", new PropertyCellExtractor("bid.price", ExampleProduct.class));
     extractors.put("offerPrice", new PropertyCellExtractor("offer.price", ExampleProduct.class));     
     extractors.put("bestBidPrice", new PropertyCellExtractor("bestBid.price", ExampleProduct.class));
     extractors.put("bestOfferPrice", new PropertyCellExtractor("bestOffer.price", ExampleProduct.class));
     
     extractors.put("bestBidCompany", new PropertyCellExtractor("bestBid.company", ExampleProduct.class));
     extractors.put("bestOfferCompany", new PropertyCellExtractor("bestOffer.company", ExampleProduct.class));
     extractors.put("bidPriceBackground", new PredicateCellExtractor("bid.company == bestBid.company", "highlightBest", "highlightNormal"));
     extractors.put("offerPriceBackground", new PredicateCellExtractor("offer.company == bestOffer.company", "highlightBest", "highlightNormal"));
     
     RowExtractor extractor = new RowExtractor(extractors);
     Map<String, Object> row = extractor.extract(product);
    
     assertEquals(row.get("bidPrice"), 11.1);
     assertEquals(row.get("offerPrice"), 13.1);
     assertEquals(row.get("bestBidPrice"), 11.1);
     assertEquals(row.get("bestOfferPrice"), 10.1);     
     assertEquals(row.get("bestBidCompany"), "HSBC");
     assertEquals(row.get("bestOfferCompany"), "ANZ");     
     assertEquals(row.get("bidPriceBackground"), "highlightBest");
     assertEquals(row.get("offerPriceBackground"), "highlightNormal");
     
     long start = System.currentTimeMillis();
     
     for(int i = 0; i < 1000000; i++) {
        Map<String, Object> next = extractor.extract(product);
        
        assertFalse("Row should not be empty", next.isEmpty());
     }
     long end = System.currentTimeMillis();
     long duration = end - start;
     long rowsPerMillisecond = 1000000 / duration;
     long rowsPerSecond = rowsPerMillisecond * 1000;
     
     System.err.println("Time for 1 million was " + duration + " milliseconds, rows per second was " + rowsPerSecond);

   }

}
