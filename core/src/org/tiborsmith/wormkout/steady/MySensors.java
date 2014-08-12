package org.tiborsmith.wormkout.steady;

/**
 * Created by tibor on 26.7.14.
 * Abstract interface for sensors, since libgdx version is inadequate
 */
public interface MySensors {
    public void registerSensorListeners ();
    public void unregisterSensorListeners ();

    public float getGravityX ();
    public float getGravityY ();
    public float getGravityZ ();

    public float getLinearAccelerationX ();
    public float getLinearAccelerationY ();
    public float getLinearAccelerationZ ();


    public float getRotationQuaternion0 ();
    public float getRotationQuaternion1 ();
    public float getRotationQuaternion2 ();
    public float getRotationQuaternion3 ();

}
