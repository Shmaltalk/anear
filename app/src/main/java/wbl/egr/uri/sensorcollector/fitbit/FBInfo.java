package wbl.egr.uri.sensorcollector.fitbit;

public class FBInfo {
  private String addr;
  private String name;

  public FBInfo(String addr, String name) {
    this.addr = addr;
    this.name = name;
  }

  public String getName() {
    return name;
  }
  public String getMacAddress() {
    return addr;
  }
}
