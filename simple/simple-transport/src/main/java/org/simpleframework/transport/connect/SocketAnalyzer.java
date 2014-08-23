/*
 * SocketAnalyzer.java February 2012
 *
 * Copyright (C) 2007, Niall Gallagher <niallg@users.sf.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package org.simpleframework.transport.connect;

import java.nio.channels.SelectableChannel;

import org.simpleframework.transport.trace.TraceAnalyzer;
import org.simpleframework.transport.trace.Trace;

/**
 * The <code>SocketAnalyzer</code> is used to wrap an analyzer object. 
 * Wrapping an analyzer in this way ensures that even if the analyzer 
 * is badly written there is little chance that it will affect the
 * operation of the server. All <code>Trace</code> objects returned
 * from this will catch all exceptions within the created trace.
 * 
 * @author Niall Gallagher
 */
class SocketAnalyzer implements TraceAnalyzer {
   
   /**
    * This is the analyzer that is used to create the trace objects.
    */
   private final TraceAnalyzer analyzer;
   
   /**
    * Constructor for the <code>SocketAnalyzer</code> object. This will
    * be given the analyzer that is to be used to create traces. This 
    * can be a null value, in which case the trace provided will be
    * a simple empty void that swallows all trace events.
    * 
    * @param analyzer the analyzer that is to be wrapped by this
    */
   public SocketAnalyzer(TraceAnalyzer analyzer) {
      this.analyzer = analyzer;
   }
 
   /**
    * This method is used to attach a trace to the specified channel.
    * Attaching a trace basically means associating events from that
    * trace with the specified socket. It ensures that the events 
    * from a specific channel can be observed in isolation.
    * 
    * @param channel this is the channel to associate with the trace
    * 
    * @return this returns a trace associated with the channel
    */
   public Trace attach(SelectableChannel channel) {
      Trace trace = null;
      
      if(analyzer != null) {
         trace = analyzer.attach(channel);
      }
      return new SocketTrace(trace);
   }
   
   /**
    * This is used to stop the analyzer and clear all trace information.
    * Stopping the analyzer is typically done when the server is stopped
    * and is used to free any resources associated with the analyzer. If
    * an analyzer does not hold information this method can be ignored.
    */
   public void stop() {
      if(analyzer != null) {
         analyzer.stop();
      }
   }
}