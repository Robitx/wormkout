package org.tiborsmith.wormkout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by tibor on 31.7.14.
 */

public class MyAssets {
    Wormkout game;
    public MyAssets (Wormkout game){
        this.game = game;
        assets = new AssetManager();
    }

    public AssetManager assets;
    public Skin skin;
    public Model gate;

    public Viewport viewport;



   // public float progress = 0;
   // public boolean loading=false;






    public void load(){
        assets.load("data/gate.g3db", Model.class);
        assets.load("data/default.fnt", BitmapFont.class);
        assets.load("data/uiskin.json", Skin.class);

    }

    public void afterLoading(){
        skin = game.myAssets.assets.get("data/uiskin.json", Skin.class);
        gate = game.myAssets.assets.get("data/gate.g3db", Model.class);


        /*if(Gdx.graphics.getHeight()< 1280 || Gdx.graphics.getWidth() < 800)
            viewport = new FitViewport(1280,800);
        else*/
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
    }




    public void dispose(){
        assets.dispose();
    }
}
