package org.tiborsmith.wormkout.steady;

/**
 * Created by tibor on 13.8.14.
 */
public interface MyActionResolver {

    public boolean isSignedInGPGS();
    public void signInGPGS();
    public void signOutGPGS();
    public void rateGame();
    public void submitScoreGPGS(String scoreId,int score);
    public void unlockAchievementGPGS(String achievementId);
    public void getLeaderboardGPGS();
    public void getAchivementsGPGS();

}
