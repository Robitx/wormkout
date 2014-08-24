package org.tiborsmith.wormkout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
    public Model skybox;
    public MyLevelParts parts = new MyLevelParts();

    public Viewport viewport;

    public ShaderProgram fontShader;

    public Image lgdx, lincompetech;
    public Image lgplus, lfacebook, ltwitter, lplaystore, ldonate, lyoutube;







    public void load(){
        assets.load("graphics/gate.g3db", Model.class);
        assets.load("graphics/skybox.g3db", Model.class);

        TextureLoader.TextureParameter param = new TextureLoader.TextureParameter();
        param.minFilter = Texture.TextureFilter.MipMapLinearNearest;
        param.magFilter = Texture.TextureFilter.Linear;
        param.genMipMaps = true;
        assets.load("graphics/myfont.png", Texture.class, param);
        assets.load("graphics/uiskin.atlas", TextureAtlas.class);
        assets.load("graphics/gdx.png", Texture.class);
        assets.load("graphics/incompetech.png", Texture.class);
        assets.load("graphics/gplus.png", Texture.class);
        assets.load("graphics/facebook.png", Texture.class);
        assets.load("graphics/twitter.png", Texture.class);
        assets.load("graphics/playstore.png", Texture.class);
        assets.load("graphics/donate.gif", Texture.class);
        assets.load("graphics/youtube.png", Texture.class);
    }

    public void afterLoading(){
        skin = new Skin();
        BitmapFont font = new BitmapFont(Gdx.files.internal("graphics/myfont.fnt"),
                new TextureRegion(assets.get("graphics/myfont.png", Texture.class)), false);
        skin.add("default-font", font, BitmapFont.class);
        skin.add("bold-font", font, BitmapFont.class);
        skin.addRegions(assets.get("graphics/uiskin.atlas", TextureAtlas.class));
        skin.load(Gdx.files.internal("graphics/uiskin.json"));


        fontShader = new ShaderProgram(Gdx.files.internal("shaders/font.vsh"), Gdx.files.internal("shaders/font.fsh"));



        lgdx = new Image(assets.get("graphics/gdx.png", Texture.class));
        lincompetech = new Image(assets.get("graphics/incompetech.png", Texture.class));
        lgplus = new Image(assets.get("graphics/gplus.png", Texture.class));
        lfacebook = new Image(assets.get("graphics/facebook.png", Texture.class));
        ltwitter = new Image(assets.get("graphics/twitter.png", Texture.class));
        lplaystore = new Image(assets.get("graphics/playstore.png", Texture.class));
        ldonate = new Image(assets.get("graphics/donate.gif", Texture.class));
        lyoutube = new Image(assets.get("graphics/youtube.png", Texture.class));


        gate = assets.get("graphics/gate.g3db", Model.class);
        skybox = assets.get("graphics/skybox.g3db", Model.class);

        parts.loadElements();
        viewport = new ScreenViewport();
        viewport.update(Gdx.graphics.getWidth(),Gdx.graphics.getHeight(),true);
    }




    public void dispose(){
        assets.dispose();
        parts.dispose();

        fontShader.dispose();
    }
}
