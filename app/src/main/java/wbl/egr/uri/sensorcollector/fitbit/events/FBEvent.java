package wbl.egr.uri.sensorcollector.fitbit.events;
import com.google.gson.JsonObject;

public class FBEvent {
  private double heartRate;
  private int timestamp;
  private double accX, accY, accZ;
  private boolean contact;

  public FBEvent(JsonObject obj) {
    this.heartRate = obj.get("heartrate").getAsDouble();
    this.timestamp = obj.get("timestamp").getAsInt();
    this.accX = obj.get("accX").getAsDouble();
    this.accY = obj.get("accY").getAsDouble();
    this.accZ = obj.get("accZ").getAsDouble();
    this.contact = obj.get("contact").getAsBoolean();
  }

  public double getHeartRate() { return heartRate; }
  public int getTimestamp() { return timestamp; }
  public double getAccX() { return accX; }
  public double getAccY() { return accY; }
  public double getAccZ() { return accZ; }
  public boolean getContact() { return contact; }
}
