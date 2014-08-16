package org.tiborsmith.wormkout.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

import net.dermetfan.utils.libgdx.scene2d.ui.FileChooser;

import org.tiborsmith.wormkout.Wormkout;
import org.tiborsmith.wormkout.steady.MyFileChooser;

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
    private Window mainWindow;
    private Window musicWindow;
    private Window levelWindow;
    private Window creditsWindow;
    private Window helpWindow;



    @Override
    public void render (float delta){
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);


        modelBatch.begin(game.myPlayer.cam);
        modelBatch.render(game.myLevel.gates, environment);
        modelBatch.end();
        game.myLevel.update(delta);


        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

        if (!game.myAudio.playing)
            game.myAudio.startMusic();

        if (game.welcomeBack){
            game.signInContorl();
            game.welcomeBack = false;
        }

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


        //first lvl then player
        game.myLevel.loadLevel();
        game.myPlayer.initPlayer();
        game.myPlayer.speed=0;



        //separated code for windows on Mainscreen
        mainMenu();
        settingsMenu();
        musicMenu();
        levelMenu();
        helpMenu();
        creditsMenu();

        // from game screen is first window levelWindow
        if (game.playMenu){
            mainWindow.setVisible(false);
            levelWindow.setVisible(true);
        }

        //listener for back key on main MainScreen
        stage.addListener(new InputListener(){
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if(keycode == Input.Keys.BACK){
                    if(!mainWindow.isVisible()){
                        mainWindow.setVisible(true);
                        levelWindow.setVisible(false);
                        musicWindow.setVisible(false);
                        helpWindow.setVisible(false);
                        creditsWindow.setVisible(false);
                        settingsWindow.setVisible(false);
                    }
                    else {
                        Gdx.app.exit();
                    }
                    return true;
                }
                else
                    return false;
            }
        });

    }


    private void creditsMenu(){
        creditsWindow = makeWindow("Wormkout - Credits");
        creditsWindow.getButtonTable().add(makeCloseButton()).height(levelWindow.getPadTop());

    }


    private void helpMenu(){
        helpWindow = makeWindow("Wormkout - Help");
        helpWindow.getButtonTable().add(makeCloseButton()).height(levelWindow.getPadTop());

    }



    private void levelMenu(){
        levelWindow = makeWindow("Wormkout - Level Chooser");
        levelWindow.getButtonTable().add(makeCloseButton()).height(levelWindow.getPadTop());


        Table levelTable = new Table();
        ScrollPane scroll = new ScrollPane(levelTable.top());
        levelTable.add(new Label(" ", game.myAssets.skin)).colspan(2).expandX().fill().row();

        for (int i=0 ; i < game.levelStates.lvls.size; i++){
            //play button or lock label
            if (!game.levelStates.lvls.get(i).locked){
                String buttonString;
                if (game.levelStates.lvls.get(i).finished){
                    float time = game.levelStates.lvls.get(i).bestTime;
                    buttonString = game.levelStates.lvls.get(i).name +
                            "  [best time: "+ (int)time/60 + "m " + (int)time%60 + "s]";
                }
                else {
                    buttonString = game.levelStates.lvls.get(i).name + "  [best time: --m --s]";
                }
                TextButton lvlButton = new TextButton(buttonString,game.myAssets.skin);
                final int j = i;
                lvlButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        game.currentLevel = j;
                        game.setScreen(game.gameScreen);
                    }
                });
                levelTable.add(lvlButton).width(400).align(Align.left);
            }
            else {
                Label label = new Label(game.levelStates.lvls.get(i).name + "  [locked]",game.myAssets.skin);
                label.setAlignment(Align.center);
                levelTable.add(label).width(400).align(Align.left);
            }
            Label lvlLabel = new Label(" " + game.levelStates.lvls.get(i).description,game.myAssets.skin);
            lvlLabel.setAlignment(Align.left);
            levelTable.add(lvlLabel).expandX().align(Align.left).fill().row();
            levelTable.add(new Label(" ", game.myAssets.skin)).expandX().colspan(2).fill().row();
        }
        levelTable.add(new Label(" ", game.myAssets.skin)).expand().colspan(2).fill().row();

        levelWindow.add(scroll).expand().fill();
    }


    private void musicMenu(){
        musicWindow = makeWindow("Wormkout - Music Playlist");
        musicWindow.getButtonTable().add(makeCloseButton()).height(musicWindow.getPadTop());

        final CheckBox pDCB = new CheckBox("Play the default music.", game.myAssets.skin);
        if (game.myPlayList.playDefault)
            pDCB.setChecked(true);
        else
            pDCB.setChecked(false);
        pDCB.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (pDCB.isChecked()){
                    game.myPlayList.playDefault = true;
                    game.myPlayList.savePlayList();
                }
                else if (game.myPlayList.numOfDefaultSong == game.myPlayList.songPaths.size){
                    new Dialog("", game.myAssets.skin){}.text("You need to add some of your own music" +
                            "\n before you can disable the default one.").button("  Ok  ").show(stage);
                    pDCB.setChecked(true);
                    game.myPlayList.playDefault = true;
                    game.myPlayList.savePlayList();
                }
                else {
                    game.myPlayList.playDefault = false;
                    game.myPlayList.savePlayList();
                }
            }
        });

        final List playlist = new List(game.myAssets.skin);
        playlist.setItems(game.myPlayList.songNames);
        ScrollPane scroll = new ScrollPane(playlist);
        TextButton removeButton = new TextButton(" Remove song ",game.myAssets.skin);
        removeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int i = playlist.getSelectedIndex();
                if (i < game.myPlayList.numOfDefaultSong){
                    new Dialog("", game.myAssets.skin) {
                   }.text("You can't remove default music...").button("  Ok  ").show(stage);
                }
                else {
                    game.myPlayList.songPaths.removeIndex(i);
                    game.myPlayList.songNames.removeIndex(i);
                    playlist.setItems(game.myPlayList.songNames);
                    if (game.myPlayList.songPaths.size == game.myPlayList.numOfDefaultSong) {
                        pDCB.setChecked(true);
                        game.myPlayList.playDefault = true;
                    }
                    game.myPlayList.savePlayList();
                }
            }
        });
        Table playlistTable = new Table();
        playlistTable.add(scroll).expand().fill().row();
        playlistTable.add(removeButton).expandX().row();
        playlistTable.add(pDCB);


        MyFileChooser fileChooser = new MyFileChooser(game.myAssets.skin, new FileChooser.Listener() {
            @Override
            public void choose(FileHandle file) {
                game.myPlayList.songPaths.add(file.path());
                game.myPlayList.songNames.add(file.name());
                playlist.setItems(game.myPlayList.songNames);
                game.myPlayList.savePlayList();
            }

            @Override
            public void choose(Array<FileHandle> files) {}

            @Override
            public void cancel() {}
        });
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.exists() && (pathname.isDirectory() || pathname.getName().endsWith(".mp3"))){
                    return true;
                }
                return false;
            }
        };
        fileChooser.setFileFilter(filter);
        fileChooser.add(Gdx.files.external("")); // root for tree
        fileChooser.getTree().getSelection().setMultiple(false);  //disables multiple selection


        SplitPane dividedMusicWindow = new SplitPane(playlistTable,fileChooser,false,game.myAssets.skin);
        musicWindow.add(dividedMusicWindow).expand().fill().row();
    }

    /**
     * prepares main menu window and adds it to stage
     */
    private void settingsMenu(){
        settingsWindow = makeWindow("Workmout - Settings");
        settingsWindow.getButtonTable().add(makeCloseButton()).height(settingsWindow.getPadTop());

        //setup for GPGS login logout button
        final Label gpgsLabel;
        final TextButton gpgsButton;
        if (game.myActionResolver.isSignedInGPGS()) {
            gpgsButton = new TextButton("Sign out", game.myAssets.skin);
            gpgsLabel = new Label("Google Play Game Services \n [You are successfully signed in.]",game.myAssets.skin);
        }
        else{
            gpgsButton = new TextButton("Sign in", game.myAssets.skin);
            gpgsLabel = new Label("Google Play Game Services \n [You are signed out at this moment.]",game.myAssets.skin);
        }
        gpgsLabel.setAlignment(Align.center);
        gpgsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                if (game.myActionResolver.isSignedInGPGS()) {
                    gpgsButton.getLabel().setText("Sign in");
                    gpgsLabel.setText("Google Play Game Services \n [You are signed out at this moment]");
                    game.myActionResolver.signOutGPGS();
                    new Dialog("", game.myAssets.skin) {
                    }.text("You have just sign out from GPGS.\n" +
                            "With this setting you can't use LeaderBoards and Achievements.").button("  Ok  ").show(stage);
                }
                else {
                    gpgsButton.getLabel().setText("Sign out");
                    gpgsLabel.setText("Google Play Game Services \n [You are successfully sign in.]");
                    game.myActionResolver.signInGPGS();
                }
            }
        });

        //settings for sound effects and music volume
        final Label musicLabel = new Label("Music volume",game.myAssets.skin);
        final Label soundLabel = new Label("Sound effects volume",game.myAssets.skin);
        musicLabel.setAlignment(Align.center);
        soundLabel.setAlignment(Align.center);
        final Slider musicSlider = new Slider(0.0f,1.0f,0.02f,false,game.myAssets.skin);
        final Slider soundSlider = new Slider(0.0f,1.0f,0.02f,false,game.myAssets.skin);
        musicSlider.setValue(game.mySettings.musicVolume);
        soundSlider.setValue(game.mySettings.soundVolume);
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.mySettings.musicVolume = musicSlider.getValue();
                game.mySettings.saveSettings();
                game.setMusicVolume(game.mySettings.musicVolume);
            }
        });
        soundSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.mySettings.soundVolume = soundSlider.getValue();
                game.mySettings.saveSettings();
            }
        });


        settingsWindow.add(musicLabel).right().fill().expandX().row();
        settingsWindow.add(musicSlider).width(stage.getWidth() / 2).row();
        settingsWindow.add(new Label(" ",game.myAssets.skin)).expandX().fill().row();
        settingsWindow.add(new Label(" ",game.myAssets.skin)).expandX().fill().row();

        settingsWindow.add(soundLabel).right().fill().expandX().row();
        settingsWindow.add(soundSlider).width(stage.getWidth() / 2).row();
        settingsWindow.add(new Label(" ",game.myAssets.skin)).expandX().fill().row();
        settingsWindow.add(new Label(" ",game.myAssets.skin)).expandX().fill().row();

        settingsWindow.add(gpgsLabel).right().fill().expandX().row();
        settingsWindow.add(new Label(" ",game.myAssets.skin)).expandX().fill().row();
        settingsWindow.add(gpgsButton).width(200).row();
    }


    /**
     * prepares main menu window and adds it to stage
     */
    private void mainMenu(){
        mainWindow = makeWindow("Workmout");
        mainWindow.setVisible(true);

        TextButton playButton = new TextButton("    Play    ",game.myAssets.skin);
        //playButton.setStyle(game.myAssets.skin.get("bold", TextButton.TextButtonStyle.class));
        TextButton settingsButton = new TextButton("  Settings  ",game.myAssets.skin);
        TextButton musicButton = new TextButton("    Music    ",game.myAssets.skin);
        TextButton leaderboardsButton = new TextButton("Leaderboards",game.myAssets.skin);
        TextButton achievementsButton = new TextButton("Achievements",game.myAssets.skin);
        TextButton exitButton = new TextButton("    Exit    ",game.myAssets.skin);
        TextButton creditsButton = new TextButton("    Credits    ",game.myAssets.skin);
        TextButton helpButton = new TextButton("    Help    ",game.myAssets.skin);


        float wFB = 200;
        mainWindow.add(playButton).width(wFB).expand().colspan(2).row();
        mainWindow.add(settingsButton).width(wFB).expand();
        mainWindow.add(musicButton).width(wFB).expand().row();
        mainWindow.add(leaderboardsButton).width(wFB).expand();
        mainWindow.add(achievementsButton).width(wFB).expand().row();
        mainWindow.add(creditsButton).width(wFB).expand();
        mainWindow.add(helpButton).width(wFB).expand().row();
        mainWindow.add(exitButton).width(wFB).expand().colspan(2);


        //listeners for buttons
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                levelWindow.setVisible(true);
                mainWindow.setVisible(false);
            }
        });

        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.setVisible(true);
                mainWindow.setVisible(false);
            }
        });

        musicButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                musicWindow.setVisible(true);
                mainWindow.setVisible(false);
            }
        });

        leaderboardsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.myActionResolver.isSignedInGPGS())
                    game.myActionResolver.getLeaderboardGPGS();
                else {
                    Dialog dialog = new Dialog("", game.myAssets.skin);
                    dialog.text("You need to be logged in with GPGS (Google Play Game Services),\n" +
                            "before you can use Leaderboards. Do you wish to sign in now?");
                    TextButton yesButton = new TextButton("  Yes  ", game.myAssets.skin);
                    dialog.button(yesButton, true);
                    TextButton noButton = new TextButton("  No  ", game.myAssets.skin);
                    dialog.button(noButton, false);
                    dialog.show(stage);

                    yesButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            game.myActionResolver.signInGPGS();
                        }
                    });
                }
            }
        });

        achievementsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (game.myActionResolver.isSignedInGPGS())
                    game.myActionResolver.getAchivementsGPGS();
                else {
                    Dialog dialog = new Dialog("", game.myAssets.skin);
                    dialog.text("You need to be logged in with GPGS (Google Play Game Services),\n" +
                            "before you can use Achievements. Do you wish to sign in now?");
                    TextButton yesButton = new TextButton("  Yes  ", game.myAssets.skin);
                    dialog.button(yesButton, true);
                    TextButton noButton = new TextButton("  No  ", game.myAssets.skin);
                    dialog.button(noButton, false);
                    dialog.show(stage);

                    yesButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            game.myActionResolver.signInGPGS();
                        }
                    });
                }
            }
        });

        creditsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                creditsWindow.setVisible(true);
                mainWindow.setVisible(false);
            }
        });

        helpButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                helpWindow.setVisible(true);
                mainWindow.setVisible(false);
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
    }


    /**
     * @param title test in title bar
     * @return returns new window with prepared common parameters
     */
    private Window makeWindow(String title){
        Window window = new Window(title,game.myAssets.skin);
        window.setVisible(false);
        window.setWidth(stage.getWidth() / 1.0272f);
        window.setHeight(stage.getHeight() / 1.0272f);
        window.setCenterPosition(stage.getWidth() / 2, stage.getHeight() / 2);
        window.setMovable(false);
        stage.addActor(window);
        return window;
    }


    /**
     * @return closeButton for with common code for all secondary windows
     */
    private TextButton makeCloseButton(){
        TextButton closeButton = new TextButton(" Back ",game.myAssets.skin);
        closeButton.getLabel().setAlignment(Align.center);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                levelWindow.setVisible(false);
                settingsWindow.setVisible(false);
                musicWindow.setVisible(false);
                creditsWindow.setVisible(false);
                helpWindow.setVisible(false);
                mainWindow.setVisible(true);
            }
        });
        return closeButton;
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
        game.myAudio.stopMusic();
        game.myLevel.disposeLevel();
    }

    @Override
    public void pause (){
        game.myAudio.stopMusic();
    }

    @Override
    public void resume (){ }

    @Override
    public void dispose (){ }
}
