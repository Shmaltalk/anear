package wbl.egr.uri.sensorcollector.fitbit;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.app.Service;
import wbl.egr.uri.sensorcollector.collector_server.CollectorServer;

public class FBClient {
  private FBInfo info;
  private Service service;
  private CollectorServer server;
  private FBSensorManager sensManager;
  private FBNotificationManager notifManager;

  public FBClient(Service serv, FBInfo myInfo, CollectorServer server) {
    info = myInfo;
    service = serv;
    this.server = server;
    sensManager = new FBSensorManager(server);
    notifManager = new FBNotificationManager(server);
  }

  public void connect(){
    if(!server.isAlive()) {
      try {
        server.start();
      } catch (Exception e) {
        System.out.println("ERROR: Server failed to start: " + e);
      }
    }
  }

  public void disconnect() {
    if(server.isAlive()) {
      try {
        server.stop();
      } catch (Exception e) {
        System.out.println("ERROR: Server failed to stop: " + e);
      }
    }
  }

  public boolean isConnected() {
    return server.isAlive();
  }

  public FBSensorManager getSensorManager() {
    return sensManager;
  }

  public FBNotificationManager getNotificationManager() {
    return notifManager;
  }
}
