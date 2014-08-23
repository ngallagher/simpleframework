package org.simpleframework.http.socket.table;

public enum WebSocketTableColumn {
   BID_OUTRIGHT_VOLUME("bov", "Bid Outright Volume"),
   OFFER_OUTRIGHT_VOLUME("bov", "Offer Outright Volume"),
   BID_OUTRIGHT("bo", "Bid Outright"),
   OFFER_OUTRIGHT("bov", "Offer Outright"),   
   BID_EFP_VOLUME("bov", "Bid EFP Volume"),
   OFFER_EFP_VOLUME("bov", "Offer EFP Volume"),
   BID_EFP("bo", "Bid EFP"),
   OFFER_EFP("bov", "Offer EFP"),
   PRODUCT("p", "Product");
   
   public final String name;
   public final String title;
   
   private WebSocketTableColumn(String name, String title) {
      this.name = name;
      this.title = title;
   }
   
}
