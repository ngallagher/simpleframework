package org.simpleframework.http.validate.test;

import org.simpleframework.common.KeyMap;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.http.Request;
import org.simpleframework.http.RequestWrapper;
import org.simpleframework.http.Response;
import org.simpleframework.http.ResponseWrapper;
import org.simpleframework.http.StatusLine;

public class FilterAnalyser implements Analyser {
   
   private final Analyser analyser;
   
   public FilterAnalyser(Analyser analyser) {
      this.analyser = analyser;
   }

   public void analyse(StatusLine status, KeyMap<String> header, Buffer body) throws Exception {
      analyser.analyse(status, header, body);
   }

   public void compose(StringBuilder target, KeyMap<String> header, Buffer body) throws Exception {
      analyser.compose(target, header, body);
   }

   public void handle(Request request, Response response) throws Exception {
      analyser.handle(new RequestWrapper(request), new ResponseWrapper(response));
   }
}