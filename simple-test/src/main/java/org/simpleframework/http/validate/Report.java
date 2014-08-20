package org.simpleframework.http.validate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementMap;

public class Report {
   
   @ElementMap(attribute=true, key="name")
   private Map<String, String> header;
   
   @Element(data=true)
   private String body;
   
   @Element(required=false)
   private String nextStatus;
   
   @Attribute
   private String statusDescription;
   
   @Attribute
   private int statusCode;
   
   @Attribute
   private int major; 
   
   @Attribute
   private int minor; 
   
   @ElementList(required=false)
   private List<String> errors;
   
   private Report() {
      super();
   }

   public Report(Result response, String nextStatus) throws IOException {
      this.errors = new ArrayList<String>();
      this.statusDescription = response.getStatusLine().getDescription();
      this.statusCode = response.getStatusLine().getCode();
      this.major = response.getStatusLine().getMajor();
      this.minor = response.getStatusLine().getMinor();
      this.header = response.getHeader();
      this.body = response.getContent();
      this.nextStatus = nextStatus;
   }
   
   public String getStatusLine() {
      return String.format("HTTP/%s.%s %s %s", major, minor, statusCode, statusDescription);
   }
   
   public boolean isError() {
      return errors.size() > 0;
   }
   
   public void addError(String error) {
	   errors.add(error);
   }
   
   public void addException(Exception e) {
      errors.add(e.getMessage());
   }
}
