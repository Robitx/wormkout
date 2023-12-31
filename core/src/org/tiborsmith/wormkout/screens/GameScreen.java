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

public class GameScreen implements Screen {
    Wormkout g;
    public GameScreen(Wormkout game){ this.g = game; }

    private Stage stage;
   // private Batch batch;
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


        if (!(g.level.gameVictory || g.level.gameOver)) {
            if (!calibrating) {
                g.player.updatePlayer(delta);
                gNLabel.setText(g.assets.str.format("RemainingGates", g.level.update(delta)));
                timer += delta;
            } else {
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
                } else {
                    msgLabel.setText(g.assets.str.get("BeforeCalibration"));
                    msgLabel.setVisible(calibrating);
                    if (timer == 0) {
                        g.pDI.say(g.assets.str.get("BeforeCalibration"), g.settings.soundVolume);
                        timer += delta;
                    }
                }
            }
        }

        if (g.level.gameOver) {
            int choice = (int)(3*Math.random());
            if (choice == 0)
                g.pDI.say(g.assets.str.get("sayGameOver1"),g.settings.soundVolume);
            else if (choice == 1)
                g.pDI.say(g.assets.str.get("sayGameOver2"),g.settings.soundVolume);
            else
                g.pDI.say(g.assets.str.get("sayGameOver3"),g.settings.soundVolume);
            g.welcomeBack = false;


            if (g.pDI.appVersion()>0){
                g.pDI.showOrLoadInterstital();
            }
            g.setScreen(g.mainScreen);
            return;
        }

        if (g.level.gameVictory){
            if (!calibrating) {
                // sets new best time
                g.lvls.lvls.get(g.currentLevel).finished = true;
                float bestTime = g.lvls.lvls.get(g.currentLevel).bestTime;
                if (bestTime > timer) {
                    g.lvls.lvls.get(g.currentLevel).bestTime = timer;
                    g.pDI.say(g.assets.str.get("sayVictoryNewBestTime"), g.settings.soundVolume);
                    g.lvls.lvls.get(g.currentLevel).rewards=1;
                    if (timer < g.level.levelSize/14.5f){
                        g.lvls.lvls.get(g.currentLevel).rewards=2;
                    }
                    if (timer < g.level.levelSize/19.5f){
                        g.lvls.lvls.get(g.currentLevel).rewards=3;
                    }
                } else {
                    g.pDI.say(g.assets.str.get("sayVictorySlow"), g.settings.soundVolume);
                }
                // unlocks next lvl
                if (g.currentLevel + 1 < g.lvls.lvls.size && g.lvls.lvls.get(g.currentLevel + 1).locked) {
                    g.lvls.lvls.get(g.currentLevel + 1).locked = false;
                    g.pDI.appendSay(g.assets.str.get("sayLevelUnlock"), g.settings.soundVolume);
                    g.lvls.unlockingBuffer++;
                }
                g.lvls.saveLevelProgress();

            if (g.pDI.appVersion()>0){
                g.pDI.showOrLoadInterstital();
            }
            g.setScreen(g.mainScreen);
            }
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


        final int minimalSpeed = 10;

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

        gNLabel = new sLabel(g.assets.str.format("RemainingGates",0), g.assets.skin);
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
        g.level.loadLevel();
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
