package wbl.egr.uri.sensorcollector.fitbit.listeners;

import wbl.egr.uri.sensorcollector.fitbit.events.FBAccelerometerEvent;
import wbl.egr.uri.sensorcollector.fitbit.events.FBHeartRateEvent;

public interface FBHeartRateEventListener {
  void onBandHeartRateChanged(FBHeartRateEvent heartRateEvent);
}
