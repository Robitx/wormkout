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

/**
 * Created by tibor on 31.7.14.
 */
public class MyLevel {
    private Wormkout game;
    public MyLevel(Wormkout game){ this.game = game; }

    private float gateRadius = 1.0f;
    private float gateDistance = gateRadius*0.618f*0.618f;
    private int noRG = 25; // number of rendered gates
    private Array<Vector3> path = new Array<Vector3>(); // array with path for level
    private Color[] gateColors= new Color[32]; // array of colors for rendered gates
    private Vector3 poLRG = new Vector3(0,0,0); //position of last rendered gate
    private int noLRG = 0; //number of last rendered gate
    private float victoryAC = 5.0f; //victory animation countdown
    private float startAC = 5.0f; //start animation countdown
    private Vector3 tmpVector = new Vector3(0,0,0); // temporary vector

    public boolean gameOver = false;
    public boolean gameVictory = false;
    public Array<ModelInstance> gates = new Array<ModelInstance>();


    /**
     * tests if player is still inside gates path
     * @return true if new gate should be loaded
     */
    private boolean collisionTest(){
        gates.get(1).transform.getTranslation(tmpVector).sub(game.myPlayer.position);
        if (tmpVector.len2()<gateRadius*gateRadius){
            return true;
        }
        else {
            gates.get(0).transform.getTranslation(tmpVector).sub(game.myPlayer.position);
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
        game.myAudio.generateColors(delta, gateColors);
        for (int i=0; i< noRG; i++){
            gates.get(i).materials.get(0).set(ColorAttribute.createDiffuse(gateColors[i].r,gateColors[i].g,gateColors[i].b,1));
            //gates.get(i).transform.scl(1+gateColors[i].a);
        }
    }


    /**
     * finish is noRG before lastGate
     * @param delta
     */
    public void update(float delta){
        if (noLRG < path.size)
        {
            if(collisionTest()){
                addNextGate();
            }
            updateColors(delta);
        }
        else{
            victoryAnimation(delta);
        }
    }


    /**
     * animates gate colors, lowers music volume to zero
     * @param delta
     */
    private void victoryAnimation(float delta) {
        victoryAC-=delta;
        if (victoryAC > 0) {
            // gradually lowers volume to zero
            game.myAudio.device.setVolume(victoryAC/5.0f*game.myState.mySettings.musicVolume);

            // white and yellow gradually going to black
            for (int i=0; i< noRG; i++){
                gateColors[0].set(Color.WHITE).mul(victoryAC/5.0f);
                if ((int)(victoryAC/0.2f)%2 == 1){
                    gateColors[0].set(Color.YELLOW).mul(victoryAC/5.0f);
                }
                gates.get(i).materials.get(0).set(ColorAttribute.createDiffuse(gateColors[0]));
            }
        } else {
            gameVictory = true;
            for (int i=0; i< noRG; i++){
                gateColors[0].set(0,0,0,0);
                gates.get(i).materials.get(0).set(ColorAttribute.createDiffuse(gateColors[0]));
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
                gateColors[0].set((5.0f-startAC)/3.0f,0,0,1);
            }
            else if (startAC>1.0f){
                gateColors[0].set(Color.ORANGE);
            }
            else{
                gateColors[0].set(Color.GREEN);
            }

            for (int i=0; i< noRG; i++){
                gates.get(i).materials.get(0).set(ColorAttribute.createDiffuse(gateColors[0]));
            }
            return true;
        }
        else return false;
    }


    /**
     * Adds new gate to gates
     */
    private void addGate(){
        ModelInstance gate = new ModelInstance(game.myAssets.gate);  //makes new model instance
        tmpVector.set(path.get(noLRG)).sub(path.get(noLRG-1)).nor();  //get direction vector for next gate
        gate.transform.setToRotation(new Vector3(0,0,-1),tmpVector);  //rotates gate according to next gate orientation
        gate.transform.trn(tmpVector.scl(gateDistance));  //translates about gate distance in specified direction
        gate.transform.trn(poLRG);  //adds position of current last gate
        gate.transform.getTranslation(poLRG);  //updates pOLGR
        noLRG++;  //shifts marker of LRG
        gate.transform.scl(gateRadius);  //scale gate
        gates.add(gate);  //add gate to gates
    }


    /**
     * This adds next gate to gates and removes first one from gates
     */
    private void addNextGate(){
        addGate();
        gates.removeIndex(0);
    }


    /**
     * later loading from file and so on
     * @return true if level loaded
     */
    private boolean loadPath(){
        FileHandle file = Gdx.files.internal(game.myState.levelStates.lvls.get(game.myState.currentLevel).fileName);
        try {
            DataInputStream dis = new DataInputStream(file.read());
            while(dis.available()>0)
            {
                path.add(new Vector3(dis.readFloat(),dis.readFloat(),dis.readFloat()));
            }
        } catch (IOException e) {//e.printStackTrace();
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
        for (int i=0; i<32; i++){
            gateColors[i] = new Color(Color.WHITE);
        }

        poLRG.set(0,0,0);
        noLRG = 0;
        //currentGate=0;
        gameOver = false;
        gameVictory = false;
        victoryAC = 5.0f;
        startAC = 5.0f;

        //zero gate
        ModelInstance zeroInstance = new ModelInstance(game.myAssets.gate);
        gates.add(zeroInstance);
        noLRG++;

        //load first noRG gates
        for (int i=1; i< noRG; i++){
            addGate();
        }
    }


    public void disposeLevel(){
        gates.clear();
    }

}
