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
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

import net.dermetfan.utils.libgdx.scene2d.ui.FileChooser;

import org.tiborsmith.wormkout.Wormkout;
import org.tiborsmith.wormkout.steady.ShaderCheckBox;
import org.tiborsmith.wormkout.steady.ShaderDialog;
import org.tiborsmith.wormkout.steady.ShaderFileChooser;
import org.tiborsmith.wormkout.steady.ShaderLabel;
import org.tiborsmith.wormkout.steady.ShaderList;
import org.tiborsmith.wormkout.steady.ShaderTextButton;
import org.tiborsmith.wormkout.steady.ShaderWindow;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by tibor on 5.8.14.
 */
public class MainScreen implements Screen {
    Wormkout g;
    public MainScreen(Wormkout g){ this.g = g; }


    private Stage stage;
    private ModelBatch modelBatch;
    private Environment environment;

    private ShaderWindow settingsWindow;
    private ShaderWindow mainWindow;
    private ShaderWindow musicWindow;
    private ShaderWindow levelWindow;
    private ShaderWindow creditsWindow;
    private ShaderWindow helpWindow;



    @Override
    public void render (float delta){
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);


        modelBatch.begin(g.player.cam);
        modelBatch.render(g.level.gates, environment);
        modelBatch.end();
        g.level.update(delta);


        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

        if (!g.audio.playing)
            g.audio.startMusic();

        if (g.welcomeBack){
            g.signInContorl();
            g.welcomeBack = false;
        }

    }

    @Override
    public void show (){
        g.splashScreen.dispose();
        stage = new Stage();
        stage.setViewport(g.assets.viewport);
        Gdx.input.setInputProcessor(stage);

        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));


        //first lvl then player
        g.level.loadLevel();
        g.player.initPlayer();
        g.player.speed=0;



        //separated code for MyWindows on Mainscreen
        mainMenu();
        settingsMenu();
        musicMenu();
        levelMenu();
        helpMenu();
        creditsMenu();

        // from g screen is first window levelWindow
        if (g.playMenu){
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
        levelTable.add(new ShaderLabel(" ", g.assets.skin)).colspan(2).expandX().fill().row();

        for (int i=0 ; i < g.levelStates.lvls.size; i++){
            //play button or lock label
            if (!g.levelStates.lvls.get(i).locked){
                String buttonString;
                if (g.levelStates.lvls.get(i).finished){
                    float time = g.levelStates.lvls.get(i).bestTime;
                    buttonString = g.levelStates.lvls.get(i).name +
                            "  [best time: "+ (int)time/60 + "m " + (int)time%60 + "s]";
                }
                else {
                    buttonString = g.levelStates.lvls.get(i).name + "  [best time: --m --s]";
                }
                ShaderTextButton lvlButton = new ShaderTextButton(buttonString, g.assets.skin);
                final int j = i;
                lvlButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        g.currentLevel = j;
                        g.setScreen(g.gameScreen);
                    }
                });
                levelTable.add(lvlButton).width(400).align(Align.left);
            }
            else {
                ShaderLabel label = new ShaderLabel(g.levelStates.lvls.get(i).name + "  [locked]", g.assets.skin);
                label.setAlignment(Align.center);
                levelTable.add(label).width(400).align(Align.left);
            }
            ShaderLabel lvlLabel = new ShaderLabel(" " + g.levelStates.lvls.get(i).description, g.assets.skin);
            lvlLabel.setAlignment(Align.left);
            levelTable.add(lvlLabel).expandX().align(Align.left).fill().row();
            levelTable.add(new ShaderLabel(" ", g.assets.skin)).expandX().colspan(2).fill().row();
        }
        levelTable.add(new ShaderLabel(" ", g.assets.skin)).expand().colspan(2).fill().row();

        levelWindow.add(scroll).expand().fill();
    }


    private void musicMenu(){
        musicWindow = makeWindow("Wormkout - Music Playlist");
        musicWindow.getButtonTable().add(makeCloseButton()).height(musicWindow.getPadTop());

        final ShaderCheckBox pDCB = new ShaderCheckBox("Play the default music.", g.assets.skin);
        if (g.playList.playDefault)
            pDCB.setChecked(true);
        else
            pDCB.setChecked(false);
        pDCB.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (pDCB.isChecked()){
                    g.playList.playDefault = true;
                    g.playList.savePlayList();
                }
                else if (g.playList.numOfDefaultSong == g.playList.songPaths.size){
                    new ShaderDialog("", g.assets.skin){}.text("You need to add some of your own music" +
                            "\n before you can disable the default one.").button("  Ok  ").show(stage);
                    pDCB.setChecked(true);
                    g.playList.playDefault = true;
                    g.playList.savePlayList();
                }
                else {
                    g.playList.playDefault = false;
                    g.playList.savePlayList();
                }
            }
        });

        final ShaderList playlist = new ShaderList(g.assets.skin);
        playlist.setItems(g.playList.songNames);
        ScrollPane scroll = new ScrollPane(playlist);
        ShaderTextButton removeButton = new ShaderTextButton(" Remove song ", g.assets.skin);
        removeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int i = playlist.getSelectedIndex();
                if (i < g.playList.numOfDefaultSong){
                    new ShaderDialog("", g.assets.skin) {
                   }.text("You can't remove default music...").button("  Ok  ").show(stage);
                }
                else {
                    g.playList.songPaths.removeIndex(i);
                    g.playList.songNames.removeIndex(i);
                    playlist.setItems(g.playList.songNames);
                    if (g.playList.songPaths.size == g.playList.numOfDefaultSong) {
                        pDCB.setChecked(true);
                        g.playList.playDefault = true;
                    }
                    g.playList.savePlayList();
                }
            }
        });
        Table playlistTable = new Table();
        playlistTable.add(scroll).expand().fill().row();
        playlistTable.add(removeButton).expandX().row();
        playlistTable.add(pDCB);


        ShaderFileChooser fileChooser = new ShaderFileChooser(g.assets.skin, new FileChooser.Listener() {
            @Override
            public void choose(FileHandle file) {
                g.playList.songPaths.add(file.path());
                g.playList.songNames.add(file.name());
                playlist.setItems(g.playList.songNames);
                g.playList.savePlayList();
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
        fileChooser.add(Gdx.files.external(""),g.assets.skin); // root for tree
        fileChooser.getTree().getSelection().setMultiple(false);  //disables multiple selection


        SplitPane dividedMusicWindow = new SplitPane(playlistTable,fileChooser,false, g.assets.skin);
        musicWindow.add(dividedMusicWindow).expand().fill().row();
    }

    /**
     * prepares main menu window and adds it to stage
     */
    private void settingsMenu(){
        settingsWindow = makeWindow("Workmout - Settings");
        settingsWindow.getButtonTable().add(makeCloseButton()).height(settingsWindow.getPadTop());

        //setup for GPGS login logout button
        final ShaderLabel gpgsLabel;
        final ShaderTextButton gpgsButton;
        if (g.myActionResolver.isSignedInGPGS()) {
            gpgsButton = new ShaderTextButton("Sign out", g.assets.skin);
            gpgsLabel = new ShaderLabel("Google Play Game Services \n [You are successfully signed in.]", g.assets.skin);
        }
        else{
            gpgsButton = new ShaderTextButton("Sign in", g.assets.skin);
            gpgsLabel = new ShaderLabel("Google Play Game Services \n [You are signed out at this moment.]", g.assets.skin);
        }
        gpgsLabel.setAlignment(Align.center);
        gpgsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                if (g.myActionResolver.isSignedInGPGS()) {
                    gpgsButton.getLabel().setText("Sign in");
                    gpgsLabel.setText("Google Play Game Services \n [You are signed out at this moment]");
                    g.myActionResolver.signOutGPGS();
                    new ShaderDialog("", g.assets.skin) {
                    }.text("You have just sign out from GPGS.\n" +
                            "With this setting you can't use LeaderBoards and Achievements.").button("  Ok  ").show(stage);
                }
                else {
                    gpgsButton.getLabel().setText("Sign out");
                    gpgsLabel.setText("Google Play Game Services \n [You are successfully sign in.]");
                    g.myActionResolver.signInGPGS();
                }
            }
        });

        //settings for sound effects and music volume
        final ShaderLabel musicLabel = new ShaderLabel("Music volume", g.assets.skin);
        final ShaderLabel soundLabel = new ShaderLabel("Sound effects volume", g.assets.skin);
        musicLabel.setScale(1.0f+g.settings.musicVolume);
        soundLabel.setScale(1.0f+g.settings.soundVolume);
        musicLabel.setAlignment(Align.center);
        soundLabel.setAlignment(Align.center);
        final Slider musicSlider = new Slider(0.0f,1.0f,0.02f,false, g.assets.skin);
        final Slider soundSlider = new Slider(0.0f,1.0f,0.02f,false, g.assets.skin);
        musicSlider.setValue(g.settings.musicVolume);
        soundSlider.setValue(g.settings.soundVolume);
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                g.settings.musicVolume = musicSlider.getValue();
                g.settings.saveSettings();
                g.setMusicVolume(g.settings.musicVolume);
                musicLabel.setScale(1.0f+musicSlider.getValue());
            }
        });
        soundSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                g.settings.soundVolume = soundSlider.getValue();
                g.settings.saveSettings();
                soundLabel.setScale(1.0f + soundSlider.getValue());
            }
        });



        Table settingTable = new Table(g.assets.skin);
        settingTable.add(musicLabel).right().fill().expandX().row();
        settingTable.add(musicSlider).width(stage.getWidth() / 2).row();
        settingTable.add(new ShaderLabel(" ", g.assets.skin)).expandX().fill().row();
        settingTable.add(new ShaderLabel(" ", g.assets.skin)).expandX().fill().row();

        settingTable.add(soundLabel).right().fill().expandX().row();
        settingTable.add(soundSlider).width(stage.getWidth() / 2).row();
        settingTable.add(new ShaderLabel(" ", g.assets.skin)).expandX().fill().row();
        settingTable.add(new ShaderLabel(" ", g.assets.skin)).expandX().fill().row();

        settingTable.add(gpgsLabel).right().fill().expandX().row();
        settingTable.add(new ShaderLabel(" ", g.assets.skin)).expandX().fill().row();
        settingTable.add(gpgsButton).width(200).row();

        ScrollPane scroll = new ScrollPane(settingTable);
        settingsWindow.add(scroll);
    }


    /**
     * prepares main menu window and adds it to stage
     */
    private void mainMenu(){
        mainWindow = makeWindow("Workmout");
        mainWindow.setVisible(true);

        ShaderTextButton playButton = new ShaderTextButton("    Play    ", g.assets.skin);
        ShaderTextButton settingsButton = new ShaderTextButton("  Settings  ", g.assets.skin);
        ShaderTextButton musicButton = new ShaderTextButton("    Music    ", g.assets.skin);
        ShaderTextButton leaderboardsButton = new ShaderTextButton("Leaderboards", g.assets.skin);
        ShaderTextButton achievementsButton = new ShaderTextButton("Achievements", g.assets.skin);
        ShaderTextButton exitButton = new ShaderTextButton("    Exit    ", g.assets.skin);
        ShaderTextButton creditsButton = new ShaderTextButton("    Credits    ", g.assets.skin);
        ShaderTextButton helpButton = new ShaderTextButton("    Help    ", g.assets.skin);


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
                if (g.myActionResolver.isSignedInGPGS())
                    g.myActionResolver.getLeaderboardGPGS();
                else {
                    ShaderDialog dialog = new ShaderDialog("", g.assets.skin);
                    dialog.text("You need to be logged in with GPGS (Google Play Game Services),\n" +
                            "before you can use Leaderboards. Do you wish to sign in now?");
                    ShaderTextButton yesButton = new ShaderTextButton("  Yes  ", g.assets.skin);
                    dialog.button(yesButton, true);
                    ShaderTextButton noButton = new ShaderTextButton("  No  ", g.assets.skin);
                    dialog.button(noButton, false);
                    dialog.show(stage);

                    yesButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            g.myActionResolver.signInGPGS();
                        }
                    });
                }
            }
        });

        achievementsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (g.myActionResolver.isSignedInGPGS())
                    g.myActionResolver.getAchivementsGPGS();
                else {
                    ShaderDialog dialog = new ShaderDialog("", g.assets.skin);
                    dialog.text("You need to be logged in with GPGS (Google Play Game Services),\n" +
                            "before you can use Achievements. Do you wish to sign in now?");
                    ShaderTextButton yesButton = new ShaderTextButton("  Yes  ", g.assets.skin);
                    dialog.button(yesButton, true);
                    ShaderTextButton noButton = new ShaderTextButton("  No  ", g.assets.skin);
                    dialog.button(noButton, false);
                    dialog.show(stage);

                    yesButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            g.myActionResolver.signInGPGS();
                        }
                    });
                }
            }
        });

        creditsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                creditsWindow.setVisible(true);
                mainWindow.setVisible(false);
            }
        });

        helpButton.addListener(new ClickListener() {
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
    private ShaderWindow makeWindow(String title){
        ShaderWindow window = new ShaderWindow(title, g.assets.skin);
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
    private ShaderTextButton makeCloseButton(){
        ShaderTextButton closeButton = new ShaderTextButton(" Back ", g.assets.skin);
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
        g.audio.stopMusic();
        g.level.disposeLevel();
    }

    @Override
    public void pause (){
        g.audio.stopMusic();
    }

    @Override
    public void resume (){ }

    @Override
    public void dispose (){ }
}
