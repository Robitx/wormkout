package org.tiborsmith.wormkout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by tibor on 30.7.14.
 */
public class MyPlayer {
    private Wormkout game;
    public MyPlayer (Wormkout game){
        this.game = game;
    }

    public PerspectiveCamera cam = null;
    public final Vector3 position = new Vector3(0,0,0);
    public final Vector3 direction = new Vector3(0,0,-1);
    public float speed = 2.0f;


    private float phi = 0, theta = 0;


    public void getDirection(){
        phi = game.mySensorProcessing.getPhi();
        theta = game.mySensorProcessing.getTheta();


        if (!(Float.isNaN(phi) || Float.isNaN(theta))){
            direction.y=(float)Math.sin(theta);
            direction.z=(-1.0f)*(float)(Math.cos(theta)*Math.cos(phi));
            direction.x=(float)(Math.cos(theta)*Math.sin(phi));
            direction.nor();
        }
        else {
            Gdx.app.log("NaN","phi or theta have been NaN");
        }

    }

    public void updateCam(float delta){
        getDirection();
        position.mulAdd(direction, delta * speed);
        cam.position.set(position);
        cam.lookAt(position.x+direction.x, position.y+direction.y, position.z+direction.z);
        cam.up.set(0,1,0);
        cam.update();
    }


    public void setCam(){
        position.set(0, 0, 0);
        direction.set(0,0,-1);

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        cam.position.set(position);
        cam.lookAt(position.x+direction.x, position.y+direction.y, position.z+direction.z);
        cam.up.set(0,1,0);
        cam.near = 0.1f;
        cam.far = 1000f;
        cam.update();
    }
}
