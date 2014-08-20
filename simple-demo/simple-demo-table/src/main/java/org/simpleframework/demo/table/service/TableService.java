package org.simpleframework.demo.table.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.simpleframework.demo.table.Query;
import org.simpleframework.demo.table.product.ProductStaticSource;
import org.simpleframework.demo.table.telemetry.PerformanceAnalyzer;
import org.simpleframework.http.Request;
import org.simpleframework.http.socket.Session;
import org.simpleframework.http.socket.WebSocket;
import org.simpleframework.http.socket.service.Service;

public class TableService implements Service {
   
   private final ProductStaticSource source;
   private final PerformanceAnalyzer service;
   private final List<TableUpdater> updaters;
   
   public TableService(List<TableUpdater> updaters, PerformanceAnalyzer service, ProductStaticSource source) {
      this.updaters = updaters;
      this.service = service;
      this.source = source;
   }

   @Override
   public void connect(Session session) {      
      WebSocket socket = session.getSocket();
      Request request = session.getRequest();      
      String user = request.getParameter("user");
      String company = request.getParameter("company");
      String filter = request.getParameter("products");
      String companies = request.getParameter("companies");  
      List<String> products = new ArrayList<String>();
      List<String> companyList = new ArrayList<String>();
      
      if(companies != null && !companies.isEmpty()) {
         String[] values = companies.split(",");
         
         for(String value : values) {
            companyList.add(value);
         }
      }
      if(filter != null && !filter.isEmpty()) {
         String[] values = filter.split(",");
         
         for(String value : values) {
            products.add(value);
         }
      } else {
         List<String> everything = source.getNames();
         
         for(String value : everything) {
            products.add(value);
         }
      }      
      if(user != null && company != null) {
         Query client = new Query(user, company, companyList, products);
         
         for(TableUpdater updater : updaters) {
            TableListener callbacks = new TableListener(updater, service);
            try {
               socket.register(callbacks);
               updater.subscribe(socket, client);
            }catch(Exception e) {
               e.printStackTrace();
            }
         }      
      }
   }

}
