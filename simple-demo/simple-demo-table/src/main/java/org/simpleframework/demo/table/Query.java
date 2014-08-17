package org.simpleframework.demo.table;

import java.util.Collections;
import java.util.List;

public class Query {

   private final List<String> companies;
   private final List<String> products;
   private final String name;
   private final String company;
   
   public Query(String name, String company) {
      this(name, company, Collections.EMPTY_LIST);
   }

   public Query(String name, String company, List<String> companies) {
      this(name, company, companies, Collections.EMPTY_LIST);
   }
   
   public Query(String name, String company, List<String> companies, List<String> products) {
      this.companies = Collections.unmodifiableList(companies);
      this.products = Collections.unmodifiableList(products);
      this.company = company;
      this.name = name;
   }

   public String getName() {
      return name;
   }
   
   public String getCompany() {
      return company;
   }
   
   public List<String> getCompanies() {
      return companies;
   }
   
   public List<String> getProducts() {
      return products;
   }
}
