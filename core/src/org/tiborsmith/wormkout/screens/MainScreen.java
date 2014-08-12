package org.tiborsmith.wormkout.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

import net.dermetfan.utils.libgdx.scene2d.ui.FileChooser;

import org.tiborsmith.wormkout.Wormkout;
import org.tiborsmith.wormkout.utils.MyFileChooser;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by tibor on 5.8.14.
 */
public class MainScreen implements Screen {
    Wormkout game;
    public MainScreen(Wormkout game){ this.game = game; }


    private Stage stage;
    private ModelBatch modelBatch;
    private Environment environment;

    private Window settingsWindow;
    private Window mainMenuWindow;
    private Window musicWindow;
    private Window playWindow;


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
        modelBatch.render(game.myLevel.gates, environment);
        modelBatch.end();
        game.myLevel.update(delta);


        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void show (){
        game.splashScreen.dispose();
        stage = new Stage();
        stage.setViewport(game.myAssets.viewport);
        Gdx.input.setInputProcessor(stage);

        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        game.myAudio.start(Gdx.files.external(game.myState.myPlayList.songPaths.get(0)), false);
        //first lvl then player
        game.myLevel.loadLevel();
        game.myPlayer.setCam();

        mainMenu();
        settingsMenu();
        musicMenu();
        playMenu();



    }




    private void playMenu(){
        TextButton closeButton = new TextButton(" x ",game.myAssets.skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playWindow.setVisible(false);
                mainMenuWindow.setVisible(true);
            }
        });



        Table mainLvlTable = new Table();
        ScrollPane scroll = new ScrollPane(mainLvlTable.top());

        for (int i=0 ; i < game.myState.levelStates.lvls.size; i++){
            //play button or lock label
            if (!game.myState.levelStates.lvls.get(i).locked){
                String buttonString;
                if (game.myState.levelStates.lvls.get(i).finished){
                    float time = game.myState.levelStates.lvls.get(i).bestTime;
                    buttonString = game.myState.levelStates.lvls.get(i).name +
                            "  [ best time: "+ (int)time/60 + "m " + (int)time%60 + "s ]";
                }
                else {
                    buttonString = game.myState.levelStates.lvls.get(i).name + "  [ best time: --m --s ]";
                }
                TextButton lvlButton = new TextButton(buttonString,game.myAssets.skin);
                final int j = i;
                lvlButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        game.myState.currentLevel = j;
                        game.setScreen(game.gameScreen);
                    }
                });
                mainLvlTable.add(lvlButton);
            }
            else {
                Label label = new Label(game.myState.levelStates.lvls.get(i).name + "  [ locked ]",game.myAssets.skin);
                label.setAlignment(Align.center);
                mainLvlTable.add(label).fill();
            }
            // empty column (spacing)
            mainLvlTable.add(new Label(" ",game.myAssets.skin)).fill();
            // description label
            Label lvlLabel = new Label(game.myState.levelStates.lvls.get(i).description,game.myAssets.skin);
            mainLvlTable.add(lvlLabel).expandX().fill();


            mainLvlTable.row();
            mainLvlTable.add(new Label(" ",game.myAssets.skin)).expandX().fill().row();


        }

        playWindow = new Window("Wormkout - choose level",game.myAssets.skin);
        playWindow.setVisible(false);
        playWindow.getButtonTable().add(closeButton).height(musicWindow.getPadTop());
        playWindow.setWidth(stage.getWidth()/1.0272f);
        playWindow.setHeight(stage.getHeight()/1.0272f);
        playWindow.setCenterPosition(stage.getWidth()/2,stage.getHeight()/2);
        playWindow.add(scroll).expand().fill();
        stage.addActor(playWindow);

    }

    private void musicMenu(){
        TextButton closeButton = new TextButton(" x ",game.myAssets.skin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                musicWindow.setVisible(false);
                mainMenuWindow.setVisible(true);
            }
        });



        final CheckBox pDCB = new CheckBox("Play also the default music", game.myAssets.skin); // play default checkbox
        if (game.myState.myPlayList.playDefault)
            pDCB.setChecked(true);
        else
            pDCB.setChecked(false);
        pDCB.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (pDCB.isChecked()){
                    game.myState.myPlayList.playDefault = true;
                    game.myState.myPlayList.savePlayList();
                }
                else if (game.myState.myPlayList.numOfDefaultSong == game.myState.myPlayList.songPaths.size){
                    new Dialog("", game.myAssets.skin) {}.text("You need to add some of your own music" +
                            "\n before you can disable default one.").button("Ok").show(stage);
                    pDCB.setChecked(true);
                    game.myState.myPlayList.playDefault = true;
                    game.myState.myPlayList.savePlayList();
                }
                else {
                    game.myState.myPlayList.playDefault = false;
                    game.myState.myPlayList.savePlayList();
                }
            }
        });




        final List playlist = new List(game.myAssets.skin);
        playlist.setItems(game.myState.myPlayList.songNames);
        ScrollPane scroll = new ScrollPane(playlist);
        TextButton removeButton = new TextButton(" Remove song ",game.myAssets.skin);
        removeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int i = playlist.getSelectedIndex();
                if (i < game.myState.myPlayList.numOfDefaultSong){
                    new Dialog("", game.myAssets.skin) {
                   }.text("You can't remove default music...").button("Ok").show(stage);
                }
                else {
                    game.myState.myPlayList.songPaths.removeIndex(i);
                    game.myState.myPlayList.songNames.removeIndex(i);
                    playlist.setItems(game.myState.myPlayList.songNames);
                    pDCB.setChecked(true);
                    game.myState.myPlayList.savePlayList();
                }
            }
        });
        Table playlistTable = new Table();
        playlistTable.add(scroll).expand().fill();
        playlistTable.row();
        playlistTable.add(removeButton).expandX();
        playlistTable.row();


        MyFileChooser fileChooser = new MyFileChooser(game.myAssets.skin, new FileChooser.Listener() {
            @Override
            public void choose(FileHandle file) {
                game.myState.myPlayList.songPaths.add(file.path());
                game.myState.myPlayList.songNames.add(file.name());
                playlist.setItems(game.myState.myPlayList.songNames);
                game.myState.myPlayList.savePlayList();
            }

            @Override
            public void choose(Array<FileHandle> files) {}

            @Override
            public void cancel() {}
        });
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.exists()){
                    if (pathname.isDirectory())
                        return true;
                    else if (pathname.getName().endsWith(".mp3"))
                        return true;
                    else
                        return false;
                    }
                return false;
            }
        };
        fileChooser.setFileFilter(filter);
        fileChooser.add(Gdx.files.external(""));
        //disables multiple selection
        fileChooser.getTree().getSelection().setMultiple(false);






        musicWindow = new Window("Wormkout - Music playlist",game.myAssets.skin);
        SplitPane divideMusicWindow = new SplitPane(playlistTable,fileChooser,false,game.myAssets.skin);
        musicWindow.setVisible(false);
        musicWindow.getButtonTable().add(closeButton).height(musicWindow.getPadTop());
        musicWindow.setWidth(stage.getWidth() / 1.0272f);
        musicWindow.setHeight(stage.getHeight() / 1.0272f);
        musicWindow.setCenterPosition(stage.getWidth() / 2, stage.getHeight() / 2);
        musicWindow.add(divideMusicWindow).expand().fill().row();
        musicWindow.add(pDCB);
        stage.addActor(musicWindow);



    }

    /**
     * prepares main menu window and adds it to stage
     */
    private void settingsMenu(){
        TextButton menuButton = new TextButton("Go back to menu",game.myAssets.skin);
        TextButton defaultButton = new TextButton("Restore default settings",game.myAssets.skin);
        TextButton audioTestButton = new TextButton("AudioTestscreen",game.myAssets.skin);
        Slider volumeSlider = new Slider(0.0f,1.0f,0.02f,false,game.myAssets.skin);


        settingsWindow = new Window("Wormkout - Settings",game.myAssets.skin);
        settingsWindow.setVisible(false);
        settingsWindow.setWidth(stage.getWidth()/1.0272f);
        settingsWindow.setHeight(stage.getHeight()/1.0272f);
        settingsWindow.setCenterPosition(stage.getWidth()/2,stage.getHeight()/2);
        settingsWindow.add(volumeSlider).width(400).height(50).expand().colspan(2);
        settingsWindow.row();
        settingsWindow.add(menuButton).width(200).height(100).expand().colspan(2);
        settingsWindow.row();
        settingsWindow.add(defaultButton).width(200).height(100).expand().colspan(2);
        settingsWindow.row();
        settingsWindow.add(audioTestButton).width(200).height(100).expand().colspan(2);
        settingsWindow.debug();
        stage.addActor(settingsWindow);



        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.setVisible(false);
                mainMenuWindow.setVisible(true);
            }
        });



        defaultButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.myState.mySettings.restoreDefaultSettings();
            }
        });

        audioTestButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.audioTestScreen);
            }
        });

    }


    /**
     * prepares main menu window and adds it to stage
     */
    private void mainMenu(){
        TextButton playButton = new TextButton("    Play    ",game.myAssets.skin);
        final TextButton settingsButton = new TextButton("  Settings  ",game.myAssets.skin);
        TextButton musicButton = new TextButton("    Music    ",game.myAssets.skin);
        TextButton highScoresButton = new TextButton("High Scores",game.myAssets.skin);
        TextButton achievementsButton = new TextButton("Achievements",game.myAssets.skin);
        TextButton exitButton = new TextButton("    Exit    ",game.myAssets.skin);
        TextButton creditsButton = new TextButton("    Credits    ",game.myAssets.skin);
        TextButton helpButton = new TextButton("    Help    ",game.myAssets.skin);


        mainMenuWindow = new Window(" Wormkout ",game.myAssets.skin);
        mainMenuWindow.setWidth(stage.getWidth()/1.0272f);
        mainMenuWindow.setHeight(stage.getHeight()/1.0272f);
        mainMenuWindow.setCenterPosition(stage.getWidth()/2,stage.getHeight()/2);

        mainMenuWindow.add(playButton).expand().colspan(2);
        mainMenuWindow.row();
        mainMenuWindow.add(settingsButton).expand();
        mainMenuWindow.add(musicButton).expand();
        mainMenuWindow.row();
        mainMenuWindow.add(highScoresButton).expand();
        mainMenuWindow.add(achievementsButton).expand();
        mainMenuWindow.row();
        mainMenuWindow.add(creditsButton).expand();
        mainMenuWindow.add(helpButton).expand();
        mainMenuWindow.row();
        mainMenuWindow.add(exitButton).expand().colspan(2);
        stage.addActor(mainMenuWindow);




        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                playWindow.setVisible(true);
                mainMenuWindow.setVisible(false);
                //game.setScreen(game.gameScreen);
            }
        });

        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.setVisible(true);
                mainMenuWindow.setVisible(false);
            }
        });

        musicButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                musicWindow.setVisible(true);
                mainMenuWindow.setVisible(false);
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
    }


    @Override
    public void resize (int width, int height){
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void hide (){
        stage.dispose();

        modelBatch.dispose();
        environment.clear();
        game.myAudio.stop();

        game.myLevel.disposeLevel();

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
