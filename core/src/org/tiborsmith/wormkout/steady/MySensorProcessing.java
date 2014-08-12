package org.tiborsmith.wormkout.steady;


import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import org.tiborsmith.wormkout.Wormkout;

/**
 * Created by tibor on 28.7.14.
 */

public class MySensorProcessing {
    Wormkout game;
    public MySensorProcessing(Wormkout game){this.game = game;}

    final private Vector3 calibrationVector = new Vector3(0,0,1);
    final private Vector3 orientationVector = new Vector3(0,0,1);

    final private Vector2 tmp1 = new Vector2(), tmp2 = new Vector2();

    /**
     *  maybe will have to be more complicated - calibration over time interval
     */
    public void calibrate(){
        getOrientationVector(calibrationVector);
    }


    /**
     * (0,0,1) in the device coordinate system is rotated by quaternion into World coordinate system
     */
    public void getOrientationVector(Vector3 orientationVector){
        float x,y,z,w;
        w = game.mySensors.getRotationQuaternion3();
        x = game.mySensors.getRotationQuaternion0();
        y = game.mySensors.getRotationQuaternion1();
        z = game.mySensors.getRotationQuaternion2();

        orientationVector.set(2.0f * (z * x + w * y), 2.0f * (z * y - w * x), w * w + z * z - x * x - y * y);
    }


    public float getPhi(){
        getOrientationVector(orientationVector);
        tmp1.set(orientationVector.x,orientationVector.y).nor();
        tmp2.set(calibrationVector.x,calibrationVector.y).nor();
        //in order to avoid NaN from acos
        float x = MathUtils.clamp(tmp1.dot(tmp2), -0.99999f, 0.99999f);
        return (tmp1.x*tmp2.y>tmp1.y*tmp2.x)? (float)Math.acos(x) : -(float)Math.acos(x);
    }


    public float getTheta(){
        getOrientationVector(orientationVector);
        tmp1.set((float)Math.sqrt(orientationVector.x*orientationVector.x+orientationVector.y*orientationVector.y),orientationVector.z).nor();
        tmp2.set((float)Math.sqrt(calibrationVector.x*calibrationVector.x+calibrationVector.y*calibrationVector.y),calibrationVector.z).nor();
        //in order to avoid NaN from acos
        float x = MathUtils.clamp(tmp1.dot(tmp2), -0.99999f, 0.99999f);
        x = (tmp1.x*tmp2.y>tmp1.y*tmp2.x)? (float)Math.acos(x) : -(float)Math.acos(x);
        //maximum angle is +- 60 degrees from calibrated direction
        return MathUtils.clamp(x, -(float)Math.PI/3, (float)Math.PI/3);
    }



}
