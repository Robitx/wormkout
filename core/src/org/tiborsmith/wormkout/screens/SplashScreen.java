package org.tiborsmith.wormkout.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import org.tiborsmith.wormkout.Wormkout;

/**
 * Created by tibor on 28.7.14.
 */
public class SplashScreen implements Screen {
    Wormkout game;
    public SplashScreen (Wormkout game){   this.game = game; }

    private Stage stage;
    private Texture  splashTexture;
    private float timer = 0.0f;

    @Override
    public void render (float delta){
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (game.myAssets.assets.update() && timer > 5.0f) {
            game.myAssets.afterLoading();
            game.setScreen(game.mainScreen);
            return;
        }

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
        timer += delta;
    }

    @Override
    public void resize (int width, int height){
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show (){
        stage = new Stage();
        if(Gdx.graphics.getHeight()< 1280 || Gdx.graphics.getWidth() < 800)
            stage.setViewport(new FitViewport(1280,800));
        else
            stage.setViewport(new ScreenViewport());

        splashTexture = new Texture(Gdx.files.internal("data/splash.png"));
        Image splashImage = new Image(splashTexture);

        Table table = new Table();
        table.setFillParent(true);
        table.add(splashImage);
        stage.addActor(table);
        stage.getRoot().setColor(0,0,0,1);
        stage.getRoot().addAction(Actions.sequence(Actions.delay(3.1f),Actions.fadeOut(2.0f)));


        game.myAssets.load();
        game.load();
    }

    @Override
    public void hide (){
        stage.dispose();
        splashTexture.dispose();
    }

    @Override
    public void pause (){

    }

    @Override
    public void resume (){

    }

    @Override
    public void dispose (){

    }
}
