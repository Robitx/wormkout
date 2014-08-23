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
        lvls.add(getNewLevelInstance(false,false,3600,"Level 0","levels/level0.bin",
                "Quick for testing"));
        lvls.add(getNewLevelInstance(true,false,3600,"Level 1","levels/level1.bin",
                "Tutorial level."));
 
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
        public String path;
        public String description;

        public void set(boolean locked, boolean finished, float bestTime, String name, String path, String description){
            this.locked = locked;
            this.finished = finished;
            this.bestTime = bestTime;
            this.name = name;
            this.path = path;
            this.description = description;
        }
    }


    public MyLevelState getNewLevelInstance(boolean locked, boolean finished, float bestTime, String name, String path, String description){
        MyLevelState lvl= new MyLevelState();
        lvl.locked = locked;
        lvl.finished = finished;
        lvl.bestTime = bestTime;
        lvl.name = name;
        lvl.path = path;
        lvl.description = description;
        return lvl;
    }


    public MyLevelState getNewLevelInstance(){
        return new MyLevelState();
    }
}




