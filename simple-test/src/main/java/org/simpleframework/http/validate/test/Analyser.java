package org.simpleframework.http.validate.test;

import org.simpleframework.common.KeyMap;
import org.simpleframework.common.buffer.Buffer;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.StatusLine;

public interface Analyser {
   public void compose(StringBuilder target, KeyMap<String> header, Buffer body) throws Exception;
   public void handle(Request request, Response response) throws Exception;
   public void analyse(StatusLine status, KeyMap<String> header, Buffer body) throws Exception;
}
