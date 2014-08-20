package org.simpleframework.http.validate;

public interface Measurement {
   
   public void responseDuration(int count, long duration);
   public void threadWaiting();
   public void threadRunning();
   public void sentRequest(int count);
   public void receivedResponse(int count);   
   public void errorOccured();
   public void bytesTransferred(long byteCount);
   public void connectionEstablished();
   public void connectionTerminated();
}
