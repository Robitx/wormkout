package org.tiborsmith.wormkout.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;

/**
 * Created by tibor on 11.8.14.
 */
public class MyPlayList {

    public MyPlayList(){
        songNames = new Array<String>();
        songPaths = new Array<String>();
    }

    public int numOfDefaultSong;
    public boolean playDefault;
    public Array<String> songNames;
    public Array<String> songPaths;


    public void savePlayList(){
        FileHandle playlistFile = Gdx.files.local("playlist.json");
        Json json = new Json();
        playlistFile.writeString(json.prettyPrint(this), false);
    }


    /**
     * makes default playlist and saves it
     */
    public void makeDefaultPlaylist(){
        playDefault = true;
        numOfDefaultSong = 4;
        for (int i = 0; i < numOfDefaultSong; i++) {
            songNames.add("default"+i+".mp3");
            songPaths.add(Gdx.files.external(".Wormkout/default" + i + ".mp3").path());
        }
        savePlayList();
    }


    /**
     * checks if playlist is still valid since last time
     */
    public void checkPlaylistconsistency(){

        // if there are no custom songs, default once must be active
        if (numOfDefaultSong == songPaths.size)
            playDefault = true;

        // check the existence of extracted default songs (and make them again if they don't exist)
        for (int i = 0; i < numOfDefaultSong; i++) {
            if (!Gdx.files.external(".Wormkout/default"+i+".mp3").exists()) {
                Gdx.files.internal(Gdx.files.internal("audio/default" + i +
                        ".mp3").path()).copyTo(Gdx.files.external(".Wormkout/default" + i + ".mp3"));
            }
        }

        // check that custom songs still exist, if not erase them from playlist
        if (numOfDefaultSong < songPaths.size)
        {
            for (int i = numOfDefaultSong; i < songPaths.size; i++){
                if (!Gdx.files.external(songPaths.get(i).toString()).exists())
                    songPaths.removeIndex(i);
            }
        }

        //save changes
        savePlayList();
    }

}
