package org.simpleframework.demo.table.extract;

import java.util.List;

public class Client {
   
   private final List<String> partners;
   private final String name;
   private final String company;
   
   public Client(String name, String company, List<String> partners) {
      this.partners = partners;
      this.company = company;
      this.name = name;
   }

   public String getName() {
      return name;
   }
   
   public String getCompany() {
      return company;
   }
   
   public List<String> getPartners() {
      return partners;
   }
}
