package org.simpleframework.http.validate.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Scenario {
   public int requests();
   public int concurrency();
   public Protocol protocol() default Protocol.HTTP;
   public Method method() default Method.GET;
   public boolean debug() default false;
   public boolean threadDump() default false;
}
