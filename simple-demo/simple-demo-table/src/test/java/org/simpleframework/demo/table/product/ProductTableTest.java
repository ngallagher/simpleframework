package org.simpleframework.demo.table.product;

import java.util.Arrays;
import java.util.List;

import org.simpleframework.demo.table.Query;

import junit.framework.TestCase;

public class ProductTableTest extends TestCase {

   public void testDepth() throws Exception {
      ProductTable table = new ProductTable();
      DepthProcessor processor = new DepthProcessor(table);
      Query client = new Query("john@hsbc.com", "HSBC", Arrays.asList("DB", "ANZ", "HSBC"));
      ProductQuery cursor = new ProductQuery(client);
      
      processor.update(new Price("X", PriceType.EFP, Side.BID, "HSBC", 10.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.OFFER, "ANZ", 11.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "ANZ", 11.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "DB", 12.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "DB", 9.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "ANZ", 8.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.OFFER, "HSBC", 12.0, 100000L));  
      
      List<Product> product = table.query(cursor);
      
      assertEquals(product.size(), 1);
      assertEquals(product.get(0).getSecurity(), "X");
      assertEquals(product.get(0).getCompanyDepth().getOffer(PriceType.EFP).getTop().getPrice(), 12.0);
      assertEquals(product.get(0).getCompanyDepth().getBid(PriceType.EFP).getTop().getPrice(), 10.1);
      assertEquals(product.get(0).getCompanyDepth().getOffer(PriceType.EFP).getTop().getCompany(), "HSBC");
      assertEquals(product.get(0).getCompanyDepth().getBid(PriceType.EFP).getTop().getCompany(), "HSBC");      
      assertEquals(product.get(0).getMarketDepth().getOffer(PriceType.EFP).getTop().getPrice(), 11.1);
      assertEquals(product.get(0).getMarketDepth().getBid(PriceType.EFP).getTop().getPrice(), 10.1);  
      assertEquals(product.get(0).getMarketDepth().getOffer(PriceType.EFP).getTop().getCompany(), "ANZ");
      assertEquals(product.get(0).getMarketDepth().getBid(PriceType.EFP).getTop().getCompany(), "HSBC");      
   }
}
