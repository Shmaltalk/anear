package wbl.egr.uri.sensorcollector.fitbit.events;

public class FBAccelerometerEvent {
  private double accX, accY, accZ;

  public FBAccelerometerEvent(double ax, double ay, double az) {
    accX = ax;
    accY = ay;
    accZ = az;
  }

  public double getAccelerationX() {
    return accX;
  }

  public double getAccelerationY() {
    return accY;
  }

  public double getAccelerationZ() {
    return accZ;
  }
}
