package org.simpleframework.demo.table.extract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.TestCase;

import org.simpleframework.demo.table.Row;
import org.simpleframework.demo.table.TableCursor;
import org.simpleframework.demo.table.TableSubscription;
import org.simpleframework.demo.table.product.Price;
import org.simpleframework.demo.table.product.PriceType;
import org.simpleframework.demo.table.product.Side;
import org.simpleframework.demo.table.schema.ColumnStyle;
import org.simpleframework.demo.table.schema.StringColumnStyle;
import org.simpleframework.demo.table.schema.TableSchema;

public class TableCursorTest extends TestCase {
   
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
   
   public static class ExampleProductTableModel implements TableSubscription {
      
      private final Map<String, ExampleProduct> products;
      private final Map<String, Long> versions;
      private final List<String> indexes;
      private final AtomicLong version;
      
      public ExampleProductTableModel() {
         this.products = new LinkedHashMap<String, ExampleProduct>();
         this.versions = new LinkedHashMap<String, Long>();
         this.indexes = new ArrayList<String>();
         this.version = new AtomicLong();         
      }
      
      public void update(ExampleProduct product) {
         String security = product.getSecurity();
         Long current = version.getAndIncrement();
         
         products.put(security, product);
         versions.put(security, current);
      }

      @Override
      public List<Row> next() {
         Set<String> keys = products.keySet();
         
         if(!keys.isEmpty()) {
            List<Row> rows = new LinkedList<Row>();
            
            for(String key : keys) {
               ExampleProduct product = products.get(key);
               Long version = versions.get(key);
               int index = indexes.indexOf(key);
               
               if(index == -1) {
                  indexes.add(key);
                  index = indexes.indexOf(key);
               }               
               Row row = new Row(product, index, version);                  
               rows.add(row);               
            }
            return rows;
         }
         return Collections.emptyList();
      }
      
   }
  
   
   public void testRowExtractor() throws Exception {
     ExampleProduct productX = new ExampleProduct(
           "X",
           new Price("X", PriceType.EFP, Side.BID, "HSBC", 11.1, 100000L),
           new Price("X", PriceType.EFP, Side.OFFER, "HSBC", 13.1, 100000L),
           new Price("X", PriceType.EFP, Side.BID, "HSBC", 11.1, 100000L),
           new Price("X", PriceType.EFP, Side.OFFER, "ANZ", 10.1, 100000L)); 
     
     ExampleProduct productY = new ExampleProduct(
           "Y",
           new Price("Y", PriceType.EFP, Side.BID, "HSBC", 9.1, 100000L),
           new Price("Y", PriceType.EFP, Side.OFFER, "HSBC", 13.1, 100000L),
           new Price("Y", PriceType.EFP, Side.BID, "DB", 11.1, 100000L),
           new Price("Y", PriceType.EFP, Side.OFFER, "ANZ", 10.1, 100000L));
     
     ExampleProductTableModel model = new ExampleProductTableModel();
    
     model.update(productX);
     model.update(productY);     
     
     Map<String, CellExtractor> extractors = new HashMap<String, CellExtractor>();     
           
     extractors.put("bidPrice", new PropertyCellExtractor("bid.price", ExampleProduct.class));
     extractors.put("offerPrice", new PropertyCellExtractor("offer.price", ExampleProduct.class));     
     extractors.put("bestBidPrice", new PropertyCellExtractor("bestBid.price", ExampleProduct.class));
     extractors.put("bestOfferPrice", new PropertyCellExtractor("bestOffer.price", ExampleProduct.class));     
     extractors.put("bestBidCompany", new PropertyCellExtractor("bestBid.company", ExampleProduct.class));
     extractors.put("bestOfferCompany", new PropertyCellExtractor("bestOffer.company", ExampleProduct.class));
     extractors.put("bidPriceBackground", new PredicateCellExtractor("bid.company == bestBid.company", "highlightBest", "highlightNormal"));
     extractors.put("offerPriceBackground", new PredicateCellExtractor("offer.company == bestOffer.company", "highlightBest", "highlightNormal"));
     
     List<ColumnStyle> columns = new ArrayList<ColumnStyle>();
     TableSchema schema = new TableSchema("exampleProduct", columns);     
     RowExtractor extractor = new RowExtractor(extractors);
     TableCursor cursor = new TableCursor(model, schema, extractor);
     
     columns.add(new StringColumnStyle("bidPrice", "{bidPrice}"));
     columns.add(new StringColumnStyle("offerPrice","{offerPrice}"));
     columns.add(new StringColumnStyle("bestBidPrice","{bestBidPrice}"));
     columns.add(new StringColumnStyle("bestOfferPrice","{bestOfferPrice}"));
     columns.add(new StringColumnStyle("bestBidCompany","{bestBidCompany}"));     
     columns.add(new StringColumnStyle("bestOfferCompany","{bestOfferCompany}"));
     columns.add(new StringColumnStyle("bidPriceBackground","{bidPriceBackground}"));
     columns.add(new StringColumnStyle("offerPriceBackground","{offerPriceBackground}"));           
     
     List<RowChange> changes1 = cursor.update();
     
     assertNotNull(changes1);
     assertEquals(changes1.size(), 2);   
     assertEquals(changes1.get(0).getChanges().get("bidPrice"), 11.1);
     assertEquals(changes1.get(0).getChanges().get("offerPrice"), 13.1);
     assertEquals(changes1.get(0).getChanges().get("bestBidPrice"), 11.1);
     assertEquals(changes1.get(0).getChanges().get("bestOfferPrice"), 10.1);     
     assertEquals(changes1.get(0).getChanges().get("bestBidCompany"), "HSBC");
     assertEquals(changes1.get(0).getChanges().get("bestOfferCompany"), "ANZ");     
     assertEquals(changes1.get(0).getChanges().get("bidPriceBackground"), "highlightBest");
     assertEquals(changes1.get(0).getChanges().get("offerPriceBackground"), "highlightNormal");     
     assertEquals(changes1.get(1).getChanges().get("bidPrice"), 9.1);
     assertEquals(changes1.get(1).getChanges().get("offerPrice"), 13.1);
     assertEquals(changes1.get(1).getChanges().get("bestBidPrice"), 11.1);
     assertEquals(changes1.get(1).getChanges().get("bestOfferPrice"), 10.1);     
     assertEquals(changes1.get(1).getChanges().get("bestBidCompany"), "DB");
     assertEquals(changes1.get(1).getChanges().get("bestOfferCompany"), "ANZ");     
     assertEquals(changes1.get(1).getChanges().get("bidPriceBackground"), "highlightNormal");
     assertEquals(changes1.get(1).getChanges().get("offerPriceBackground"), "highlightNormal");
     
     List<RowChange> changes2 = cursor.update();
     
     assertNotNull(changes2);
     assertEquals(changes2.size(), 0); 
     
     ExampleProduct updateY = new ExampleProduct(
           "Y",
           new Price("Y", PriceType.EFP, Side.BID, "HSBC", 10.1, 100000L),
           new Price("Y", PriceType.EFP, Side.OFFER, "JPM", 8.1, 100000L),
           new Price("Y", PriceType.EFP, Side.BID, "DB", 11.1, 100000L),
           new Price("Y", PriceType.EFP, Side.OFFER, "UBS", 1.1, 100000L));
     
     model.update(updateY);
     
     List<RowChange> changes3 = cursor.update();
     
     assertNotNull(changes3);
     assertEquals(changes3.size(), 1); 
     assertEquals(changes3.get(0).getChanges().get("bidPrice"), 10.1);
     assertEquals(changes3.get(0).getChanges().get("offerPrice"), 8.1);
     assertEquals(changes3.get(0).getChanges().get("bestBidPrice"), null);
     assertEquals(changes3.get(0).getChanges().get("bestOfferPrice"), 1.1);     
     assertEquals(changes3.get(0).getChanges().get("bestBidCompany"), null);
     assertEquals(changes3.get(0).getChanges().get("bestOfferCompany"), "UBS");     
     assertEquals(changes3.get(0).getChanges().get("bidPriceBackground"), null);
     assertEquals(changes3.get(0).getChanges().get("offerPriceBackground"), null);   

   }

}
