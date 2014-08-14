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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

import org.tiborsmith.wormkout.Wormkout;

/**
 * Created by tibor on 28.7.14.
 */

public class GameScreen implements Screen {
    Wormkout game;
    public GameScreen(Wormkout game){ this.game = game; }

    private Stage stage;
    private Label msgLabel;
    private ModelBatch modelBatch;
    private Environment environment;

    private boolean calibrating;
    private float timer;


    @Override
    public void render (float delta){
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);


        modelBatch.begin(game.myPlayer.cam);
        modelBatch.render(game.myLevel.gates,environment);
        modelBatch.end();


        if (!game.myLevel.gameOver) {

            if (!calibrating) {
                game.myPlayer.updatePlayer(delta);
                game.myLevel.update(delta);
                timer += delta;
            }
            else {
                calibrating = game.myLevel.startingAnimation(delta);
                game.mySensorProcessing.calibrate();
                timer = 0;
                msgLabel.setText("Please hold still. Calibrating sensors...");
                msgLabel.setVisible(calibrating);
            }
        }
        else {
            game.setScreen(game.mainScreen);
            return;
        }

        if (game.myLevel.gameVictory){
            msgLabel.setText("Victory!");
            msgLabel.setVisible(true);

            // sets new best time
            game.levelStates.lvls.get(game.currentLevel).finished = true;
            float bestTime = game.levelStates.lvls.get(game.currentLevel).bestTime;
            if (bestTime > timer)
                game.levelStates.lvls.get(game.currentLevel).bestTime = timer;
            // unlocks next lvl
            if (game.currentLevel+1 < game.levelStates.lvls.size && game.levelStates.lvls.get(game.currentLevel+1).locked)
                game.levelStates.lvls.get(game.currentLevel+1).locked = false;
            game.levelStates.saveLevelProgress();

            game.setScreen(game.mainScreen);
            return;
        }


        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

        if (!game.myAudio.playing)
            game.myAudio.startMusic();
}

    @Override
    public void resize (int width, int height){
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show (){
        stage = new Stage();
        stage.setViewport(game.myAssets.viewport);
        Gdx.input.setInputProcessor(stage);

        final Label speedLabel = new Label("Speed: 2 gates/s",game.myAssets.skin);
        speedLabel.setAlignment(Align.left);


        stage.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == Input.Keys.BACK){
                    game.myLevel.gameOver = true;
                    return true;
                }
                else
                    return false;
            }

           @Override
           public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
               if (x> stage.getWidth()/2)
                   game.myPlayer.speed++;
               else
                   game.myPlayer.speed--;
               game.myPlayer.speed = (game.myPlayer.speed>0) ? game.myPlayer.speed : 0;
               game.myPlayer.speed = (game.myPlayer.speed<20) ? game.myPlayer.speed : 20;
               speedLabel.setText("Speed: "+game.myPlayer.speed +" gates/s");
               return true;
           }
        });



        msgLabel = new Label(" ",game.myAssets.skin);
        msgLabel.setAlignment(Align.center);
        msgLabel.setVisible(false);

        Table table = new Table();
        table.setFillParent(true);
        table.add(speedLabel).expandX().fill().row();
        table.add(msgLabel).expand().fill().row();
        stage.addActor(table);


        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        calibrating = true;
        game.playMenu = true;
        game.mySensors.registerSensorListeners();

        //first lvl then player
        game.myLevel.loadLevel();
        game.myPlayer.initPlayer();
    }



    @Override
    public void hide (){
        stage.dispose();
        modelBatch.dispose();
        environment.clear();
        game.myAudio.stopMusic();
        game.myLevel.disposeLevel();
        game.mySensors.unregisterSensorListeners();
    }

    @Override
    public void pause (){
        game.myAudio.stopMusic();
    }

    @Override
    public void resume (){
    }

    @Override
    public void dispose (){
    }
}
