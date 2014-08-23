package org.tiborsmith.wormkout.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.analysis.KissFFT;
import com.badlogic.gdx.audio.io.Mpg123Decoder;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import org.tiborsmith.wormkout.Wormkout;

/**
 * Created by tibor on 4.8.14.
 */
public class AudioTestScreen implements Screen {
    Wormkout game;
    public AudioTestScreen (Wormkout game){ this.game = game; }


    public boolean playing;
    public AudioDevice device;
    private KissFFT fft;
    private Mpg123Decoder decoder;
    private Thread playbackThread;


    public static final int WIDTH = Gdx.graphics.getWidth();
    public static final int HEIGHT = Gdx.graphics.getHeight();

    private Stage stage;


    short[] samples = new short[2048];
    float[] spectrum = new float[1025];
    float[] maxValues = new float[1024+1];
    float[] topValues = new float[512];

    Texture texture;
    private Color[] colors= new Color[32];
    OrthographicCamera camera;
    SpriteBatch batch;
    private BitmapFont font;

    int NB_BARS = 32;
    float barWidth = ((float) WIDTH / (float) NB_BARS/(float)3);
    float FALLING_SPEED = (1.0f / 3.0f);

    @Override
    public void render (float delta){
        Gdx.gl.glClearColor(0.75f, 0.75f, 0.75f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        batch.begin();

        camera.update();
        batch.setProjectionMatrix(camera.combined);


        int nb = 2;
        for (int i = 0; i < 3*NB_BARS; i += 3) {
            float sum = avg(i*nb,nb)+avg(i*nb+nb,nb)+avg(i*nb+2*nb,nb);
            if (sum>0.0f){
                for (int j = 0; j < 3; j++) {
                    topValues[i+j] -= 0.618f*delta;
                    topValues[i+j] = (avg(i*nb+j*nb,nb)/sum > topValues[i+j]) ? avg(i*nb+j*nb,nb)/sum : topValues[i+j];

                }
            }
        }

        for (int i = 0; i < NB_BARS; i++) {
            colors[i].set(topValues[3 * i], topValues[3 * i + 1], topValues[3 * i + 2], 1.0f);
            // almost black => use randomized color
            if (colors[i].r + colors[i].g + colors[i].b < 0.0000001f) {
                //colors[i].set(2*(float)Math.random(),2*(float)Math.random(),0.01f+2*(float)Math.random(),1.0f);
                colors[i].set(Color.DARK_GRAY);
            }


            if (colors[i].r > colors[i].g && colors[i].g > colors[i].b) {
                colors[i].g *= 0.5;
                colors[i].b *= 0.25;
                colors[i].mul(0.618f / colors[i].r);
            } else if (colors[i].r > colors[i].b && colors[i].b > colors[i].g) {
                colors[i].g *= 0.25;
                colors[i].b *= 0.5;
                colors[i].mul(0.618f / colors[i].r);
            } else if (colors[i].g > colors[i].r && colors[i].r > colors[i].b) {
                colors[i].r *= 0.5;
                colors[i].b *= 0.25;
                colors[i].mul(0.618f / colors[i].g);
            } else if (colors[i].g > colors[i].b && colors[i].b > colors[i].r) {
                colors[i].r *= 0.25;
                colors[i].b *= 0.5;
                colors[i].mul(0.618f / colors[i].g);
            } else if (colors[i].b > colors[i].r && colors[i].r > colors[i].g) {
                colors[i].r *= 0.5;
                colors[i].g *= 0.25;
                colors[i].mul(0.618f / colors[i].b);
            } else if (colors[i].b > colors[i].g && colors[i].g > colors[i].r) {
                colors[i].r *= 0.25;
                colors[i].g *= 0.5;
                colors[i].mul(0.618f / colors[i].b);
            } else {
                float max = colors[i].r;
                if (colors[i].g > max) {
                    max = colors[i].g;
                }
                if (colors[i].b > max)
                    max = colors[i].b;
                colors[i].mul(0.618f / max);
            }

            batch.draw(texture, i*3 * barWidth, 0, barWidth,
                    HEIGHT*colors[i].r, 0, 0, 16, 5, false, false);

            batch.draw(texture, i*3 * barWidth+barWidth, 0, barWidth,
                    HEIGHT*colors[i].g, 0, 5, 16, 5, false, false);
            batch.draw(texture, i*3 * barWidth+2*barWidth, 0, barWidth,
                    HEIGHT*colors[i].b, 0, 10, 16, 5, false, false);
        }

      /*  int nb = (samples.length / NB_BARS) / 2;
        float sum=0.01f;
        for (int i = 0; i < NB_BARS; i++)
            sum+=avg(i*nb, nb);
        for (int i = 0; i < NB_BARS; i++) {


            if (avg(i*nb, nb) > maxValues[i]) {
                maxValues[i] = avg(i*nb, nb);
            }

            if (avg(i*nb, nb) > topValues[i]) {
                topValues[i] = avg(i*nb, nb);
            }

            // drawing spectrum (in blue)
            batch.draw(texture, i * barWidth, 0, barWidth,
                    scale(avg(i*nb, nb)), 0, 0, 16, 5, false, false);
            // drawing max values (in yellow)
            batch.draw(texture, i * barWidth, scale(topValues[i]),
                    barWidth, 4, 0, 5, 16, 5, false, false);
            // drawing top values (in red)
            batch.draw(texture, i * barWidth, scale(maxValues[i]),
                    barWidth, 2, 0, 10, 16, 5, false, false);

            font.draw(batch, "Spectrum sample value: " + topValues[0], 20, 200);
            topValues[i] -= FALLING_SPEED*delta;
        }*/


        batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize (int width, int height){
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void show (){
        stage = new Stage();
        TextButton menuButton = new TextButton("Go back to menu",game.assets.skin);

        Table table = new Table();
        table.setFillParent(true);
        table.add(menuButton);
        table.row();
        //table.add(audioTestButton);

        stage.addActor(table);
        menuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(game.mainScreen);
            }
        });

        Gdx.input.setInputProcessor(stage);


        font = new BitmapFont();
        font.setColor(Color.BLUE);
        font.setScale(2.0f);

        // create the camera
        camera = new OrthographicCamera();

        camera.setToOrtho(false, WIDTH, HEIGHT);
        // load colors
        texture = new Texture(Gdx.files.internal("tmp/colors-borders.png"));
        // create the spritebatch
        batch = new SpriteBatch();

        // fast fourier transform
        fft = new KissFFT(2048);

        for (int i = 0; i < topValues.length; i++) {
            topValues[i] = 0;
        }
        for (int i=0; i<32; i++){
            colors[i] = new Color(Color.WHITE);
        }

        if (game.currentSong >= game.playList.songPaths.size) {
            game.currentSong = 0;
        }
        if ((!game.playList.playDefault) && game.currentSong < game.playList.numOfDefaultSong)
            game.currentSong = game.playList.numOfDefaultSong;

        decoder = new Mpg123Decoder(Gdx.files.external(game.playList.songPaths.get(game.currentSong)));


        // Create an audio device for playback
        device = Gdx.audio.newAudioDevice(decoder.getRate(), decoder.getChannels() == 1 );
        device.setVolume(game.settings.musicVolume);

        // start a thread for playback
        playbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int readSamples = 0;

                // read until we reach the end of the file or playing is stopped
                while (!Thread.currentThread().isInterrupted()
                        && (readSamples = decoder.readSamples(samples, 0, samples.length)) > 0) {
                    // get audio spectrum
                    fft.spectrum(samples, spectrum);
                    // write the samples to the AudioDevice
                    device.writeSamples(samples, 0, readSamples);
                }
                // if while cycle ended by finishing song, set different one
                //if (!Thread.currentThread().isInterrupted()) {
                game.currentSong++;
                //}
                playing = false;
            }
        });
        playing = true;
        playbackThread.setDaemon(true);
        playbackThread.start();
    }

    @Override
    public void hide (){
        stopMusic();

        stage.dispose();
    }

    @Override
    public void pause (){

    }

    @Override
    public void resume (){

    }

    @Override
    public void dispose (){

    }

    private float scale(float x) {
        return x / 256*HEIGHT/2;
    }

    private float avg(int pos, int nb) {
        int sum = 0;
        for (int i = 0; i < nb; i++) {
            sum += spectrum[pos + i];
        }

        return (float) (sum / nb);
    }

    public void stopMusic (){
        // synchronize with the thread
        if (playbackThread != null)
            try {
                playbackThread.interrupt();
                playbackThread.join();
            } catch (InterruptedException e) {
                Gdx.app.error("AudioThread", "Thread had problem with ending",e);
            }

        //dispose of stuff if its not null
        if (device != null)
            device.dispose();
        if (decoder != null)
            decoder.dispose();
        if(fft != null)
            fft.dispose();
    }
}
