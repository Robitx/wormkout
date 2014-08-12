package org.tiborsmith.wormkout.gamestates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import org.tiborsmith.wormkout.Wormkout;

/**
 *
 */
public class MyState {
    Wormkout game;

    public MyState(Wormkout game) {
        this.game = game;
    }


    public MyLevels levelStates;
    public MySettings mySettings;
    public MyPlayList myPlayList;
    public int currentLevel = 0;
    public int currentSong = 0;


    /**
     * load setting, game progress and music playlist
     */
    public void load() {
        //settings
        FileHandle settingsFile = Gdx.files.local("settings.json");
        if (settingsFile.exists()) {
            Json json = new Json();
            mySettings = json.fromJson(MySettings.class, settingsFile.readString());
            setMusicVolume(mySettings.musicVolume);
        } else {
            mySettings = new MySettings();
            mySettings.restoreDefaultSettings();
        }

        //game progress
        FileHandle levelFile = Gdx.files.local("levelStates.json");
        if (levelFile.exists()) {
            Json json = new Json();
            levelStates = json.fromJson(MyLevels.class, levelFile.readString());
        } else {
            levelStates = new MyLevels();
            levelStates.makeDefault();
        }

        //music playlist
        FileHandle playlistFile = Gdx.files.local("playlist.json");
        if (playlistFile.exists()) {
            Json json = new Json();
            myPlayList = json.fromJson(MyPlayList.class, playlistFile.readString());
        } else {
            myPlayList = new MyPlayList();
            myPlayList.makeDefaultPlaylist();
        }
        myPlayList.checkPlaylistconsistency();
    }


    /**
     *
     * @param volume
     */
    public void setMusicVolume(float volume){
        if (volume>=0 && volume <=1) {
            mySettings.musicVolume = volume;
            if (game.myAudio.device != null)
                game.myAudio.device.setVolume(volume);
        }
        mySettings.saveSettings();
    }

}







