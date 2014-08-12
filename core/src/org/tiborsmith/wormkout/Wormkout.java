package org.tiborsmith.wormkout;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import org.tiborsmith.wormkout.states.MyLevels;
import org.tiborsmith.wormkout.states.MyPlayList;
import org.tiborsmith.wormkout.states.MySettings;
import org.tiborsmith.wormkout.screens.AudioTestScreen;
import org.tiborsmith.wormkout.screens.GameScreen;
import org.tiborsmith.wormkout.screens.MainScreen;
import org.tiborsmith.wormkout.screens.SplashScreen;


public class Wormkout extends Game {
    //screens in game
    public SplashScreen splashScreen;
    public GameScreen gameScreen;
    public AudioTestScreen audioTestScreen;
    public MainScreen mainScreen;

    //my own control of sensors
    public final MySensors mySensors;
    public final MySensorProcessing mySensorProcessing;
    //player class (camera,controls)
    public final MyPlayer myPlayer;
    //loading models, music, levels and such
    public final MyAssets myAssets;
    public final MyAudio myAudio;
    public final MyLevel myLevel;


    //state classes and variables
    public MyLevels levelStates;
    public MySettings mySettings;
    public MyPlayList myPlayList;
    public int currentLevel = 0;
    public int currentSong = 0;



    public Wormkout(MySensors mySensors){
        this.mySensors = mySensors;
        mySensorProcessing = new MySensorProcessing(this);
        myAssets = new MyAssets(this);
        myPlayer = new MyPlayer(this);
        myAudio = new MyAudio(this);
        myLevel = new MyLevel(this);
    }


	@Override
	public void create () {
        splashScreen = new SplashScreen(this);
        gameScreen = new GameScreen(this);
        audioTestScreen = new AudioTestScreen(this);
        mainScreen = new MainScreen(this);


        super.setScreen(splashScreen);
	}

    @Override
    public void dispose(){
        myAssets.dispose();

        mainScreen.dispose();
        audioTestScreen.dispose();
        gameScreen.dispose();
    }

   @Override
	public void render () { super.render(); }



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
            if (myAudio.device != null)
                myAudio.device.setVolume(volume);
        }
        mySettings.saveSettings();
    }

}
