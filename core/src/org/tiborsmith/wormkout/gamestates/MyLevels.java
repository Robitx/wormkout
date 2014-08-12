package org.tiborsmith.wormkout.gamestates;

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
        lvls.get(0).set(false,false,3600,"Level 1","levels/Level_1.bin","Only horizontal, few minutes long." +
                " Straight path, good for seeing music visualization");
        lvls.add(getNewLevelInstance());
        lvls.get(1).set(false,false,3600,"Level 2","levels/Level_2.bin","Only horizontal, few minutes long." +
                " First non trivial path.");
        lvls.add(getNewLevelInstance());
        lvls.get(2).set(true,false,3600,"Level 3","levels/Level_3.bin","Not yet...");

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
        public String fileName;
        public String description;

        public void set(boolean locked, boolean finished, float bestTime, String name, String fileName, String description){
            this.locked = locked;
            this.finished = finished;
            this.bestTime = bestTime;
            this.name = name;
            this.fileName = fileName;
            this.description = description;
        }
    }


    public MyLevelState getNewLevelInstance(){
        return new MyLevelState();
    }
}




