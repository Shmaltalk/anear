package wbl.egr.uri.sensorcollector.fitbit;

import wbl.egr.uri.sensorcollector.collector_server.CollectorServer;
import wbl.egr.uri.sensorcollector.fitbit.events.FBAccelerometerEvent;
import wbl.egr.uri.sensorcollector.fitbit.listeners.FBAccelerometerEventListener;
import wbl.egr.uri.sensorcollector.fitbit.listeners.FBHeartRateEventListener;

public class FBSensorManager {
  private FBHeartRateEventListener hrListener;
  private FBAccelerometerEventListener accListener;
  private boolean stopped;

  public FBSensorManager() {
    stopped = false;
  }

  public void registerHeartRateEventListener(FBHeartRateEventListener ls) {
    hrListener = ls;
  }

  public void registerAccelerometerEventListener(FBAccelerometerEventListener ls) {
    accListener = ls;
  }

  public void stop() {
    stopped = true;
  }

  public void start() {
    stopped = false;
  }

  public FBHeartRateEventListener getHeartRateEventListener() {
    if (stopped) {
      return null;
    }
    return hrListener;
  }
}
