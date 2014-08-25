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
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.I18NBundle;

import org.tiborsmith.wormkout.MyAssets;
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
    MyAssets assets;
    public MainScreen(Wormkout g){
        this.g = g;
        assets = g.myAssets;
    }


    //bundle with strings
    I18NBundle str;


    private Stage stage;
    private ModelBatch modelBatch;
    private Environment environment;

    private sWindow settingsWindow;
    private sWindow mainWindow;
    private sWindow musicWindow;
    private sWindow levelWindow;
    private sWindow creditsWindow;
    private sWindow helpWindow;

    private sLabel gpgsLabel;
    private sTextButton gpgsButton;


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
        sLabel.setShader(assets.fontShader);
        str = g.myAssets.str;

        g.splashScreen.dispose();
        stage = new Stage();
        stage.setViewport(assets.viewport);
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
                        g.tts.say(str.get("Bye"),g.settings.soundVolume);
                        Gdx.app.exit();
                    }
                    return true;
                }
                else
                    return false;
            }
        });


        if (g.welcomeBack){
            if (g.settings.automaticSignInGPGS) {
                g.myActionResolver.signInGPGS();
                g.levelStates.saveLevelProgress();
            }
            g.tts.say(str.get("sNormalWelcome1"), g.settings.soundVolume);
            g.welcomeBack = false;
        }
        if (g.firstLaunch) {
            sDialog dialog = new sDialog("", assets.skin);
            dialog.text(str.get("sdFirstWelcome"));
            sTextButton yesButton = new sTextButton(str.get("Visit"), assets.skin);
            dialog.button(yesButton, true);
            sTextButton noButton = new sTextButton(str.get("Ignore"), assets.skin);
            dialog.button(noButton, false);
            dialog.show(stage);
            yesButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    mainWindow.setVisible(false);
                    helpWindow.setVisible(true);
                    g.myActionResolver.unlockAchievementGPGS("achievement_visited_help");
                }
            });
            g.tts.say(str.get("sdFirstWelcome"),g.settings.soundVolume);
            g.firstLaunch = false;
        }

    }

    private sLabel emptyLine(){
        return new sLabel(" ", assets.skin);
    }

    private sLabel titleLabel(String title ,float scale){
        sLabel label = new sLabel(title, assets.skin);
        label.setScale(scale);
        label.setAlignment(Align.center);
        return label;
    }



    private void creditsMenu(){
        creditsWindow = makeWindow(str.get("cWTitle"));
        creditsWindow.getButtonTable().add(makeCloseButton()).height(levelWindow.getPadTop());

        Table creditsTable = new Table();
        creditsTable.add(titleLabel(str.get("cWAboutTheAuthorTitle"),2)).colspan(2).expandX().fill().row();

        sLabel aboutMeLabel = new sLabel(str.get("cwAboutMeText"),assets.skin);
        aboutMeLabel.setWrap(true);
        creditsTable.add(emptyLine());
        creditsTable.add(aboutMeLabel).expandX().fill().row();
        creditsTable.add(titleLabel(str.get("cWManifestoTitle"),2)).colspan(2).expandX().fill().row();
        sLabel aboutWormkoutLabel1 = new sLabel(str.get("cwAboutWormkoutText"),assets.skin);
        aboutWormkoutLabel1.setWrap(true);
        creditsTable.add(emptyLine());
        creditsTable.add(aboutWormkoutLabel1).expandX().fill().row();



        creditsTable.add(ImageButtonWithDescription("playstore",str.get("cWplaystoreLink"),
                str.get("cWPlaystoreText"),true)).colspan(2).expandX().fill().row();
        creditsTable.add(emptyLine()).colspan(2).row();
        creditsTable.add(ImageButtonWithDescription("gplus",str.get("cWgplusLink"),
                str.get("cWGooglePlusText"),true)).colspan(2).expandX().fill().row();
        creditsTable.add(emptyLine()).colspan(2).row();
        creditsTable.add(ImageButtonWithDescription("facebook",str.get("cWfacebookLink"),
                str.get("cWFacebookText"),true)).colspan(2).expandX().fill().row();
        creditsTable.add(emptyLine()).colspan(2).row();
        creditsTable.add(ImageButtonWithDescription("twitter",str.get("cWtwitterLink"),
                str.get("cWTwitterText"),true)).colspan(2).expandX().fill().row();
        creditsTable.add(emptyLine()).colspan(2).row();
        creditsTable.add(ImageButtonWithDescription("youtube",str.get("cWyoutubeLink"),
                str.get("cWYoutubeText"),true)).colspan(2).expandX().fill().row();
        creditsTable.add(emptyLine()).colspan(2).row();
        creditsTable.add(ImageButtonWithDescription("donate",str.get("cWpaypalLink"),
                str.get("cWDonatePaypalText"),true)).colspan(2).expandX().fill().row();


        creditsTable.add(emptyLine()).row();
        creditsTable.add(titleLabel(str.get("cWCreditsTitle"),2)).colspan(2).expandX().fill().row();


        creditsTable.add(ImageButtonWithDescription("gdx",str.get("cWlibgdxLink"),
                str.get("cWlibGDXCreditsText"),true)).colspan(2).expandX().fill().row();
        creditsTable.add(emptyLine()).row();
        creditsTable.add(ImageButtonWithDescription("incompetech",str.get("cWincompetechLink"),
                str.get("cWIncompetechCreditsText"),true)).colspan(2).expandX().fill().row();


        creditsTable.add(emptyLine()).row();
        ScrollPane scroll = new ScrollPane(creditsTable);
        creditsWindow.add(scroll).expand().fill().center();
    }

    public Table ImageButtonWithDescription(String imgname, final String link, String description, final boolean button){
        Table table = new Table();
        ImageButton logo = new ImageButton(new Image(assets.images.findRegion(imgname)).getDrawable());
        logo.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (button) {
                    Gdx.net.openURI(link);
                    g.myActionResolver.unlockAchievementGPGS("achievement_link_exploration");
                }
            }
        });
        sLabel label = new sLabel(description ,assets.skin);
        label.setWrap(true);
        table.add(emptyLine());
        table.add(logo).fill();
        table.add(emptyLine());
        table.add(label).expandX().fill().row();
        return table;
    }


    private void helpMenu(){
        helpWindow = makeWindow(str.get("hWTitle"));
        helpWindow.getButtonTable().add(makeCloseButton()).height(levelWindow.getPadTop());


        Table helpTable = new Table();
        helpTable.add(titleLabel(str.get("hWHTPTitle"),2)).colspan(2).expandX().fill().row();

        sLabel helpIntro = new sLabel(str.get("hWHowPlayIntroText"),assets.skin);
        helpIntro.setWrap(true);
        helpTable.add(emptyLine());
        helpTable.add(helpIntro).expandX().fill().row();
        helpTable.add(ImageButtonWithDescription("howtocontrol","",
                str.get("hWHowPlayText"),false)).colspan(2).expandX().fill().row();


        helpTable.add(titleLabel(str.get("hWMASETitle"),2)).colspan(2).expandX().fill().row();
        sLabel musicHelpIntro = new sLabel(str.get("hWHowMusicText"),assets.skin);
        musicHelpIntro.setWrap(true);
        helpTable.add(emptyLine());
        helpTable.add(musicHelpIntro).expandX().fill().row();

        helpTable.add(emptyLine()).row();
        ScrollPane scroll = new ScrollPane(helpTable);
        helpWindow.add(scroll).expand().fill().center();
    }



    private Table generateLevelTable(final int level){
        Table table = new Table();

        //play button or lock label
        if (!g.levelStates.lvls.get(level).locked){
            sTextButton lvlButton = new sTextButton(g.levelStates.lvls.get(level).name, assets.skin);
            lvlButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    g.currentLevel = level;
                    g.setScreen(g.gameScreen);
                }
            });
            table.add(lvlButton).width(200).padLeft(10).align(Align.left);
        }
        else {
            sLabel label = new sLabel(g.levelStates.lvls.get(level).name+" " + str.get("locked"), assets.skin);
            label.setAlignment(Align.center);
            table.add(label).width(200).padLeft(10).align(Align.left);
        }

        //level description
        sLabel lvlLabel = new sLabel(g.levelStates.lvls.get(level).description, assets.skin);
        lvlLabel.setAlignment(Align.left);
        table.add(lvlLabel).expandX().align(Align.left).padLeft(10).fill();

        //leaderboard button with best time
        String LBString;
        if (g.levelStates.lvls.get(level).finished){
            float time = g.levelStates.lvls.get(level).bestTime;
            LBString = str.format("TimeScore",  (int)time/60, (int)time%60, (int)(time*1000)%1000);
        }
        else {
            LBString = str.get("emptyTimeScore");
        }
        sTextButton LBButton = new sTextButton( LBString, assets.skin);
        LBButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (g.myActionResolver.isSignedInGPGS()) {
                    g.levelStates.saveLevelProgress();
                    g.tts.say(str.get("sayLeaderBoardWindowOpen"),g.settings.soundVolume);
                    g.myActionResolver.getLeaderboardGPGS(g.levelStates.lvls.get(level).name);
                }
                else {
                    sDialog dialog = new sDialog("", assets.skin);
                    dialog.text(str.get("sdNoCanDoWithoutSignInGPGS"));
                    sTextButton yesButton = new sTextButton(str.get("Yes"), assets.skin);
                    dialog.button(yesButton, true);
                    dialog.button(new sTextButton(str.get("No"), assets.skin), false);
                    dialog.show(stage);

                    yesButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            GPGSSignIn();
                        }
                    });
                    g.tts.say(str.get("sdNoCanDoWithoutSignInGPGS"),g.settings.soundVolume);
                }
            }
        });
        table.add(LBButton).width(175).padLeft(10).padRight(10).align(Align.left).row();


        table.add(emptyLine()).expandX().colspan(3).fill().row();
        return table;
    }

    private void levelMenu(){
        levelWindow = makeWindow(str.get("lWTitle"));
        levelWindow.getButtonTable().add(makeCloseButton()).height(levelWindow.getPadTop());


        Table levelTable = new Table();
        ScrollPane scroll = new ScrollPane(levelTable.top());
        //levelTable.add(new sLabel(" ", g.as.skin)).expandX().fill().row();

        Table table = new Table();
        table.add(titleLabel(str.get("Level"),1.25f)).width(200).align(Align.left).padLeft(10).fill();
        table.add(titleLabel(str.get("Description"),1.25f)).expandX().align(Align.left).padLeft(10).fill();
        table.add(titleLabel(str.get("Time"),1.25f)).width(175).align(Align.left).padLeft(10).padRight(10).fill().row();

        levelTable.add(table).expand().fill().row();
        for (int i=0 ; i < g.levelStates.lvls.size; i++){
            levelTable.add(generateLevelTable(i)).expandX().fill().row();
        }
        levelTable.add(emptyLine()).expand().fill().row();

        levelWindow.add(scroll).expand().fill();
    }


    private void musicMenu(){
        musicWindow = makeWindow(str.get("mWTitle"));
        musicWindow.getButtonTable().add(makeCloseButton()).height(musicWindow.getPadTop());

        final sCheckBox pDCB = new sCheckBox(str.get("mWPDMCheckbox"), assets.skin);
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
                    okDialog(str.get("sdDisableDefaultMusic"), true);
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

        final sList playlist = new sList(assets.skin,assets.fontShader);
        playlist.setItems(g.playList.songNames);
        ScrollPane scroll = new ScrollPane(playlist);
        sTextButton removeButton = new sTextButton(str.get("mWRSButton"), assets.skin);
        removeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                int i = playlist.getSelectedIndex();
                if (i < g.playList.numOfDefaultSong){
                    okDialog(str.get("sdAttemptToRemoveDefaultMusic"), true);
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


        sFileChooser fileChooser = new sFileChooser(assets.skin, new FileChooser.Listener() {
            @Override
            public void choose(FileHandle file) {
                g.playList.songPaths.add(file.path());
                g.playList.songNames.add(file.name());
                playlist.setItems(g.playList.songNames);
                g.playList.savePlayList();
                g.myActionResolver.unlockAchievementGPGS("achievement_customized_music");
            }

            @Override
            public void choose(Array<FileHandle> files) {}

            @Override
            public void cancel() {}
        },str.get("mWASButton"));
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
        fileChooser.add(Gdx.files.external(""),assets.skin); // root for tree
        fileChooser.getTree().getSelection().setMultiple(false);  //disables multiple selection


        SplitPane dividedMusicWindow = new SplitPane(playlistTable,fileChooser,false, assets.skin);
        musicWindow.add(dividedMusicWindow).expand().fill().row();
    }

    /**
     * prepares main menu window and adds it to stage
     */
    private void settingsMenu(){
        settingsWindow = makeWindow(str.get("sWTitle"));
        settingsWindow.getButtonTable().add(makeCloseButton()).height(settingsWindow.getPadTop()).row();

        //setup for GPGS login logout button
        if (g.myActionResolver.isSignedInGPGS()) {
            gpgsButton = new sTextButton(str.get("Disable"), assets.skin);
            gpgsLabel = new sLabel(str.get("sWGPGSInLabel"), assets.skin);
        }
        else{
            gpgsButton = new sTextButton(str.get("Enable"), assets.skin);
            gpgsLabel = new sLabel(str.get("sWGPGSOutLabel"), assets.skin);
        }
        gpgsLabel.setAlignment(Align.center);
        gpgsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                if (g.myActionResolver.isSignedInGPGS()) {
                    gpgsButton.getLabel().setText(str.get("Enable"));
                    gpgsLabel.setText(str.get("sWGPGSOutLabel"));
                    g.myActionResolver.signOutGPGS();
                    g.settings.automaticSignInGPGS = false;
                    g.settings.saveSettings();
                    okDialog(str.get("sdPlayerManuallySignOffGPGS"), true);
                }
                else {
                    GPGSSignIn();
                }
            }
        });

        //settings for sound effects and music volume
        final sLabel musicLabel = new sLabel(str.get("sWMVLabel"), assets.skin);
        final sLabel soundLabel = new sLabel(str.get("sWSEVLabel"), assets.skin);
        musicLabel.setScale(0.5f+g.settings.musicVolume);
        soundLabel.setScale(0.5f+g.settings.soundVolume);
        musicLabel.setAlignment(Align.center);
        soundLabel.setAlignment(Align.center);
        final Slider musicSlider = new Slider(0.0f,1.0f,0.02f,false, assets.skin);
        final Slider soundSlider = new Slider(0.0f,1.0f,0.02f,false, assets.skin);
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
                    g.tts.say(str.get("saySoundSliderJoke1"), g.settings.soundVolume);
                else if (g.settings.soundVolume > 0.33)
                    g.tts.say(str.get("saySoundSliderJoke2"), g.settings.soundVolume);
                else
                    g.tts.say(str.get("saySoundSliderJoke3"), g.settings.soundVolume);
            }
        });
        soundSlider.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                event.stop();
                return false;
            }
        });




        Table settingTable = new Table(assets.skin);


        settingTable.add(musicLabel).right().fill().height(50).expandX().row();
        settingTable.add(musicSlider).width(stage.getWidth() / 2).row();
        settingTable.add(emptyLine()).expandX().fill().row();


        settingTable.add(soundLabel).right().fill().height(50).expandX().row();
        settingTable.add(soundSlider).width(stage.getWidth() / 2).row();
        settingTable.add(emptyLine()).expandX().fill().row();


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
        mainWindow = makeWindow(assets.str.get(str.get("Wormkout")));
        mainWindow.setVisible(true);
        sTextButton exitButton = new sTextButton(str.get("Exit"), assets.skin);
        exitButton.getLabel().setAlignment(Align.center);
        mainWindow.getButtonTable().add(exitButton).height(mainWindow.getPadTop()).row();

        sTextButton playButton = new sTextButton(str.get("Play"), assets.skin);
        sTextButton settingsButton = new sTextButton(str.get("Settings"), assets.skin);
        sTextButton musicButton = new sTextButton(str.get("Music"), assets.skin);
        sTextButton achievementsButton = new sTextButton(str.get("Achievements"), assets.skin);
        sTextButton creditsButton = new sTextButton(str.get("Credits"), assets.skin);
        sTextButton helpButton = new sTextButton(str.get("Help"), assets.skin);


        float wFB = 200;
        mainWindow.add(playButton).width(wFB).expand().colspan(2).row();
        mainWindow.add(settingsButton).width(wFB).expand();
        mainWindow.add(musicButton).width(wFB).expand().row();
        mainWindow.add(helpButton).width(wFB).expand();
        mainWindow.add(achievementsButton).width(wFB).expand().row();
        mainWindow.add(creditsButton).width(wFB).expand().colspan(2);


        //listeners for buttons
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                levelWindow.setVisible(true);
                mainWindow.setVisible(false);
                g.tts.say(str.get("sayLevelWindow"),g.settings.soundVolume);
            }
        });

        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.setVisible(true);
                mainWindow.setVisible(false);
                g.tts.say(str.get("saySettingsWindow"),g.settings.soundVolume);
            }
        });

        musicButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                musicWindow.setVisible(true);
                mainWindow.setVisible(false);
                g.tts.say(str.get("sayMusicWindow"),g.settings.soundVolume);
            }
        });

        achievementsButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (g.myActionResolver.isSignedInGPGS())
                    g.myActionResolver.getAchievementsGPGS();
                else {
                    sDialog dialog = new sDialog("", assets.skin);
                    dialog.text(str.get("sdNoCanDoWithoutSignInGPGS"));
                    sTextButton yesButton = new sTextButton(str.get("Yes"), assets.skin);
                    dialog.button(yesButton, true);
                    dialog.button(new sTextButton(str.get("No"), assets.skin), false);
                    dialog.show(stage);

                    yesButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            GPGSSignIn();
                        }
                    });
                    g.tts.say(str.get("sdNoCanDoWithoutSignInGPGS"),g.settings.soundVolume);
                }
            }
        });

        creditsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                creditsWindow.setVisible(true);
                mainWindow.setVisible(false);
                g.myActionResolver.unlockAchievementGPGS("achievement_visited_credits");
                g.tts.say(str.get("sayCreditsWindow"),g.settings.soundVolume);
            }
        });

        helpButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                helpWindow.setVisible(true);
                mainWindow.setVisible(false);
                g.tts.say(str.get("sayHelpWindow"),g.settings.soundVolume);
                g.myActionResolver.unlockAchievementGPGS("achievement_visited_help");
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                g.tts.say(str.get("Bye"),g.settings.soundVolume);
                Gdx.app.exit();
            }
        });
    }


    /**
     * @param title test in title bar
     * @return returns new window with prepared common parameters
     */
    private sWindow makeWindow(String title){
        sWindow window = new sWindow(title, assets.skin, assets.fontShader);
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
        sTextButton closeButton = new sTextButton(str.get("Back"), assets.skin);
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


    private void okDialog(String msg, boolean speak){
        new sDialog("", assets.skin){}.text(msg).button(str.get("Ok")).show(stage);
        if (speak)
            g.tts.say(msg, g.settings.soundVolume);
    }


    private void GPGSSignIn(){
        gpgsButton.getLabel().setText(str.get("Disable"));
        gpgsLabel.setText(str.get("sWGPGSInLabel"));
        g.settings.automaticSignInGPGS = true;
        g.settings.saveSettings();
        g.myActionResolver.signInGPGS();
        g.levelStates.saveLevelProgress();
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
