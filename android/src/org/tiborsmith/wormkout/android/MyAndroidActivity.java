package org.tiborsmith.wormkout.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.GameHelper;

import org.tiborsmith.wormkout.Wormkout;
import org.tiborsmith.wormkout.states.MyActionResolver;

public class MyAndroidActivity extends AndroidApplication implements GameHelper.GameHelperListener, MyActionResolver {

    private GameHelper gameHelper;

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

        initialize(new Wormkout(new MyAndroidSensors(this.getContext()),this), config);

        gameHelper = new GameHelper(this, GameHelper.CLIENT_GAMES );
        gameHelper.enableDebugLog(false);
        gameHelper.setMaxAutoSignInAttempts(0);
        gameHelper.setup(this);
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
    public void getAchivementsGPGS() {
        startActivityForResult(Games.Achievements.getAchievementsIntent(gameHelper.getApiClient()), REQUEST_CODE);
    }



    @Override
    public void getLeaderboardGPGS() {
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(gameHelper.getApiClient(), getString(R.string.Leaderboard_lvl1)), REQUEST_CODE);
    }

    @Override
    public void rateGame() {
        /*
        String str ="https://play.google.com/store/apps/details?id=org.tiborsmith.wormkout";
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
    public void submitScoreGPGS(String scoreId, int score) {
        Games.Leaderboards.submitScore(gameHelper.getApiClient(), scoreId, score);
        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(gameHelper.getApiClient(), scoreId), REQUEST_CODE);
    }

    @Override
    public void unlockAchievementGPGS(String achievementId) {
        if (achievementId == getString(R.string.Achievement_unlockedLvl)) {
            Games.Achievements.increment(gameHelper.getApiClient(), achievementId,1);
        }
        else {
            Games.Achievements.unlock(gameHelper.getApiClient(), achievementId);
        }
    }


}
