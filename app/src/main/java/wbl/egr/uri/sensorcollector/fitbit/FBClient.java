package wbl.egr.uri.sensorcollector.fitbit;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.app.Service;
import wbl.egr.uri.sensorcollector.collector_server.CollectorServer;

public class FBClient {
  private FBInfo info;
  //private Service service;
  private FBSensorManager sensManager;
  private FBNotificationManager notifManager;

  public FBClient(FBInfo myInfo) {
    info = myInfo;
    //service = serv;
    sensManager = new FBSensorManager();
    notifManager = new FBNotificationManager();
  }

  public FBSensorManager getSensorManager() {
    return sensManager;
  }

  public FBNotificationManager getNotificationManager() {
    return notifManager;
  }
}
