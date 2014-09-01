package org.tiborsmith.wormkout.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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
 * Created by tibor on 27.8.14.
 */
public class TutorialScreen implements Screen {
    Wormkout g;
    public TutorialScreen(Wormkout game){ this.g = game; }

    private Stage stage;
    private sLabel msgLabel;
    private sLabel gNLabel;
    private ModelBatch modelBatch;
    private Environment environment;

    sLabel speedLabel;

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

        timer += delta;


        if (timer<0.25) {
            g.pDI.say(g.assets.str.get("BeforeCalibration"), g.settings.soundVolume);
            msgLabel.setText(g.assets.str.get("BeforeCalibration"));
            g.player.speed = 0;
            speedLabel.setText(g.assets.str.format("Speed",g.player.speed));
        }
        else if (calibrating) {
            if (g.mySensorProcessing.isVertical()) {
                if (timer > 2.0f) {
                    msgLabel.setText("");
                    g.mySensorProcessing.calibrate();
                }
                if (timer > 3.0f) {
                    g.pDI.say(g.assets.str.get("TutorialCalibrationFinished")+" "+
                            g.assets.str.get("TutorialTapRight"),g.settings.soundVolume);
                    calibrating = false;
                }

            } else {
                timer =1;
            }
            g.player.speed = 0;
            speedLabel.setText(g.assets.str.format("TutorialSpeed",g.player.speed));
        }


        if (!calibrating) {
            int gN = g.level.update(delta);
            if (gN>115)
                msgLabel.setText(g.assets.str.get("TutorialTapRight"));
            if (gN==115)
                msgLabel.setText("");
            if (gN==110)
                g.pDI.say(g.assets.str.get("TutorialTapLeft"), g.settings.soundVolume);
            if (gN>95 && gN<110)
                msgLabel.setText(g.assets.str.get("TutorialTapLeft"));
            if (gN==95)
                msgLabel.setText("");
            if (gN==90)
                g.pDI.say(g.assets.str.get("TutorialTurnLeft"), g.settings.soundVolume);
            if (gN>65 && gN<90)
                msgLabel.setText(g.assets.str.get("TutorialTurnLeft"));
            if (gN==65)
                msgLabel.setText("");
            if (gN==55)
                g.pDI.say(g.assets.str.get("TutorialUpOrDown"),g.settings.soundVolume);
            if (gN>25 && gN<55)
                msgLabel.setText(g.assets.str.get("TutorialUpOrDown"));
            if (gN==25)
                msgLabel.setText("");

            g.player.updatePlayer(delta);
            gNLabel.setText(g.assets.str.format("RemainingGates",gN));
            gNLabel.setVisible(true);
        }


        if (g.level.gameOver) {
            g.pDI.say(g.assets.str.get("sayGameOver1"),g.settings.soundVolume);
            g.setScreen(g.mainScreen);
            return;
        }

        if (g.level.gameVictory){
            // sets new best time
            g.lvls.lvls.get(g.currentLevel).finished = true;
            g.pDI.say(g.assets.str.get("sayVictorySlow"),g.settings.soundVolume);
            // unlocks next lvl
            if (g.currentLevel+1 < g.lvls.lvls.size && g.lvls.lvls.get(g.currentLevel+1).locked) {
                g.lvls.lvls.get(g.currentLevel + 1).locked = false;
                g.pDI.appendSay(g.assets.str.get("sayLevelUnlock"),g.settings.soundVolume);
            }
            g.lvls.saveLevelProgress();

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
        g.player.speed = 0;


        speedLabel = new sLabel(g.assets.str.format("Speed",0), g.assets.skin);
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
                g.player.speed = (g.player.speed>0) ? g.player.speed : 0;
                g.player.speed = (g.player.speed<5) ? g.player.speed : 5;
                speedLabel.setText(g.assets.str.format("TutorialSpeed",g.player.speed));
                return true;
            }
        });

        gNLabel = new sLabel(g.assets.str.format("RemainingGates",0), g.assets.skin);
        gNLabel.setAlignment(Align.right);
        gNLabel.setVisible(false);

        msgLabel = new sLabel(" ", g.assets.skin);
        msgLabel.setScale(2.0f);
        msgLabel.setColor(Color.CYAN);
        msgLabel.setAlignment(Align.center);
        msgLabel.setVisible(true);
        msgLabel.setWrap(true);

        Table table = new Table();
        table.setFillParent(true);
        table.add(speedLabel).expandX().padLeft(10).fill();
        table.add(gNLabel).expandX().padRight(10).fill().row();
        table.add(msgLabel).expand().fill().colspan(2).row();
        stage.addActor(table);
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