package org.tiborsmith.wormkout.steady;

/**
 * Created by tibor on 22.8.14.
 */
public interface MyTTS {
    public void say(String phrase, float volume);
    public void appendSay(String phrase, float volume);
}
