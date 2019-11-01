package wbl.egr.uri.sensorcollector.fitbit;

import wbl.egr.uri.sensorcollector.collector_server.CollectorServer;
import wbl.egr.uri.sensorcollector.fitbit.listeners.FBEventListener;

public class FBSensorManager {
  private FBEventListener listener;
  private boolean stopped;

  public FBSensorManager() {
    stopped = false;
  }

  public void registerEventListener(FBEventListener ls) {
    listener = ls;
  }

  public void stop() {
    stopped = true;
  }

  public void start() {
    stopped = false;
  }

  public FBEventListener getEventListener() {
    return listener;
  }
}
