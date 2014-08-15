package org.simpleframework.http.core;

import junit.framework.TestCase;

import org.simpleframework.http.Query;
import org.simpleframework.http.message.MockBody;
import org.simpleframework.http.message.MockHeader;

public class QueryBuilderTest extends TestCase{
   
   public void testBuilder() throws Exception {
      MockRequest request = new MockRequest();
      
      request.setContentType("application/x-www-form-urlencoded");
      request.setContent("a=post_A&c=post_C&e=post_E");
      
      MockBody body = new MockBody();
      MockHeader header = new MockHeader("/path?a=query_A&b=query_B&c=query_C&d=query_D");
      MockEntity entity = new MockEntity(body, header);
      QueryBuilder builder = new QueryBuilder(request, entity);

      Query form = builder.build();
      
      assertEquals(form.getAll("a").size(), 2);
      assertEquals(form.getAll("b").size(), 1);
      assertEquals(form.getAll("c").size(), 2);
      assertEquals(form.getAll("e").size(), 1);  
      
      assertEquals(form.get("a"), "query_A");
      assertEquals(form.get("b"), "query_B");
      assertEquals(form.get("c"), "query_C");
      assertEquals(form.get("e"), "post_E");   
   }

}
