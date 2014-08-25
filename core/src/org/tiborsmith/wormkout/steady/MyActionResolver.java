package org.tiborsmith.wormkout.steady;

/**
 * Created by tibor on 13.8.14.
 */
public interface MyActionResolver {

    public boolean isSignedInGPGS();
    public void signInGPGS();
    public void signOutGPGS();
    public void rateGame();
    public void submitScoreGPGS(String levelName,int score);
    public void unlockAchievementGPGS(String achievement);
    public void incrementAchievementGPGS(String achievement,int score);
    public void getLeaderboardGPGS(String levelName);
    public void getAchievementsGPGS();

}
