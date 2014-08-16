package org.simpleframework.demo.table.extract;

import java.io.File;

public class FragmentColumnStyle implements ColumnStyle {

   private final ValueEncoder encoder;
   private final Fragment fragment;
   private final String caption;
   private final String name;
   private final boolean sortable;
   private final boolean resizable;

   public FragmentColumnStyle(File fragment, String name, String caption) {
      this(fragment, name, caption, true, false);
   }
   
   public FragmentColumnStyle(File fragment, String name, String caption, boolean resizable, boolean sortable) {
      this.fragment = new Fragment(fragment);
      this.encoder = new ValueEncoder();
      this.resizable = resizable;
      this.sortable = sortable;
      this.caption = caption;  
      this.name = name;
   }
   
   @Override
   public String getStyle() {
      String template = fragment.getFragment();
      String title = encoder.encode(caption);
      
      return String.format("%s,%s,%s,%s,%s,%s", name, title, template, resizable, sortable);
   }
}
