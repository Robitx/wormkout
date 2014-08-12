package org.tiborsmith.wormkout.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import org.tiborsmith.wormkout.Wormkout;

/**
 * Created by tibor on 28.7.14.
 */

public class GameScreen implements Screen {
    Wormkout game;
    public GameScreen(Wormkout game){ this.game = game; }

    private Stage stage;
    private Batch batch;
    private BitmapFont font;
    private ModelBatch modelBatch;
    private Environment environment;

    private boolean calibrating;
    private float timer;

    @Override
    public void render (float delta){
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        // Enable face culling- be careful with spriteBatch, might cull sprites as well!
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        // What faces to remove with the face culling.
        Gdx.gl.glCullFace(GL20.GL_BACK);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);


        modelBatch.begin(game.myPlayer.cam);
        modelBatch.render(game.myLevel.gates,environment);
        modelBatch.end();




        if (!game.myLevel.gameOver) {

            if (!calibrating) {
                game.myPlayer.updateCam(delta);
                game.myLevel.update(delta);
                timer += delta;

            } else {
                calibrating = game.myLevel.startingAnimation(delta);
                batch.begin();
                font.draw(batch, "Calibrating, hold still", Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2-50);
                batch.end();
                game.mySensorProcessing.calibrate();
                timer = 0;
            }
        }
        else
        {
            batch.begin();
            font.draw(batch, "Game Over", Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2-50);
            batch.end();
            game.setScreen(game.mainScreen);
            return;
        }



        if (game.myLevel.gameVictory){
            batch.begin();
            font.draw(batch, "You won", Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2-50);
            batch.end();

            // sets new best time
            game.myState.levelStates.lvls.get(game.myState.currentLevel).finished = true;
            float bestTime = game.myState.levelStates.lvls.get(game.myState.currentLevel).bestTime;
            if (bestTime > timer)
                game.myState.levelStates.lvls.get(game.myState.currentLevel).bestTime = timer;
            // unlocks next lvl
            if (game.myState.currentLevel+1 < game.myState.levelStates.lvls.size && game.myState.levelStates.lvls.get(game.myState.currentLevel+1).locked)
                game.myState.levelStates.lvls.get(game.myState.currentLevel+1).locked = false;
            game.myState.levelStates.saveLevelProgress();

            game.setScreen(game.mainScreen);
            return;
        }



        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

        byte laZ = (byte)(10*game.mySensors.getLinearAccelerationZ());

        batch.begin();
        font.draw(batch, "FPS: "+ Gdx.graphics.getFramesPerSecond()+ " ZAcceleration" + laZ, 20, 200);
        font.draw(batch, "PX "+ (game.myPlayer.position.x)+" PY "
                + (game.myPlayer.position.y)+" PZ "+ (game.myPlayer.position.z), 20, 300);
        batch.end();

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

        TextButton menuButton = new TextButton("Go back to menu",game.myAssets.skin);

        Table table = new Table();
        table.setFillParent(true);
        table.add(menuButton);
        table.row();
        stage.addActor(table);



        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.mainScreen);
            }
        });




        batch = stage.getBatch();
        font = new BitmapFont();
        font.setColor(Color.BLUE);
        font.setScale(2.0f);


        game.mySensors.registerSensorListeners();

        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        calibrating = true;



        game.myAudio.start(Gdx.files.external(game.myState.myPlayList.songPaths.get(0)), false);
        //first lvl then player
        game.myLevel.loadLevel();
        game.myPlayer.setCam();
    }



    @Override
    public void hide (){
        stage.dispose();
       // batch.dispose();
        modelBatch.dispose();
        environment.clear();

        game.mySensors.unregisterSensorListeners();
        game.myAudio.stop();

        game.myLevel.disposeLevel();


        // batch.dispose();
       // font.dispose();


    }

    @Override
    public void pause (){
        game.myAudio.stop();
    }

    @Override
    public void resume (){
        game.myAudio.start(Gdx.files.external(game.myState.myPlayList.songPaths.get(0)), false);
    }

    @Override
    public void dispose (){
    }
}
