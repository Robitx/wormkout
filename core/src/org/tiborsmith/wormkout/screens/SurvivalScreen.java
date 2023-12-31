package org.tiborsmith.wormkout.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

import org.tiborsmith.wormkout.Wormkout;
import org.tiborsmith.wormkout.ui.sLabel;

/**
 * Created by tibor on 28.7.14.
 */

public class SurvivalScreen implements Screen {
    Wormkout g;
    public SurvivalScreen(Wormkout game){ this.g = game; }

    private Stage stage;
    private sLabel msgLabel;
    private sLabel gNLabel;
    private ModelBatch modelBatch;
    private Environment environment;

    private boolean calibrating;
    private float timer;


    @Override
    public void render (float delta){
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);


        modelBatch.begin(g.player.cam);
        modelBatch.render(g.level.skybox);
        modelBatch.render(g.level.gates,environment);
        modelBatch.end();


        if (!calibrating) {
            g.player.updatePlayer(delta);
            timer += g.level.updateInfinite(delta);
            gNLabel.setText(g.assets.str.format("ScoreGates",(int)timer));
        }
        else {
            if (g.mySensorProcessing.isVertical()) {
                timer += delta;
                calibrating = g.level.startingAnimation(delta);
                if (!calibrating)
                    timer = 0;
                if (timer > 2.0f)
                    g.mySensorProcessing.calibrate();
                msgLabel.setText(g.assets.str.get("Calibration"));
                msgLabel.setVisible(calibrating);
                gNLabel.setVisible(!calibrating);
            }
            else {
                msgLabel.setText(g.assets.str.get("BeforeCalibration"));
                msgLabel.setVisible(calibrating);
                if (timer==0) {
                    g.pDI.say(g.assets.str.get("BeforeCalibration"), g.settings.soundVolume);
                    timer+=delta;
                }
            }

        }

        if (g.level.gameOver) {
            g.pDI.say(g.assets.str.get("sayGameOver1"),g.settings.soundVolume);


            float bestScore = g.lvls.lvls.get(g.currentLevel).bestTime;
            if (bestScore < timer) {
                g.lvls.lvls.get(g.currentLevel).bestTime = timer;
                g.pDI.say(g.assets.str.get("sayVictoryNewBestScore"),g.settings.soundVolume);
            }
            else {
                g.pDI.say(g.assets.str.get("sayDoneBetterBefore"),g.settings.soundVolume);
            }
            g.lvls.saveLevelProgress();

            if (g.pDI.appVersion()>0){
                g.pDI.showOrLoadInterstital();
            }
            g.setScreen(g.mainScreen);
            return;
        }



        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();



        if (!g.audio.playing)
            g.audio.startMusic();
    }

    @Override
    public void resize (int width, int height){
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show (){
        Gdx.gl.glClearColor(1.0f, 0.0f, 0.0f, 1);
        sLabel.setShader(g.assets.fontShader);
        stage = new Stage();
        stage.setViewport(g.assets.viewport);
        Gdx.input.setInputProcessor(stage);

        //better function
        int ms;
        if (g.currentLevel == g.lvls.lvls.size-1)
            ms = 30;
        else if (g.currentLevel == g.lvls.lvls.size-2)
            ms = 25;
        else if (g.currentLevel == g.lvls.lvls.size-3)
            ms = 20;
        else if (g.currentLevel == g.lvls.lvls.size-4)
            ms = 15;
        else
            ms = 10;

        final int minimalSpeed = ms;

        final sLabel speedLabel = new sLabel(g.assets.str.format("Speed",minimalSpeed), g.assets.skin);
        speedLabel.setAlignment(Align.left);
        stage.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == Input.Keys.BACK){
                    g.level.gameOver = true;
                    return true;
                }
                else
                    return false;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (x> stage.getWidth()/2)
                    g.player.speed++;
                else
                    g.player.speed--;
                g.player.speed = (g.player.speed>minimalSpeed) ? g.player.speed : minimalSpeed;
                g.player.speed = (g.player.speed<50) ? g.player.speed : 50;
                speedLabel.setText(g.assets.str.format("Speed",g.player.speed));
                return true;
            }
        });

        gNLabel = new sLabel(g.assets.str.format("ScoreGates",0), g.assets.skin);
        gNLabel.setAlignment(Align.right);
        gNLabel.setVisible(false);

        msgLabel = new sLabel(" ", g.assets.skin);
        msgLabel.setScale(2.0f);
        msgLabel.setAlignment(Align.center);
        msgLabel.setVisible(false);
        msgLabel.setWrap(true);

        Table table = new Table();
        table.setFillParent(true);
        table.add(speedLabel).expandX().padLeft(10).fill();
        table.add(gNLabel).expandX().padRight(10).fill().row();
        table.add(msgLabel).expand().fill().colspan(2).row();
        stage.addActor(table);


        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.9f, 0.9f, 0.9f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        calibrating = true;
        g.playMenu = true;
        timer = 0;
        g.mySensors.registerSensorListeners();
        g.mySensorProcessing.newCalibration = true;

        //first lvl then player
        g.level.loadInfiniteLevel();
        g.player.initPlayer();
        g.player.speed = minimalSpeed;
    }



    @Override
    public void hide (){
        stage.dispose();
        modelBatch.dispose();
        environment.clear();
        g.audio.stopMusic();
        g.level.disposeLevel();
        g.mySensors.unregisterSensorListeners();
    }

    @Override
    public void pause (){
        g.audio.stopMusic();
    }

    @Override
    public void resume (){
    }

    @Override
    public void dispose (){
    }
}
