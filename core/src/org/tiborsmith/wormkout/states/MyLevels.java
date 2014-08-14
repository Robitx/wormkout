package org.tiborsmith.wormkout.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

/**
 * Created by tibor on 10.8.14.
 */
public class MyLevels {

    public Array<MyLevelState> lvls;

    public MyLevels(){
        lvls = new Array<MyLevelState>();
    }


    /**
     * Saves current instance of this class into file
     * later add encryption
     */
    public void saveLevelProgress(){
        FileHandle levelFile = Gdx.files.local("levelStates.json");
        Json json = new Json();
        levelFile.writeString(json.prettyPrint(this), false);
    }


    /**
     * Makes default version of Level states and saves it
     * later add encryption
     */
    public void makeDefault(){
        lvls.clear();
        lvls.add(getNewLevelInstance());
        lvls.get(0).set(false,false,3600,"Level 1",new byte[]{4,2,5,0,9,9,0,9,9,9,9,5,2,2,6,5,5,5},
                "Tutorial level.");
        lvls.add(getNewLevelInstance());
        lvls.get(1).set(false,false,3600,"Level 2",new byte[]{4,7,5,0,9,9,0,0,0,0,0,5,2,2,6,5,5,5,0,9,9,0,9,9,9,9,5,2,2,6,5,5,5},"Blabla");
        lvls.add(getNewLevelInstance());
        lvls.get(2).set(true,false,3600,"Level 3",new byte[]{4,8,5,0,9,9,0,5,5,5,},"Not yet...");

        saveLevelProgress();
    }


    /**
     * Nested class with information about single level
     */
    public static class MyLevelState{
        public boolean locked;
        public boolean finished;
        public float bestTime;
        public String name;
        public byte[] path;  // maybe make transient and store somewhere else instead
        public String description;

        public void set(boolean locked, boolean finished, float bestTime, String name, byte[] path, String description){
            this.locked = locked;
            this.finished = finished;
            this.bestTime = bestTime;
            this.name = name;
            this.path = path;
            this.description = description;
        }
    }


    public MyLevelState getNewLevelInstance(){
        return new MyLevelState();
    }
}




