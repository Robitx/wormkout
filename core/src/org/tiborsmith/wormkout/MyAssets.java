package org.tiborsmith.wormkout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
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
    public MyLevelParts parts = new MyLevelParts();

    public Viewport viewport;






    public void load(){
        assets.load("graphics/gate.g3db", Model.class);
        //TextureLoader.TextureParameter param = new TextureLoader.TextureParameter();
        //param.minFilter = Texture.TextureFilter.MipMapLinearNearest;
        //param.magFilter = Texture.TextureFilter.Linear;
        //param.genMipMaps = true;
        //assets.load("graphics/myfont.png", Texture.class, param);
        //assets.load("graphics/myfont.fnt", BitmapFont.class);
        assets.load("graphics/uiskin.json", Skin.class);
    }

    public void afterLoading(){
        skin = game.myAssets.assets.get("graphics/uiskin.json", Skin.class);
        // maybe set according to screen size
        skin.getFont("default-font").setScale(0.5f);
        skin.getFont("bold-font").setScale(0.5f);
        //skin.getFont("default-font").getRegion().getTexture().setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Linear);


        gate = game.myAssets.assets.get("graphics/gate.g3db", Model.class);


        parts.loadElements();

        /*if(Gdx.graphics.getHeight()< 1280 || Gdx.graphics.getWidth() < 800)
            viewport = new FitViewport(1280,800);
        else*/
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
    }




    public void dispose(){
        assets.dispose();
        parts.dispose();
    }
}
