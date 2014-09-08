package org.tiborsmith.wormkout.steady;


import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import org.tiborsmith.wormkout.Wormkout;

/**
 * Created by tibor on 28.7.14.
 */

public class MySensorProcessing {
    Wormkout g;
    public MySensorProcessing(Wormkout game){this.g = game;}

    public final Vector3 calibrationVector = new Vector3(0,0,1);
    public final Vector3 orientationVector = new Vector3(0,0,1);
    public boolean newCalibration = true;

    final private Vector2 tmp1 = new Vector2(), tmp2 = new Vector2();

    /**
     *  maybe will have to be more complicated - calibration over time interval
     */
    public void calibrate(){
        if (newCalibration){
            getOrientationVector(calibrationVector);
            newCalibration = false;
        }
        else {
            getOrientationVector(orientationVector);
            calibrationVector.add(orientationVector).scl(0.5f).nor();
        }
    }


    /**
     * (0,0,1) in the device coordinate system is rotated by quaternion into World coordinate system
     */
    public void getOrientationVector(Vector3 orientationVector){
        float x,y,z,w;
        w = g.mySensors.getRotationQuaternion3();
        x = g.mySensors.getRotationQuaternion0();
        y = g.mySensors.getRotationQuaternion1();
        z = g.mySensors.getRotationQuaternion2();

        orientationVector.set(2.0f * (z * x + w * y), 2.0f * (z * y - w * x), w * w + z * z - x * x - y * y);
    }


    public float getPhi(){
        getOrientationVector(orientationVector);
        tmp1.set(orientationVector.x,orientationVector.y).nor();
        tmp2.set(calibrationVector.x,calibrationVector.y).nor();
        //in order to avoid NaN from acos
        float x = MathUtils.clamp(tmp1.dot(tmp2), -1.0f, 1.0f);
        return (tmp1.x*tmp2.y>tmp1.y*tmp2.x)? (float)Math.acos(x) : -(float)Math.acos(x);
    }


    public float getTheta(){
        getOrientationVector(orientationVector);
        tmp1.set((float)Math.sqrt(orientationVector.x*orientationVector.x+orientationVector.y*orientationVector.y),orientationVector.z).nor();
        tmp2.set((float)Math.sqrt(calibrationVector.x*calibrationVector.x+calibrationVector.y*calibrationVector.y),calibrationVector.z).nor();
        //in order to avoid NaN from acos
        float x = MathUtils.clamp(tmp1.dot(tmp2), -1.0f, 1.0f);
        x = (tmp1.x*tmp2.y>tmp1.y*tmp2.x)? (float)Math.acos(x) : -(float)Math.acos(x);
        //maximum angle is +- PI/2.25 from calibrated direction
        return MathUtils.clamp(x, -(float)Math.PI/2.025f, (float)Math.PI/2.025f);
    }


    //return true if gravity on Z is small enough
    public boolean isVertical(){
        if (g.mySensors.getGravityZ()*g.mySensors.getGravityZ()*1.5<
                g.mySensors.getGravityY()*g.mySensors.getGravityY()+
                        g.mySensors.getGravityX()*g.mySensors.getGravityX())
            return true;
        return false;
    }


}
