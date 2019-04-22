package wbl.egr.uri.sensorcollector.fitbit;

import android.app.Activity;
import android.app.Service;
import wbl.egr.uri.sensorcollector.collector_server.CollectorServer;

import java.util.ArrayList;
import java.util.List;

public class FBClientManager {
  private static FBClientManager instance = null;
  List<FBInfo> bandsConnected;
  private CollectorServer server;

  public static FBClientManager getInstance() {
    if(instance == null) {
      instance = new FBClientManager();
    }
    return instance;
  }

  private FBClientManager() {
    bandsConnected = new ArrayList<>();
    server = new CollectorServer();
    try {
      server.start();
    } catch (Exception e) {
      System.out.println("Server failed to start: " + e);
    }
  }

  public List<FBInfo> getConnectedBands() {
    return new ArrayList<>(bandsConnected);
  }

  public FBClient create(Service serv, FBInfo info) {
    return new FBClient(serv, info, server);
  }
}
