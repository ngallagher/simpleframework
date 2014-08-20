package org.simpleframework.http.validate.test;

import org.simpleframework.http.Status;
import org.simpleframework.http.StatusLine;

class ResponseStatus implements StatusLine {
   
   private int code;
   private String text;
   private int major;
   private int minor;
   public int getCode() {
      return code;
   }
   public void setCode(int code) {
      this.code = code;
   }
   public String getDescription() {
      return text;
   }
   public void setDescription(String text) {
      this.text = text;
   }
   public int getMajor() {
      return major;
   }
   public void setMajor(int major) {
      this.major = major;
   }
   public int getMinor() {
      return minor;
   }
   public void setMinor(int minor) {
      this.minor = minor;
   }
   public Status getStatus() {
      return Status.getStatus(code);
   }
   public void setStatus(Status status) {
      this.code = status.getCode();
      this.text = status.getDescription();
   }
}
