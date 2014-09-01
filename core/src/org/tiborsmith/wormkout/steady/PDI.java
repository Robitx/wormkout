package org.tiborsmith.wormkout.steady;

/**
 * Created by tibor on 13.8.14.
 * Interface with platform dependent functions
 */
public interface PDI {

    public boolean isSignedInGPGS();
    public void signInGPGS();
    public void signOutGPGS();
    public void rateGame();
    public void submitScoreGPGS(String levelName,int score);
    public void unlockAchievementGPGS(String achievement);
    public void incrementAchievementGPGS(String achievement,int score);
    public void getLeaderboardGPGS(String levelName);
    public void getAchievementsGPGS();

    public void say(String phrase, float volume);
    public void appendSay(String phrase, float volume);

    public int appVersion();  //full paid = 0, demo = 1, full free = 2

    public void showOrLoadInterstital();

}

