package org.tiborsmith.wormkout;

import com.badlogic.gdx.Game;

import org.tiborsmith.wormkout.gamestates.MyState;
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

    public final MyState myState;





    public Wormkout(MySensors mySensors){
        this.mySensors = mySensors;
        mySensorProcessing = new MySensorProcessing(this);
        myAssets = new MyAssets(this);
        myPlayer = new MyPlayer(this);
        myAudio = new MyAudio(this);
        myLevel = new MyLevel(this);
        myState = new MyState(this);
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
}
