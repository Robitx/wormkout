package org.tiborsmith.wormkout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by tibor on 14.8.14.
 */
public class MyLevelParts {
    private MyElement[] element = new MyElement[10];
    private Vector3 tmpV = new Vector3();
    private Vector3 tmpLV = new Vector3();
    private Quaternion tmpQ = new Quaternion();

    /**
     * @param path pointer to array with path is taken and element is added to it
     * @param i number of element which is added
     */
    public void getNextPart(Array<Vector3> path,final int i){
        tmpV.set(path.get(path.size-1)).sub(path.get(path.size-2)).nor();  //direction vector for last gate in path
        tmpQ.setFromCross(new Vector3(0,0,-1),tmpV);  // calculate rotation quaternion
        tmpLV.set(path.get(path.size-1));  // current last vector from path
        for (int j=0; j < element[i].array.size; j++){
            tmpV.set(element[i].array.get(j));  // take vector from element which I am adding to path
            tmpV.mul(tmpQ); // rotate it into proper direction
            tmpV.add(tmpLV); // translate it by adding last vector from path before expanding
            path.add(new Vector3());
            path.get(path.size-1).set(tmpV);// add adjusted vector to path
        }
    }

    public void loadElements(){
        for (int i=0; i < element.length; i++) {
            FileHandle file = Gdx.files.internal("levelparts/"+i+".bin");
            element[i] = new MyElement();
            try {
                DataInputStream dis = new DataInputStream(file.read());
                while (dis.available() > 0) {
                    element[i].array.add(new Vector3(dis.readFloat(), dis.readFloat(), dis.readFloat()));
                }
            } catch (IOException e) {/*e.printStackTrace();*/}
        }
    }

    public void dispose(){
        for (int i=0; i < element.length; i++) {
            element[i].array.clear();
        }
    }

    private static class MyElement{
        private Array<Vector3> array = new Array<Vector3>();
    }
}
