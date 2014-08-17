package org.tiborsmith.wormkout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

        TextureLoader.TextureParameter param = new TextureLoader.TextureParameter();
        param.minFilter = Texture.TextureFilter.MipMapLinearNearest;
        param.magFilter = Texture.TextureFilter.Linear;
        param.genMipMaps = true;
        assets.load("graphics/myfont.png", Texture.class, param);
        assets.load("graphics/bold.png", Texture.class, param);
        assets.load("graphics/uiskin.atlas", TextureAtlas.class);
    }

    public void afterLoading(){
        skin = new Skin();
        BitmapFont font = new BitmapFont(Gdx.files.internal("graphics/myfont.fnt"),
                new TextureRegion(assets.get("graphics/myfont.png", Texture.class)), false);
        BitmapFont fontBold = new BitmapFont(Gdx.files.internal("graphics/bold.fnt"),
                new TextureRegion(assets.get("graphics/bold.png", Texture.class)), false);
        skin.add("default-font", fontBold, BitmapFont.class);
        skin.add("bold-font", fontBold, BitmapFont.class);
        skin.addRegions(assets.get("graphics/uiskin.atlas", TextureAtlas.class));
        skin.load(Gdx.files.internal("graphics/uiskin.json"));





        gate = game.assets.assets.get("graphics/gate.g3db", Model.class);


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
