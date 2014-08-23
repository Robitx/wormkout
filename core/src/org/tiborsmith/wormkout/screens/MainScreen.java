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

import org.tiborsmith.wormkout.Wormkout;
import org.tiborsmith.wormkout.ui.FileChooser;
import org.tiborsmith.wormkout.ui.sCheckBox;
import org.tiborsmith.wormkout.ui.sDialog;
import org.tiborsmith.wormkout.ui.sFileChooser;
import org.tiborsmith.wormkout.ui.sLabel;
import org.tiborsmith.wormkout.ui.sList;
import org.tiborsmith.wormkout.ui.sTextButton;
import org.tiborsmith.wormkout.ui.sWindow;

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

    private sWindow settingsWindow;
    private sWindow mainWindow;
    private sWindow musicWindow;
    private sWindow levelWindow;
    private sWindow creditsWindow;
    private sWindow helpWindow;



    @Override
    public void render (float delta){
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);


        modelBatch.begin(g.player.cam);
        modelBatch.render(g.level.skybox);
        modelBatch.render(g.level.gates, environment);
        modelBatch.end();
        g.level.update(delta);


        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

        if (!g.audio.playing)
            g.audio.startMusic();
    }

    @Override
    public void show (){
        sLabel.setShader(g.assets.fontShader);

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
                        g.tts.say("Bye",g.settings.soundVolume);
                        Gdx.app.exit();
                    }
                    return true;
                }
                else
                    return false;
            }
        });


        if (g.welcomeBack){
            g.signInContorl();
            g.welcomeBack = false;

        }
        if (g.firstLaunch) {
            Dialog(g.str.dFirstWelcome,true);
            g.firstLaunch = false;
        }
        else if (!g.playMenu){
            g.tts.say(g.str.sNormalWelcome, g.settings.soundVolume);
        }

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
        levelTable.add(new sLabel(" ", g.assets.skin)).colspan(2).expandX().fill().row();

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
                sTextButton lvlButton = new sTextButton(buttonString, g.assets.skin);
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
                sLabel label = new sLabel(g.levelStates.lvls.get(i).name + "  [locked]", g.assets.skin);
                label.setAlignment(Align.center);
                levelTable.add(label).width(400).align(Align.left);
            }
            sLabel lvlLabel = new sLabel(" " + g.levelStates.lvls.get(i).description, g.assets.skin);
            lvlLabel.setAlignment(Align.left);
            levelTable.add(lvlLabel).expandX().align(Align.left).fill().row();
            levelTable.add(new sLabel(" ", g.assets.skin)).expandX().colspan(2).fill().row();
        }
        levelTable.add(new sLabel(" ", g.assets.skin)).expand().colspan(2).fill().row();

        levelWindow.add(scroll).expand().fill();
    }


    private void musicMenu(){
        musicWindow = makeWindow("Wormkout - Music Playlist");
        musicWindow.getButtonTable().add(makeCloseButton()).height(musicWindow.getPadTop());

        final sCheckBox pDCB = new sCheckBox("Play the default music.", g.assets.skin);
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
                    Dialog(g.str.dDisableDefaultMusic,true);
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

        final sList playlist = new sList(g.assets.skin,g.assets.fontShader);
        playlist.setItems(g.playList.songNames);
        ScrollPane scroll = new ScrollPane(playlist);
        sTextButton removeButton = new sTextButton("Remove song", g.assets.skin);
        removeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int i = playlist.getSelectedIndex();
                if (i < g.playList.numOfDefaultSong){
                    Dialog(g.str.dAttemptToRemoveDefaultMusic,true);
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
        playlistTable.add(removeButton).width(200).row();
        playlistTable.add(pDCB);


        sFileChooser fileChooser = new sFileChooser(g.assets.skin, new FileChooser.Listener() {
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
        settingsWindow = makeWindow("Wormkout - Settings");
        settingsWindow.getButtonTable().add(makeCloseButton()).height(settingsWindow.getPadTop()).row();

        //setup for GPGS login logout button
        final sLabel gpgsLabel;
        final sTextButton gpgsButton;
        if (g.myActionResolver.isSignedInGPGS()) {
            gpgsButton = new sTextButton("Sign out", g.assets.skin);
            gpgsLabel = new sLabel("Google Play Game Services \n [You are successfully signed in.]", g.assets.skin);
        }
        else{
            gpgsButton = new sTextButton("Sign in", g.assets.skin);
            gpgsLabel = new sLabel("Google Play Game Services \n [You are signed out at this moment.]", g.assets.skin);
        }
        gpgsLabel.setAlignment(Align.center);
        gpgsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                if (g.myActionResolver.isSignedInGPGS()) {
                    gpgsButton.getLabel().setText("Sign in");
                    gpgsLabel.setText("Google Play Game Services \n [You are signed out at this moment]");
                    g.myActionResolver.signOutGPGS();
                    Dialog(g.str.dPlayerManuallySignOffGPGS,true);
                }
                else {
                    gpgsButton.getLabel().setText("Sign out");
                    gpgsLabel.setText("Google Play Game Services \n [You are successfully sign in.]");
                    g.myActionResolver.signInGPGS();
                }
            }
        });

        //settings for sound effects and music volume
        final sLabel musicLabel = new sLabel("Music volume", g.assets.skin);
        final sLabel soundLabel = new sLabel("Sound effects volume", g.assets.skin);
        musicLabel.setScale(0.5f+g.settings.musicVolume);
        soundLabel.setScale(0.5f+g.settings.soundVolume);
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
                musicLabel.setScale(0.5f + musicSlider.getValue());
            }
        });
        musicSlider.addListener(new InputListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                event.stop();
                return false;
            }
        });
        soundSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                g.settings.soundVolume = soundSlider.getValue();
                g.settings.saveSettings();
                soundLabel.setScale(0.5f + soundSlider.getValue());
                if (g.settings.soundVolume > 0.66)
                    g.tts.say("Testing testing. Good. I like this volume.", g.settings.soundVolume);
                else if (g.settings.soundVolume > 0.33)
                    g.tts.say("I have feelings too you know. And you just hurt them.", g.settings.soundVolume);
                //else if (g.settings.soundVolume > 0.25)
                //    g.tts.say("I just hope you won't mute me completely.", g.settings.soundVolume);
                else
                    g.tts.say("You don't like me? Well let me tell you something. I don't like you either.", g.settings.soundVolume);
            }
        });
        soundSlider.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                event.stop();
                return false;
            }
        });




        Table settingTable = new Table(g.assets.skin);


        settingTable.add(musicLabel).right().fill().height(50).expandX().row();
        settingTable.add(musicSlider).width(stage.getWidth() / 2).row();
        settingTable.add(new sLabel(" ", g.assets.skin)).expandX().fill().row();


        settingTable.add(soundLabel).right().fill().height(50).expandX().row();
        settingTable.add(soundSlider).width(stage.getWidth() / 2).row();
        settingTable.add(new sLabel(" ", g.assets.skin)).expandX().fill().row();


        settingTable.add(gpgsLabel).right().fill().expandX().row();
        settingTable.add(gpgsButton).width(200).row();


        settingTable.setFillParent(true);
        ScrollPane scroll = new ScrollPane(settingTable);
        settingsWindow.add(scroll).expand().fill().center();
    }


    /**
     * prepares main menu window and adds it to stage
     */
    private void mainMenu(){
        mainWindow = makeWindow("Wormkout");
        mainWindow.setVisible(true);

        sTextButton playButton = new sTextButton("Play", g.assets.skin);
        sTextButton settingsButton = new sTextButton("Settings", g.assets.skin);
        sTextButton musicButton = new sTextButton("Music", g.assets.skin);
        sTextButton leaderboardsButton = new sTextButton("Leaderboards", g.assets.skin);
        sTextButton achievementsButton = new sTextButton("Achievements", g.assets.skin);
        sTextButton exitButton = new sTextButton("Exit", g.assets.skin);
        sTextButton creditsButton = new sTextButton("Credits", g.assets.skin);
        sTextButton helpButton = new sTextButton("Help", g.assets.skin);


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
                g.tts.say("Finally. By the way it's good time to stand up.",g.settings.soundVolume);
            }
        });

        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.setVisible(true);
                mainWindow.setVisible(false);
                g.tts.say("Settings are boring. Leave this place at once and don't touch my volume.",g.settings.soundVolume);
            }
        });

        musicButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                musicWindow.setVisible(true);
                mainWindow.setVisible(false);
                g.tts.say("I like music too. We have so much in common. My darling.",g.settings.soundVolume);
            }
        });

        leaderboardsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (g.myActionResolver.isSignedInGPGS())
                    g.myActionResolver.getLeaderboardGPGS();
                else {
                    sDialog dialog = new sDialog("", g.assets.skin);
                    dialog.text(g.str.dNoCanDoWithoutSignInGPGS);
                    sTextButton yesButton = new sTextButton("  Yes  ", g.assets.skin);
                    dialog.button(yesButton, true);
                    sTextButton noButton = new sTextButton("  No  ", g.assets.skin);
                    dialog.button(noButton, false);
                    dialog.show(stage);

                    yesButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            g.myActionResolver.signInGPGS();
                        }
                    });
                    g.tts.say(g.str.dNoCanDoWithoutSignInGPGS,g.settings.soundVolume);
                }
            }
        });

        achievementsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (g.myActionResolver.isSignedInGPGS())
                    g.myActionResolver.getAchivementsGPGS();
                else {
                    sDialog dialog = new sDialog("", g.assets.skin);
                    dialog.text(g.str.dNoCanDoWithoutSignInGPGS);
                    sTextButton yesButton = new sTextButton("  Yes  ", g.assets.skin);
                    dialog.button(yesButton, true);
                    sTextButton noButton = new sTextButton("  No  ", g.assets.skin);
                    dialog.button(noButton, false);
                    dialog.show(stage);

                    yesButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            g.myActionResolver.signInGPGS();
                        }
                    });
                    g.tts.say(g.str.dNoCanDoWithoutSignInGPGS,g.settings.soundVolume);
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
                g.tts.say("It looks like you have lost your way. I am always here for ya.",g.settings.soundVolume);
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                g.tts.say("Bye",g.settings.soundVolume);
                Gdx.app.exit();
            }
        });
    }


    /**
     * @param title test in title bar
     * @return returns new window with prepared common parameters
     */
    private sWindow makeWindow(String title){
        sWindow window = new sWindow(title, g.assets.skin, g.assets.fontShader);
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
    private sTextButton makeCloseButton(){
        sTextButton closeButton = new sTextButton(" Back ", g.assets.skin);
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

    private void Dialog(String msg, boolean speak){
        new sDialog("", g.assets.skin){}.text(msg).button("Ok").show(stage);
        if (speak)
            g.tts.say(msg, g.settings.soundVolume);
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
