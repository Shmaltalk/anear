package wbl.egr.uri.sensorcollector.fitbit;

import android.app.Activity;
import android.app.Service;
import wbl.egr.uri.sensorcollector.collector_server.CollectorServer;

import java.util.ArrayList;
import java.util.List;

public class FBClientManager {
  private static FBClientManager instance = null;
  List<FBInfo> bandsConnected;

  public static FBClientManager getInstance() {
    if(instance == null) {
      instance = new FBClientManager();
    }
    return instance;
  }

  private FBClientManager() {
    bandsConnected = new ArrayList<>();
  }

  public List<FBInfo> getConnectedBands() {
    return new ArrayList<>(bandsConnected);
  }

  public void addBand() {
    bandsConnected.add(new FBInfo("Mac Addr NYI", "Name NYI"));
  }

  public FBClient create(FBInfo info) {
    return new FBClient(info);
  }
}
