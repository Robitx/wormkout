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


    String FILE = "tmp/frequencytest.mp3";
    AudioDevice device;
    Mpg123Decoder decoder;
    KissFFT fft;
    Thread playbackThread;


    public static final int WIDTH = Gdx.graphics.getWidth();
    public static final int HEIGHT = Gdx.graphics.getHeight();

    private Stage stage;


    short[] samples = new short[2048];
    float[] spectrum = new float[1024+1];
    float[] maxValues = new float[1024+1];
    float[] topValues = new float[1024+1];

    Texture colors;
    OrthographicCamera camera;
    SpriteBatch batch;
    private BitmapFont font;

    int NB_BARS = 32*4;
    float barWidth = ((float) WIDTH / (float) NB_BARS);
    float FALLING_SPEED = (1.0f / 3.0f);

    @Override
    public void render (float delta){
        Gdx.gl.glClearColor(0, 1, 0, 0.1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        batch.begin();

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        int nb = (samples.length / NB_BARS) / 2;
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
            batch.draw(colors, i * barWidth, 0, barWidth,
                    scale(avg(i*nb, nb)), 0, 0, 16, 5, false, false);
            // drawing max values (in yellow)
            batch.draw(colors, i * barWidth, scale(topValues[i]),
                    barWidth, 4, 0, 5, 16, 5, false, false);
            // drawing top values (in red)
            batch.draw(colors, i * barWidth, scale(maxValues[i]),
                    barWidth, 2, 0, 10, 16, 5, false, false);

            font.draw(batch, "Spectrum sample value: " + topValues[0], 20, 200);
            topValues[i] -= FALLING_SPEED*delta;
        }


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
        TextButton menuButton = new TextButton("Go back to menu",game.myAssets.skin);

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
        // load texture
        colors = new Texture(Gdx.files.internal("tmp/colors-borders.png"));
        // create the spritebatch
        batch = new SpriteBatch();

        // fast fourier transform
         fft = new KissFFT(2048);

        for (int i = 0; i < maxValues.length; i++) {
            maxValues[i] = 0;
            topValues[i] = 0;
        }

        // the audio file has to be on the external storage (not in the assets)
        FileHandle externalFile = Gdx.files.external("tmp/audio-spectrum.mp3");
        Gdx.files.internal(FILE).copyTo(externalFile);

        // create the decoder (you can use a VorbisDecoder if you want to read
        // ogg files)
        decoder = new Mpg123Decoder(externalFile);

        // Create an audio device for playback
        device = Gdx.audio.newAudioDevice(decoder.getRate(),
                decoder.getChannels() == 1 ? true : false);

        // start a thread for playback
        playbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int readSamples = 0;

                // read until we reach the end of the file
                while (!Thread.currentThread().isInterrupted()
                        && (readSamples = decoder.readSamples(samples, 0,
                        samples.length)) > 0) {
                    // get audio spectrum
                    fft.spectrum(samples, spectrum);
                    // write the samples to the AudioDevice
                    device.writeSamples(samples, 0, readSamples);
                }
            }
        });
        playbackThread.setDaemon(true);
        playbackThread.start();

    }

    @Override
    public void hide (){
        // synchronize with the thread
        try {
            playbackThread.interrupt();
            playbackThread.join();
        } catch (InterruptedException e) {

        }
        device.dispose();
        decoder.dispose();
        fft.dispose();
        // delete the temp file
        Gdx.files.external("tmp/audio-spectrum.mp3").delete();

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
}
