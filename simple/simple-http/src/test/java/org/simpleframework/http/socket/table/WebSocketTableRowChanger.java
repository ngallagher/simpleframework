package org.simpleframework.http.socket.table;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class WebSocketTableRowChanger extends Thread {
   
   private final WebSocketTableChanger changer;

   public WebSocketTableRowChanger(WebSocketTable table) {
      this.changer = new WebSocketTableChanger(table);
   }
   
   public void run() {
      Random random = new SecureRandom();
      List<String> products = new ArrayList<String>();
      
      products.add("QTC44");
      products.add("NSW22");
      products.add("NSW23");
      products.add("NSW24");
      products.add("WAGA19");
      products.add("CGS19");
      products.add("CGS21");
      products.add("CGS22");
      products.add("CGSJun14");
      products.add("CGSOct14");
      products.add("CGSDec12");
      products.add("QTC33");
      
      for(int i = 0; i < 100; i++) {
         products.add("CGS" + i);
      }

      while(true) {
         try {     
            int rows = products.size();
            long randomWait = random.nextInt(50) + 40;
            int randomRow = random.nextInt(rows);
            int randomBid = random.nextInt(50) + 1;
            int randomOffer = random.nextInt(50) + 1;
            int randomVolume = (random.nextInt(5) + 1) * 10;
            
            if(randomRow != 0) {
               String product = products.get(randomRow);
               Map<String, Object> map = new HashMap<String, Object>();               
               
               map.put("id", randomRow);
               map.put("product", product);
               map.put("bidOutrightVolume", randomVolume);
               map.put("bidOutright", randomBid);
               map.put("offerOutright", randomOffer);
               map.put("offerOutrightVolume", randomVolume);
               map.put("bidEFPVolume", randomVolume);      
               map.put("bidEFP", randomBid);
               map.put("offerEFP", randomOffer);
               map.put("offerEFPVolume", randomVolume);
               map.put("reference", "3ySep");               

               changer.onChange(map);
            }            
            Thread.sleep(randomWait);
         } catch(Exception e) {
            e.printStackTrace();          
         }
      }
   }   

}
