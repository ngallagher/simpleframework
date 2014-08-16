package org.simpleframework.demo.table.product;

import junit.framework.TestCase;

public class DepthProcessorTest extends TestCase {
   
   public void testDepth() throws Exception {
      DepthDebugger debugger = new DepthDebugger();
      DepthProcessor processor = new DepthProcessor(debugger);
      
      processor.update(new Price("X", PriceType.EFP, Side.BID, "HSBC", 10.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.OFFER, "ANZ", 11.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "ANZ", 11.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "DB", 12.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "DB", 9.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.BID, "ANZ", 8.1, 100000L));
      processor.update(new Price("X", PriceType.EFP, Side.OFFER, "HSBC", 11.0, 100000L));      
   }

}
