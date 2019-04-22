package wbl.egr.uri.sensorcollector.fitbit.events;

public class FBHeartRateEvent {
  private double heartRate;

  public FBHeartRateEvent(double heartRate) {
    this.heartRate = heartRate;
  }

  public double getHeartRate() {
    return heartRate;
  }
}
