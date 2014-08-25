package org.tiborsmith.wormkout;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import org.tiborsmith.wormkout.screens.GameScreen;
import org.tiborsmith.wormkout.screens.MainScreen;
import org.tiborsmith.wormkout.screens.SplashScreen;
import org.tiborsmith.wormkout.states.MyLevels;
import org.tiborsmith.wormkout.states.MyPlayList;
import org.tiborsmith.wormkout.states.MySettings;
import org.tiborsmith.wormkout.steady.MyActionResolver;
import org.tiborsmith.wormkout.steady.MySensorProcessing;
import org.tiborsmith.wormkout.steady.MySensors;
import org.tiborsmith.wormkout.steady.MyTTS;


public class Wormkout extends Game {
    //screens in game
    public SplashScreen splashScreen;
    public GameScreen gameScreen;
    public MainScreen mainScreen;

    //my own control of sensors
    public final MySensors mySensors;
    public final MySensorProcessing mySensorProcessing;
    //action resolver for interaction with GPGS
    public final MyActionResolver myActionResolver;
    //interface for text to speech
    public final MyTTS tts;
    //player class (camera,controls)
    public final MyPlayer player;
    //loading models, music, levels and such
    public final MyAssets myAssets;
    public final MyAudio audio;
    public final MyLevel level;
    //strings and other constants
   // public final Constants str;


    //state classes and variables
    public MyLevels levelStates;
    public MySettings settings;
    public MyPlayList playList;
    public int currentLevel = 0;
    public int currentSong = 0;

    //some flags
    public boolean welcomeBack;
    public boolean playMenu;
    public boolean firstLaunch;


    public Wormkout(MySensors mySensors, MyActionResolver myActionResolver, MyTTS myTTS){
        super();
        this.mySensors = mySensors;
        this.myActionResolver = myActionResolver;
        this.tts = myTTS;
        mySensorProcessing = new MySensorProcessing(this);
        myAssets = new MyAssets(this);
        player = new MyPlayer(this);
        audio = new MyAudio(this);
        level = new MyLevel(this);
    //    str = new Constants();
    }


	@Override
	public void create () {
        splashScreen = new SplashScreen(this);
        gameScreen = new GameScreen(this);
        mainScreen = new MainScreen(this);


        Gdx.input.setCatchBackKey(true);

        super.setScreen(splashScreen);
	}

    @Override
    public void dispose(){
        myAssets.dispose();

        mainScreen.dispose();
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
            settings = json.fromJson(MySettings.class, settingsFile.readString());
            setMusicVolume(settings.musicVolume);
        } else {
            settings = new MySettings();
            settings.restoreDefaultSettings();
            firstLaunch = true;
        }

        //game progress
        FileHandle levelFile = Gdx.files.local("levelStates.json");
        if (levelFile.exists()) {
            Json json = new Json();
            levelStates = json.fromJson(MyLevels.class, levelFile.readString());
            levelStates.g = this;
        } else {
            levelStates = new MyLevels();
            levelStates.g = this;
            levelStates.makeDefault();
        }

        //music playlist
        FileHandle playlistFile = Gdx.files.local("playlist.json");
        if (playlistFile.exists()) {
            Json json = new Json();
            playList = json.fromJson(MyPlayList.class, playlistFile.readString());
        } else {
            playList = new MyPlayList();
            playList.makeDefaultPlaylist();
        }
        playList.checkPlaylistconsistency();
    }


    /**
     *
     * @param volume
     */
    public void setMusicVolume(float volume){
        if (volume>=0 && volume <=1) {
            settings.musicVolume = volume;
            if (audio.device != null)
                audio.device.setVolume(volume);
        }
        settings.saveSettings();
    }

}
