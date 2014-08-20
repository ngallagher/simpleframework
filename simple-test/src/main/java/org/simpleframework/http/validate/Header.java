package org.simpleframework.http.validate;

import org.simpleframework.xml.util.Entry;

public interface Header extends Entry{        
   public String getValue();  
   public boolean nameMatches(String str);
}

