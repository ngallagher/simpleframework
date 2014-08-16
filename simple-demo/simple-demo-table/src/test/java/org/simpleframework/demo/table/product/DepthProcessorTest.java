package org.simpleframework.demo.table.product;

import java.text.DecimalFormat;
import java.util.List;

import junit.framework.TestCase;

public class DepthProcessorTest extends TestCase implements DepthListener {
   
   public void testDepth() throws Exception {
      DepthProcessor processor = new DepthProcessor(this);
      
      processor.update(new Price("X", PriceType.EFP, Side.BID, "HSBC", 10.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.OFFER, "ANZ", 11.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "ANZ", 11.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "DB", 12.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "DB", 9.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "ANZ", 8.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.OFFER, "HSBC", 11.0, 100000L));      
   }

   @Override
   public void update(Depth depth) {
      StringBuilder builder = new StringBuilder();
      String security = depth.getSecurity();
      long version = depth.getVersion();
      
      builder.append(security);
      builder.append(" version ");
      builder.append(version);
      builder.append("\r\n");
      builder.append("+---------------------------------------------------------------+\r\n");
      builder.append("|Bid EFP        |Offer EFP      |Bid Outright   |Offer Outright |\r\n");
      builder.append("+---------------------------------------------------------------+\r\n");
      
      List<Price> bidEFP = depth.getBid(PriceType.EFP);
      List<Price> offerEFP = depth.getOffer(PriceType.EFP);
      List<Price> bidOutright = depth.getBid(PriceType.OUTRIGHT);
      List<Price> offerOutright = depth.getOffer(PriceType.OUTRIGHT);    
      
      for(int i = 0; i < 10; i++) {
         builder.append("|");
         builder.append(format(bidEFP, i));
         builder.append("|");       
         builder.append(format(offerEFP, i));
         builder.append("|");        
         builder.append(format(bidOutright, i));
         builder.append("|");        
         builder.append(format(offerOutright, i));
         builder.append("|\r\n");
      }
      builder.append("+---------------------------------------------------------------+");
      System.err.println(builder);
   }
   
   public String format(List<Price> prices, int level) {
      StringBuilder builder = new StringBuilder();
      DecimalFormat format = new DecimalFormat("###.##");
      int depth = prices.size();
      
      if(depth > level) {
         Price price = prices.get(level);
         Double value = price.getPrice();
         String company = price.getCompany();
         String text = format.format(value);
         int length = text.length() + company.length() + 3;            
        
         builder.append(text);
         builder.append(" (");
         builder.append(company);
         builder.append(")");
         
         for(int j = 0; j < 15 - length; j++) {
            builder.append(" ");
         }                  
      } else {
         builder.append("               ");
      }
      return builder.toString();
   }

}
