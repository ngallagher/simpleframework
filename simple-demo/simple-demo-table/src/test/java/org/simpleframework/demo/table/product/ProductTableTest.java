package org.simpleframework.demo.table.product;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

public class ProductTableTest extends TestCase {

   public void testDepth() throws Exception {
      ProductTable table = new ProductTable();
      DepthProcessor processor = new DepthProcessor(table);
      ProductQuery query = new ProductQuery("john@hsbc.com", "HSBC", Arrays.asList("DB", "ANZ", "HSBC"));
      
      processor.update(new Price("X", PriceType.EFP, Side.BID, "HSBC", 10.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.OFFER, "ANZ", 11.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "ANZ", 11.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "DB", 12.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "DB", 9.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "ANZ", 8.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.OFFER, "HSBC", 12.0, 100000L));  
      
      List<Product> product = table.query(query);
      
      assertEquals(product.size(), 1);
      assertEquals(product.get(0).getSecurity(), "X");
      assertEquals(product.get(0).getCompanyEFP().getOffer().getPrice(), 12.0);
      assertEquals(product.get(0).getCompanyEFP().getBid().getPrice(), 10.1);
      assertEquals(product.get(0).getCompanyEFP().getOffer().getCompany(), "HSBC");
      assertEquals(product.get(0).getCompanyEFP().getBid().getCompany(), "HSBC");      
      assertEquals(product.get(0).getBestEFP().getOffer().getPrice(), 11.1);
      assertEquals(product.get(0).getBestEFP().getBid().getPrice(), 10.1);  
      assertEquals(product.get(0).getBestEFP().getOffer().getCompany(), "ANZ");
      assertEquals(product.get(0).getBestEFP().getBid().getCompany(), "HSBC");      
   }
}
