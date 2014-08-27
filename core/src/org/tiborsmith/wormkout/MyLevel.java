package org.tiborsmith.wormkout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by tibor on 31.7.14.
 */
public class MyLevel {
    private Wormkout g;
    public MyLevel(Wormkout game){ this.g = game; }

    private float gateRadius = 1.0f;
    public float gateDistance = gateRadius*0.618f*0.618f;
    private int noRG = 40; // number of rendered gates
    private Array<Vector3> path = new Array<Vector3>(); // array with path for level
    private Color[] gateColors= new Color[42]; // array of colors for rendered gates
    private Vector3 poLRG = new Vector3(0,0,0); //position of last rendered gate
    private int gN = 0; //gate number
    private float startAC = 5.0f; //start animation countdown
    private Vector3 tmpVector = new Vector3(0,0,0); // temporary vector

    public boolean gameOver = false;
    public boolean gameVictory = false;
    public Array<ModelInstance> gates = new Array<ModelInstance>();
    public ModelInstance skybox;


    /**
     * tests if player is still inside gates path
     * @return true if new gate should be loaded
     */
    private boolean collisionTest(){
        gates.get(1+gN).transform.getTranslation(tmpVector).sub(g.player.position);
        if (tmpVector.len2()<gateRadius*gateRadius){
            return true;
        }
        else {
            gates.get(0+gN).transform.getTranslation(tmpVector).sub(g.player.position);
            if (tmpVector.len2()>gateRadius*gateRadius){
                gameOver=true;
            }
            return false;
        }
    }


    /**
     *  updates gate colors according to music spectrum
     * @param delta
     */
    public void updateColors(float delta){
        g.audio.generateColors(delta, gateColors);
        for (int i=7+g.player.speed/5; i< noRG; i++){
            //int j = i+g.player.speed%3;//careful with noRG here
            int j = i;
            gates.get(i).materials.get(0).set(ColorAttribute.createDiffuse(gateColors[j].r,
                    gateColors[j].g,gateColors[j].b,1));
        }
    }


    /**
     * finish is noRG before lastGate
     * @param delta
     */
    public int update(float delta){
        updateColors(delta);
        if (1 < path.size){
            if(collisionTest()){
                addNextGate();
            }
            return noRG+path.size-2;
        }
        else {
            path.clear();
            if (gN < noRG-1){
                if(collisionTest())
                    gN++;
                g.audio.device.setVolume((float)(noRG-gN)/(float)noRG * g.settings.musicVolume);
                startAC = 0.5f;
                return noRG - gN-1;
            }
            else {
                startAC-=delta;
                if (startAC < 0)
                    gameVictory = true;
                return 0;
            }
        }
    }


    /**
     * play three beeps and go from red to orange to green
     * @param delta
     * @return true if still calibrating
     */
    public boolean startingAnimation(float delta) {
        startAC-=delta;
        if (startAC > 0) {
            if (startAC>2.0f) {
                gateColors[0].set(1,0,0,1);
                if (startAC>3.0){
                    gateColors[0].set(Color.BLACK).mul((5.0f-startAC)/2);
                }
                if (startAC>3.0f && startAC<3.1f)
                    g.pDI.say("Three.",g.settings.soundVolume);
                if (startAC<2.1f)
                    g.pDI.say("Two.",g.settings.soundVolume);
            }
            else if (startAC>1.0f){
                gateColors[0].set(Color.ORANGE);
                if (startAC<1.1f)
                    g.pDI.say("One.",g.settings.soundVolume);
            }
            else{
                gateColors[0].set(Color.GREEN);
            }

            for (int i=0; i< noRG; i++){
                gates.get(i).materials.get(0).set(ColorAttribute.createDiffuse(gateColors[0]));
            }
            return true;
        }
        else {
            g.pDI.say("Go.",g.settings.soundVolume);
            return false;
        }
    }


    /**
     * Adds new gate to gates
     */
    private void addGate(){
        ModelInstance gate = new ModelInstance(g.assets.gate);  //makes new model instance
        tmpVector.set(path.get(1)).sub(path.get(0)).nor();  //get direction vector for next gate
        gate.transform.setToRotation(new Vector3(0,0,-1),tmpVector);  //rotates gate according to next gate orientation
        gate.transform.trn(tmpVector.scl(gateDistance));  //translates about gate distance in specified direction
        gate.transform.trn(poLRG);  //adds position of current last gate
        gate.transform.getTranslation(poLRG);  //updates pOLGR
        gate.transform.scl(gateRadius);  //scale gate
        gates.add(gate);  //add gate to gates
        path.removeIndex(0); // removes gate from path
    }


    /**
     * This adds next gate to gates and removes first one from gates
     */
    private void addNextGate(){
        addGate();
        gates.removeIndex(0);
    }


    /**
     * @return true if level loaded
     */
    private boolean loadPath(){
        path.add(new Vector3(0,0,0));
        path.add(new Vector3(0,0,-1));

        FileHandle lvl = Gdx.files.internal(g.levelStates.lvls.get(g.currentLevel).path);
        try {
            InputStream is = lvl.read();
            DataInputStream dis = new DataInputStream(is);
            while(dis.available()>0){
                g.assets.parts.getNextPart(path, dis.readByte());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * load path from file and set certain number of first gates to render
     */
    public void loadLevel(){
        path.clear();
        loadPath();

        //clear gate colors
        for (int i=0; i<gateColors.length; i++){
            gateColors[i] = new Color(Color.GREEN);
        }

        poLRG.set(0,0,0);
        gameOver = false;
        gameVictory = false;
        startAC = 5.0f;
        gN = 0;

        //zero gate
        ModelInstance zeroInstance = new ModelInstance(g.assets.gate);
        gates.add(zeroInstance);

        //load first noRG gates
        for (int i=1; i< noRG; i++){
            addGate();
            gates.get(gates.size-1).materials.get(0).set(ColorAttribute.createDiffuse(gateColors[i-1]));
        }

        skybox = new ModelInstance(g.assets.skybox);
    }


    public void disposeLevel(){
        gates.clear();
    }

}
