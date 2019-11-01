package wbl.egr.uri.sensorcollector.fitbit.listeners;

import wbl.egr.uri.sensorcollector.fitbit.events.FBEvent;

public interface FBEventListener {
  void onBandUpdate(FBEvent event);
}
