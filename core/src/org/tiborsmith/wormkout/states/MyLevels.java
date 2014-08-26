package org.tiborsmith.wormkout.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

import org.tiborsmith.wormkout.Wormkout;

/**
 * Created by tibor on 10.8.14.
 */
public class MyLevels {
    public transient Wormkout g;
    public int unlockingBuffer;
    public Array<MyLevelState> lvls;

    public MyLevels(){
        lvls = new Array<MyLevelState>();
    }


    /**
     * Saves current instance of this class into file
     * later add encryption
     */
    public void saveLevelProgress(){
        //try to update leaderboards and achievements
        if (g.pDI.isSignedInGPGS()) {
            for (int i=0; i< lvls.size ; i++)
                g.pDI.submitScoreGPGS(lvls.get(i).name,(int)(lvls.get(i).bestTime*1000));
            if ( unlockingBuffer > 0) {
                g.pDI.incrementAchievementGPGS("achievement_determination", unlockingBuffer);
                unlockingBuffer = 0;
            }
        }
        //save locally
        FileHandle levelFile = Gdx.files.local("levelStates.json");
        Json json = new Json();
        levelFile.writeString(json.prettyPrint(this), false);
    }


    /**
     * Makes default version of Level states and saves it
     * later add encryption
     */
    public void makeDefault(){
        unlockingBuffer = 0;
        lvls.clear();
        lvls.add(getNewLevelInstance(false,false,3600,"Pi 1","levels/Pi1.bin",
                "776 gates long, about 40 seconds with speed 20"));
        lvls.add(getNewLevelInstance(true,false,3600,"Pi 2","levels/Pi2.bin",
                "1 535 gates long, about 1:15 minutes with speed 20"));
        lvls.add(getNewLevelInstance(true,false,3600,"Pi 3","levels/Pi3.bin",
                "1 535 gates long, about 1:15 minutes with speed 20"));
        lvls.add(getNewLevelInstance(true,false,3600,"Pi 4","levels/Pi4.bin",
                "1 457 gates long, about 1:15 minutes with speed 20"));
        lvls.add(getNewLevelInstance(true,false,3600,"Pi 5","levels/Pi5.bin",
                "2 353 gates long, about 2:00 minutes with speed 20"));
        lvls.add(getNewLevelInstance(true,false,3600,"Pi 6","levels/Pi6.bin",
                "2 174 gates long, about 1:50 minutes with speed 20"));
        lvls.add(getNewLevelInstance(true,false,3600,"Pi 7","levels/Pi7.bin",
                "2 386 gates long, about 2:00 minutes with speed 20"));
        lvls.add(getNewLevelInstance(true,false,3600,"Pi 8","levels/Pi8.bin",
                "3 091 gates long, about 2:35 minutes with speed 20"));
        lvls.add(getNewLevelInstance(true,false,3600,"Pi 9","levels/Pi9.bin",
                "3 917 gates long, about 3:15 minutes with speed 20"));
        lvls.add(getNewLevelInstance(true,false,3600,"Pi 10","levels/Pi10.bin",
                "3 815 gates long, about 3:10 minutes with speed 20"));
 
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




