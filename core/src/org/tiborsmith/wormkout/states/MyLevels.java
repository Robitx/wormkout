package org.tiborsmith.wormkout.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
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
            for (int i=1; i< lvls.size-5 ; i++)
                g.pDI.submitScoreGPGS(lvls.get(i).name,(int)(lvls.get(i).bestTime*1000));
            for (int i=lvls.size-5; i< lvls.size ; i++) {
                g.pDI.submitScoreGPGS(lvls.get(i).name, (int) (lvls.get(i).bestTime));
            }
            if ( unlockingBuffer > 0) {
                g.pDI.incrementAchievementGPGS("achievement_determination", unlockingBuffer);
                unlockingBuffer = 0;
            }
        }
        //save locally
        FileHandle levelFile = Gdx.files.local("states.json");
        Json json = new Json();
        levelFile.writeString(Base64Coder.encodeString(json.prettyPrint(this)), false);
    }


    /**
     * Makes default version of Level states and saves it
     * later add encryption
     */
    public void makeDefault(){
        unlockingBuffer = 0;
        lvls.clear();
        lvls.add(getNewLevelInstance(false,false,3600,"Tutorial","levels/tutorial.bin",
                "Tutorial level, maximal speed 5 gates/s"));
        if (g.pDI.appVersion() == 0 || g.pDI.appVersion() == 2) {
            for (int i = 11; i < 43; i++) {
                if (i < 13)
                    lvls.add(getNewLevelInstance(true, false, 3600, "Pi " + i, "levels/Pi " + i + ".bin",
                            "Pi series, minimal speed 5 gates/s"));
                else if (i < 16)
                    lvls.add(getNewLevelInstance(true, false, 3600, "Pi " + i, "levels/Pi " + i + ".bin",
                            "Pi series, minimal speed 10 gates/s"));
                else if (i < 18)
                    lvls.add(getNewLevelInstance(true, false, 3600, "Pi " + i, "levels/Pi " + i + ".bin",
                            "Pi series, minimal speed 15 gates/s"));
                else
                    lvls.add(getNewLevelInstance(true, false, 3600, "Pi " + i, "levels/Pi " + i + ".bin",
                            "Pi series, minimal speed 20 gates/s"));
            }
            for (int i = 1; i < 23; i++)
                lvls.add(getNewLevelInstance(true, false, 3600, "e " + i, "levels/E " + i + ".bin",
                        "Euler series, minimal speed 20 gates/s"));
            lvls.add(getNewLevelInstance(false, false, 3600, "Crazy 1", "levels/C 1.bin",
                    "Over the hills and far away"));
            lvls.add(getNewLevelInstance(false, false, 3600, "Crazy 2", "levels/C 2.bin",
                    "Dizzy Dizzy"));
            lvls.add(getNewLevelInstance(false, false, 3600, "Crazy 3", "levels/C 3.bin",
                    "My side, your side"));
            lvls.add(getNewLevelInstance(false, false, 3600, "Crazy 4", "levels/C 4.bin",
                    "Screw up"));
            lvls.add(getNewLevelInstance(false, false, 3600, "Crazy 5", "levels/C 5.bin",
                    "Screw down"));
        }
        else {
            for (int i = 1; i < 11; i++) {
                if (i < 3)
                    lvls.add(getNewLevelInstance(true, false, 3600, "Pi " + i, "levels/Pi " + i + ".bin",
                            "Pi series, minimal speed 5 gates/s"));
                else if (i < 5)
                    lvls.add(getNewLevelInstance(true, false, 3600, "Pi " + i, "levels/Pi " + i + ".bin",
                            "Pi series, minimal speed 10 gates/s"));
                else if (i < 7)
                    lvls.add(getNewLevelInstance(true, false, 3600, "Pi " + i, "levels/Pi " + i + ".bin",
                            "Pi series, minimal speed 15 gates/s"));
                else
                    lvls.add(getNewLevelInstance(true, false, 3600, "Pi " + i, "levels/Pi " + i + ".bin",
                            "Pi series, minimal speed 20 gates/s"));
            }
        }

        lvls.add(getNewLevelInstance(false,false,0,"Very easy","",
                "Randomly generated, minimal speed 10 gates/s"));
        lvls.add(getNewLevelInstance(false,false,0,"Easy","",
                "Randomly generated, minimal speed 15 gates/s"));
        lvls.add(getNewLevelInstance(false,false,0,"Normal","",
                "Randomly generated, minimal speed 20 gates/s"));
        lvls.add(getNewLevelInstance(false,false,0,"Hard","",
                "Randomly generated, minimal speed 25 gates/s"));
        lvls.add(getNewLevelInstance(false,false,0,"Insane","",
                "Randomly generated, minimal speed 30 gates/s"));

 
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




