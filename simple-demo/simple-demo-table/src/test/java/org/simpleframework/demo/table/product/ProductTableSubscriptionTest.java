package org.simpleframework.demo.table.product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.simpleframework.demo.table.Query;
import org.simpleframework.demo.table.TableCursor;
import org.simpleframework.demo.table.extract.CellExtractor;
import org.simpleframework.demo.table.extract.PredicateCellExtractor;
import org.simpleframework.demo.table.extract.PropertyCellExtractor;
import org.simpleframework.demo.table.extract.RowChange;
import org.simpleframework.demo.table.extract.RowExtractor;
import org.simpleframework.demo.table.schema.ColumnStyle;
import org.simpleframework.demo.table.schema.StringColumnStyle;
import org.simpleframework.demo.table.schema.TableSchema;

public class ProductTableSubscriptionTest extends TestCase {

   public void testTableModel() throws Exception {
      ProductTable table = new ProductTable();
      DepthDebugger debugger = new DepthDebugger();
      List<DepthListener> listeners = new ArrayList<DepthListener>();
      
      listeners.add(debugger);
      listeners.add(table);
      
      DepthProcessor processor = new DepthProcessor(listeners);
      Query client = new Query("john@hsbc.com", "HSBC", Arrays.asList("DB", "ANZ", "HSBC"));
      ProductTableSubscription subscription = new ProductTableSubscription(table, client);
      
      processor.update(new Price("X", PriceType.EFP, Side.BID, "HSBC", 10.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.OFFER, "ANZ", 11.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "ANZ", 11.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "DB", 12.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "DB", 9.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "ANZ", 8.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.OFFER, "HSBC", 12.0, 100000L));  
      
      Map<String, CellExtractor> extractors = new HashMap<String, CellExtractor>();     
      
      extractors.put("bidEFPPrice", new PropertyCellExtractor("companyDepth.bid[EFP].top.price", Product.class));
      extractors.put("offerEFPPrice", new PropertyCellExtractor("companyDepth.offer[EFP].top.price", Product.class));
      
      extractors.put("bestBidEFPPrice", new PropertyCellExtractor("marketDepth.bid[EFP].top.price", Product.class));
      extractors.put("bestOfferEFPPrice", new PropertyCellExtractor("marketDepth.offer[EFP].top.price", Product.class));
      
      extractors.put("bestBidEFPCompany", new PropertyCellExtractor("marketDepth.bid[EFP].top.company", Product.class));
      extractors.put("bestOfferEFPCompany", new PropertyCellExtractor("marketDepth.offer[EFP].top.company", Product.class));
      
      extractors.put("bidPriceEFPBackground", new PredicateCellExtractor("companyDepth.bid[EFP].top.company == marketDepth.bid[EFP].top.company && companyDepth.bid[EFP].top.company != 'null'", "highlightBest", "highlightNormal"));
      extractors.put("offerPriceEFPBackground", new PredicateCellExtractor("companyDepth.offer[EFP].top.company == marketDepth.offer[EFP].top.company && companyDepth.offer[EFP].top.company != 'null'", "highlightBest", "highlightNormal"));
      
      List<ColumnStyle> columns = new ArrayList<ColumnStyle>();
      TableSchema schema = new TableSchema("product", columns);     
      RowExtractor extractor = new RowExtractor(extractors);
      TableCursor cursor = new TableCursor(subscription, schema, extractor); 
      
      columns.add(new StringColumnStyle("bidEFPPrice","{bidEFPPrice}"));
      columns.add(new StringColumnStyle("offerEFPPrice","{offerEFPPrice}"));
      columns.add(new StringColumnStyle("bestBidEFPPrice","{bestBidEFPPrice}"));
      columns.add(new StringColumnStyle("bestOfferEFPPrice","{bestOfferEFPPrice}"));
      columns.add(new StringColumnStyle("bestBidEFPCompany","{bestBidEFPCompany}"));     
      columns.add(new StringColumnStyle("bestOfferEFPCompany","{bestOfferEFPCompany}"));
      columns.add(new StringColumnStyle("bidPriceEFPBackground","{bidPriceEFPBackground}"));
      columns.add(new StringColumnStyle("offerPriceEFPBackground","{offerPriceEFPBackground}")); 
      
      List<RowChange> change1 = cursor.update();
      
      assertNotNull(change1);
      assertEquals(change1.size(), 1);
      assertEquals(change1.get(0).getChanges().get("bestBidEFPPrice"), 10.1);
      assertEquals(change1.get(0).getChanges().get("bestOfferEFPPrice"), 11.1);      
      assertEquals(change1.get(0).getChanges().get("bestBidEFPCompany"), "HSBC");
      assertEquals(change1.get(0).getChanges().get("bestOfferEFPCompany"), "ANZ");
      assertEquals(change1.get(0).getChanges().get("bidPriceEFPBackground"), "highlightBest");
      assertEquals(change1.get(0).getChanges().get("offerPriceEFPBackground"), "highlightNormal");  
      
      List<RowChange> change2 = cursor.update();
      
      assertNotNull(change2);
      assertEquals(change2.size(), 0);
      
      processor.update(new Price("X", PriceType.EFP, Side.BID, "HSBC", 2.1, 100000L));
      
      List<RowChange> change3 = cursor.update();
      
      assertNotNull(change3);
      assertEquals(change3.size(), 1);
      assertEquals(change3.get(0).getChanges().get("bestBidEFPPrice"), 9.1);
      assertEquals(change3.get(0).getChanges().get("bestOfferEFPPrice"), null);      
      assertEquals(change3.get(0).getChanges().get("bestBidEFPCompany"), "DB");
      assertEquals(change3.get(0).getChanges().get("bestOfferEFPCompany"), null);
      assertEquals(change3.get(0).getChanges().get("bidPriceEFPBackground"), "highlightNormal");
      assertEquals(change3.get(0).getChanges().get("offerPriceEFPBackground"), null);  
      
      List<RowChange> change4 = cursor.update();
      
      assertNotNull(change4);
      assertEquals(change4.size(), 0);      
      
      processor.update(new Price("Y", PriceType.EFP, Side.BID, "HSBC", 22.66, 200000L));
      
      List<RowChange> change5 = cursor.update();
      
      assertNotNull(change5);
      assertEquals(change5.size(), 1);
      assertEquals(change5.get(0).getChanges().get("bestBidEFPPrice"), 22.66);
      assertEquals(change5.get(0).getChanges().get("bestOfferEFPPrice"), null);      
      assertEquals(change5.get(0).getChanges().get("bestBidEFPCompany"), "HSBC");
      assertEquals(change5.get(0).getChanges().get("bestOfferEFPCompany"), null);
      assertEquals(change5.get(0).getChanges().get("bidPriceEFPBackground"), "highlightBest");
      assertEquals(change5.get(0).getChanges().get("offerPriceEFPBackground"), "highlightNormal");  
   }
}
