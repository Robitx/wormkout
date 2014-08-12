package org.tiborsmith.wormkout.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class MySettings{
    public float musicVolume;
    public float soundVolume;
    public boolean loggedInGPGS;



    public void saveSettings(){
        FileHandle settingsFile = Gdx.files.local("settings.json");
        Json json = new Json();
        settingsFile.writeString(json.prettyPrint(this), false);
    }


    public void restoreDefaultSettings(){
        FileHandle settingsFile = Gdx.files.local("settings.json");
        loggedInGPGS = false;
        musicVolume = 1.0f;
        soundVolume = 1.0f;
        Json json = new Json();
        settingsFile.writeString(json.prettyPrint(this), false);
    }
}

