package org.simpleframework.demo.table.product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.simpleframework.demo.table.extract.CellExtractor;
import org.simpleframework.demo.table.extract.PredicateCellExtractor;
import org.simpleframework.demo.table.extract.PropertyCellExtractor;
import org.simpleframework.demo.table.extract.RowChange;
import org.simpleframework.demo.table.extract.RowExtractor;
import org.simpleframework.demo.table.extract.TableCursor;
import org.simpleframework.demo.table.extract.TableSchema;

public class ProductTableModelTest extends TestCase {

   public void testTableModel() throws Exception {
      ProductTable table = new ProductTable();
      DepthDebugger debugger = new DepthDebugger();
      List<DepthListener> listeners = new ArrayList<DepthListener>();
      
      listeners.add(debugger);
      listeners.add(table);
      
      DepthProcessor processor = new DepthProcessor(listeners);
      ProductSubscription subscription = new ProductSubscription("john@hsbc.com", "HSBC", Arrays.asList("DB", "ANZ", "HSBC"));
      ProductTableModel model = new ProductTableModel(table, subscription);
      
      processor.update(new Price("X", PriceType.EFP, Side.BID, "HSBC", 10.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.OFFER, "ANZ", 11.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "ANZ", 11.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "DB", 12.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "DB", 9.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "ANZ", 8.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.OFFER, "HSBC", 12.0, 100000L));  
      
      Map<String, CellExtractor> extractors = new HashMap<String, CellExtractor>();     
      
      extractors.put("bidEFPPrice", new PropertyCellExtractor("companyEFP.bid.price", Product.class));
      extractors.put("offerEFPPrice", new PropertyCellExtractor("companyEFP.offer.price", Product.class));
      
      extractors.put("bestBidEFPPrice", new PropertyCellExtractor("bestEFP.bid.price", Product.class));
      extractors.put("bestOfferEFPPrice", new PropertyCellExtractor("bestEFP.offer.price", Product.class));
      
      extractors.put("bestBidEFPCompany", new PropertyCellExtractor("bestEFP.bid.company", Product.class));
      extractors.put("bestOfferEFPCompany", new PropertyCellExtractor("bestEFP.offer.company", Product.class));
      
      extractors.put("bidPriceEFPBackground", new PredicateCellExtractor("companyEFP.bid.company == bestEFP.bid.company", "highlightBest", "highlightNormal"));
      extractors.put("offerPriceEFPBackground", new PredicateCellExtractor("companyEFP.offer.company == bestEFP.offer.company", "highlightBest", "highlightNormal"));
      
      Set<String> columns = new HashSet<String>();
      TableSchema schema = new TableSchema(columns);     
      RowExtractor extractor = new RowExtractor(extractors);
      TableCursor cursor = new TableCursor(model, schema, extractor); 
      
      columns.add("bidEFPPrice");
      columns.add("offerEFPPrice");
      columns.add("bestBidEFPPrice");
      columns.add("bestOfferEFPPrice");
      columns.add("bestBidEFPCompany");     
      columns.add("bestOfferEFPCompany");
      columns.add("bidPriceEFPBackground");
      columns.add("offerPriceEFPBackground"); 
      
      List<RowChange> change1 = cursor.update();
      
      assertNotNull(change1);
      assertEquals(change1.size(), 1);
      assertEquals(change1.get(0).getChanges().get("bestBidEFPPrice"), 10.1);
      assertEquals(change1.get(0).getChanges().get("bestOfferEFPPrice"), 11.1);      
      assertEquals(change1.get(0).getChanges().get("bestBidEFPCompany"), "HSBC");
      assertEquals(change1.get(0).getChanges().get("bestOfferEFPCompany"), "ANZ");
      assertEquals(change1.get(0).getChanges().get("bidPriceEFPBackground"), "highlightBest");
      assertEquals(change1.get(0).getChanges().get("offerPriceEFPBackground"), "highlightNormal");        
   }
}
