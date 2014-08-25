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
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;
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

    public  I18NBundle str;
    public Viewport viewport;
    public TextureAtlas images;
    public ShaderProgram fontShader;


    public void load(){
        assets.load("graphics/gate.g3db", Model.class);
        assets.load("graphics/skybox.g3db", Model.class);

        TextureLoader.TextureParameter param = new TextureLoader.TextureParameter();
        param.minFilter = Texture.TextureFilter.MipMapLinearNearest;
        param.magFilter = Texture.TextureFilter.Linear;
        param.genMipMaps = true;
        assets.load("graphics/myfont.png", Texture.class, param);
        assets.load("graphics/uiskin.atlas", TextureAtlas.class);
        assets.load("graphics/images.atlas", TextureAtlas.class);
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

        images = assets.get("graphics/images.atlas", TextureAtlas.class);

        str = I18NBundle.createBundle(Gdx.files.internal("i18n/MyBundle"));

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
