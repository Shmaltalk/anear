package wbl.egr.uri.sensorcollector.fitbit.events;

public class FBHeartRateEvent {
  private double heartRate;
  private int timestamp;

  public FBHeartRateEvent(double heartRate, int timestamp) {
    this.heartRate = heartRate;
    this.timestamp = timestamp;
  }

  public double getHeartRate() { return heartRate; }
  public int getTimestamp() { return timestamp; }
}
