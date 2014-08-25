package org.tiborsmith.wormkout.android;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.WindowManager;
import android.widget.Toast;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GameHelper;

import org.tiborsmith.wormkout.Wormkout;
import org.tiborsmith.wormkout.steady.MyActionResolver;
import org.tiborsmith.wormkout.steady.MyTTS;

import java.util.HashMap;
import java.util.Locale;

public class MyAndroidActivity extends AndroidApplication implements GameHelper.GameHelperListener,
        MyActionResolver, TextToSpeech.OnInitListener, MyTTS {

    private GameHelper gameHelper;
    private TextToSpeech tts;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        //disabling them in gdx since I have to control them myself
        config.useCompass = false;
        config.useAccelerometer = false;

        //Immersive mode
        config.useImmersiveMode = true;

        //keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initialize(new Wormkout(new MyAndroidSensors(this.getContext()),this, this), config);

        gameHelper = new GameHelper(this, GameHelper.CLIENT_GAMES );
        gameHelper.enableDebugLog(false);
        gameHelper.setMaxAutoSignInAttempts(0);
        gameHelper.setup(this);

        tts = new TextToSpeech(this,this);
	}

    /**
     * GameHelperListener method
     */
    @Override
    public void onSignInFailed() {

    }

    /**
     * GameHelperListener method
     */
    @Override
    public void onSignInSucceeded() {

    }


    /**
     * activity state passed on gameHelper
     */
    @Override
    public void onStart(){
        super.onStart();
        gameHelper.onStart(this);
    }

    /**
     * activity state passed on gameHelper
     */
    @Override
    public void onStop(){
        super.onStop();
        gameHelper.onStop();
    }

    /**
     * activity state passed on gameHelper
     */
    @Override
    public void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        gameHelper.onActivityResult(request, response, data);
    }

    


    @Override
    public boolean isSignedInGPGS() {
        return gameHelper.isSignedIn();
    }


    private final static int REQUEST_CODE = 9002;

    @Override
    public void getAchievementsGPGS() {
        startActivityForResult(Games.Achievements.getAchievementsIntent(gameHelper.getApiClient()), REQUEST_CODE);
    }



    @Override
    public void getLeaderboardGPGS(String level) {
        String tmp = "leaderboard_"+level.replace(" ","_").toLowerCase();
        tmp = getString(getResources().getIdentifier(tmp,"string",getPackageName()));
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(gameHelper.getApiClient(), tmp), REQUEST_CODE);
    }

    @Override
    public void rateGame() {
        /*
        String str ="https://play.google.com/store/apps/details?id=org.tiborsmith.wormkout.android";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(str)));
        */
    }

    @Override
    public void signInGPGS() {
        try{
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    gameHelper.beginUserInitiatedSignIn();
                }
            });
        } catch (final Exception e){
            Gdx.app.log("MainActivity", "Log in failed: " + e.getMessage() + ".");
        }
    }

    @Override
    public void signOutGPGS() {
        try{
            runOnUiThread(new Runnable(){
                //@Override
                public void run(){
                    gameHelper.signOut();
                }
            });
        }
        catch (Exception e){
            Gdx.app.log("MainActivity", "Log out failed: " + e.getMessage() + ".");
        }
    }

    @Override
    public void submitScoreGPGS(String level, int score) {
        String tmp = "leaderboard_"+level.replace(" ","_").toLowerCase();
        tmp = getString(getResources().getIdentifier(tmp,"string",getPackageName()));
        Games.Leaderboards.submitScore(gameHelper.getApiClient(), tmp, score);
        //startActivityForResult(Games.Leaderboards.getLeaderboardIntent(gameHelper.getApiClient(), level), REQUEST_CODE);
    }

    @Override
    public void incrementAchievementGPGS(String achievement, int score) {
        if (achievement == "achievement_level_progress") {
            Games.Achievements.increment(gameHelper.getApiClient(),
                    getString(R.string.achievement_level_progress),score);
        }
    }

    @Override
    public void unlockAchievementGPGS(String achievement) {

        if (achievement == "achievement_customized_music") {
            Games.Achievements.unlock(gameHelper.getApiClient(),
                    getString(R.string.achievement_customized_music));
        }
        if (achievement == "achievement_link_exploration") {
            Games.Achievements.increment(gameHelper.getApiClient(),
                    getString(R.string.achievement_link_exploration),1);
        }
        if (achievement == "achievement_visited_help") {
            Games.Achievements.unlock(gameHelper.getApiClient(),
                    getString(R.string.achievement_visited_help));
        }
        if (achievement == "achievement_visited_credits") {
            Games.Achievements.unlock(gameHelper.getApiClient(),
                    getString(R.string.achievement_visited_credits));
        }
    }


    /**
     * My Text to speech implementation
     */


    @Override
    public void onInit(int status) {
        if (status==TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.ENGLISH);
        } else {
            tts = null;
            Toast.makeText(this, "Failed to initialize TTS engine.",
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void say(String phrase, float volume) {
        if (tts!=null) {
            HashMap<String,String> param = new HashMap<String, String>();
            param.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, ""+volume);
            tts.speak(phrase, TextToSpeech.QUEUE_FLUSH, param);
        }
    }

    @Override
    public void appendSay(String phrase, float volume) {
        if (tts!=null) {
            HashMap<String,String> param = new HashMap<String, String>();
            param.put(TextToSpeech.Engine.KEY_PARAM_VOLUME, ""+volume);
            param.put(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS,"true");
            tts.speak(phrase, TextToSpeech.QUEUE_ADD, param);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts!=null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
