package org.tiborsmith.wormkout.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import org.tiborsmith.wormkout.steady.MySensors;

/**
 * Created by tibor on 26.7.14.
 */

public class MyAndroidSensors implements MySensors {

    final Context context;
    private SensorManager manager;
    private Input.Orientation nativeOrientation;
    private SensorEventListener gravityListener;
    private SensorEventListener linearAccelerationListener;
    private SensorEventListener rotationVectorListener;

    private final float[] gravityValues = new float[3];
    private final float[] linearAccelerationValues = new float[3];
    private final float[] rotationVectorValues = new float[3];



    public MyAndroidSensors(Context context){
        this.context = context;
    }


    public void registerSensorListeners () {

        nativeOrientation = Gdx.input.getNativeOrientation();

        manager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        Sensor gravitySensor = manager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        gravityListener = new SensorListener(nativeOrientation,gravityValues,linearAccelerationValues,rotationVectorValues);
        manager.registerListener(gravityListener,gravitySensor,SensorManager.SENSOR_DELAY_GAME);

        Sensor linearAccelerationSensor = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        linearAccelerationListener = new SensorListener(nativeOrientation,gravityValues,linearAccelerationValues,rotationVectorValues);
        manager.registerListener(linearAccelerationListener,linearAccelerationSensor,SensorManager.SENSOR_DELAY_GAME);

        Sensor rotationVectorSensor = manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        rotationVectorListener = new SensorListener(nativeOrientation,gravityValues,linearAccelerationValues,rotationVectorValues);
        manager.registerListener(rotationVectorListener,rotationVectorSensor,SensorManager.SENSOR_DELAY_GAME);

    }
    public void unregisterSensorListeners () {
        if (manager != null){
            if (gravityListener != null){
                manager.unregisterListener(gravityListener);
                gravityListener = null;
            }

            if (linearAccelerationListener != null){
                manager.unregisterListener(linearAccelerationListener);
                linearAccelerationListener = null;
            }

            if (rotationVectorListener != null){
                manager.unregisterListener(rotationVectorListener);
                rotationVectorListener = null;
            }

            manager = null;
        }

    }


    public float getGravityX () {
        return gravityValues[0];
    }
    public float getGravityY () {
        return gravityValues[1];
    }
    public float getGravityZ () {
        return gravityValues[2];
    }


    public float getLinearAccelerationX (){
        return linearAccelerationValues[0];
    }
    public float getLinearAccelerationY (){
        return linearAccelerationValues[1];
    }
    public float getLinearAccelerationZ (){
        return linearAccelerationValues[2];
    }


    public float getRotationQuaternion3 (){
        float x = 1 - rotationVectorValues[0] * rotationVectorValues[0]
                - rotationVectorValues[1] * rotationVectorValues[1]
                - rotationVectorValues[2] * rotationVectorValues[2];
        return (x > 0) ?  (float)Math.sqrt(x) : (float)Math.cos(Math.asin(Math.sqrt(1-x)));
    }
    public float getRotationQuaternion0 (){
        return rotationVectorValues[0];
    }
    public float getRotationQuaternion1 (){
        return rotationVectorValues[1];
    }
    public float getRotationQuaternion2 (){
        return rotationVectorValues[2];
    }


    private class SensorListener implements SensorEventListener {
        final Input.Orientation nativeOrientation;
        final float[] gravityValues;
        final float[] linearAccelerationValues;
        final float[] rotationVectorValues;


        SensorListener (Input.Orientation nativeOrientation, float[] gravityValues, float[] linearAccelerationValues, float[] rotationVectorValues){
            this.nativeOrientation = nativeOrientation;
            this.gravityValues = gravityValues;
            this.linearAccelerationValues = linearAccelerationValues;
            this.rotationVectorValues = rotationVectorValues;
        }

        @Override
        public void onAccuracyChanged (Sensor arg0, int arg1) {
        }

        @Override
        public void onSensorChanged (SensorEvent event) {

            if (nativeOrientation == Input.Orientation.Portrait){
                if (event.sensor.getType() == Sensor.TYPE_GRAVITY){
                      System.arraycopy(event.values,0,gravityValues,0,gravityValues.length);
                }

                if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
                    System.arraycopy(event.values,0,linearAccelerationValues,0,linearAccelerationValues.length);
                }

                if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
                    System.arraycopy(event.values,0,rotationVectorValues,0,rotationVectorValues.length);
                }
            } else {
                if (event.sensor.getType() == Sensor.TYPE_GRAVITY){
                    gravityValues[0] = event.values[1];
                    gravityValues[1] = -event.values[0];
                    gravityValues[2] = event.values[2];
                }

                if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
                    linearAccelerationValues[0] = event.values[1];
                    linearAccelerationValues[1] = -event.values[0];
                    linearAccelerationValues[2] = event.values[2];
                }

                if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
                    rotationVectorValues[0] = event.values[1];
                    rotationVectorValues[1] = -event.values[0];
                    rotationVectorValues[2] = event.values[2];
                    // 3 is optional value... doesn't work well
                   // rotationVectorValues[3] = event.values[3];
                }
            }
        }
    }
}
