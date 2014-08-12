package org.tiborsmith.wormkout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.analysis.KissFFT;
import com.badlogic.gdx.audio.io.Mpg123Decoder;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;


/**
 * Created by tibor on 3.8.14.
 */
public class MyAudio {
    private Wormkout game;
    public MyAudio (Wormkout game){ this.game = game; }

    public AudioDevice device;
    private Mpg123Decoder decoder;
    private KissFFT fft;
    private Thread playbackThread;

    private short[] samples = new short[2048];
    private float[] spectrum = new float[1025];
    private float[] topValues = new float[512];



    private int NB_BARS = 32;
    private float FALLING_SPEED = (1.0f / 10.0f);

    /**
     * Need to get better color generation
     * @param delta real time connection
     * @param colors colors for gates
     */
    public void generateColors(float delta,Color[] colors){

        int nb = 2;
        for (int i = 0; i < NB_BARS*4; i++) {

            /*if (avg(i*nb, nb) > topValues[i]) {
                topValues[i] = avg(i*nb, nb);
            }*/

            topValues[i] = avg(i*nb, nb);

           // topValues[i] -= 256*FALLING_SPEED*delta;

        }

        for (int i = 0; i < NB_BARS; i++) {
            float sum = topValues[4*i]+topValues[4*i+1]+topValues[4*i+2]+topValues[4*i+3];
            if (sum>0.0f)
                colors[i].set(topValues[4*i]/sum,topValues[4*i+1]/sum,topValues[4*i+2]/sum,topValues[4*i+3]/sum);
            // almost black => use randomized color
            if(colors[i].r+colors[i].g+colors[i].b<0.0000001f){
                colors[i].set(2*(float)Math.random(),2*(float)Math.random(),0.01f+2*(float)Math.random(),1.0f);
                //colors[i].set(Color.DARK_GRAY);
            }

            float max = colors[i].r;
            if (colors[i].g>max) max = colors[i].g;
            if (colors[i].b>max) max = colors[i].b;

            colors[i].mul(0.75f/max);
            colors[i].clamp();


        }

    }

    public void start(FileHandle file, boolean external){

        // fast fourier transform
        fft = new KissFFT(2048);

        for (int i = 0; i < topValues.length; i++) {
            topValues[i] = 0;
        }

        // if I am going to play external file, then use it directly, otherwise ...
        ///if(external) {
            decoder = new Mpg123Decoder(file);
        /*}
        else {
            // the audio file has to be on the external storage (not in the assets)
            FileHandle externalFile = Gdx.files.external("tmp/audio-spectrum.mp3");
            Gdx.files.internal(file.path()).copyTo(externalFile);

            // create the decoder (you can use a VorbisDecoder if you want to read
            // ogg files)
            decoder = new Mpg123Decoder(externalFile);
        }*/


        // Create an audio device for playback
        device = Gdx.audio.newAudioDevice(decoder.getRate(), decoder.getChannels() == 1 );
        device.setVolume(game.myState.mySettings.musicVolume);

        // start a thread for playback
        playbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int readSamples = 0;

                // read until we reach the end of the file or playing is stopped
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


    public void stop (){
        // synchronize with the thread
        if (playbackThread != null)
        try {
            playbackThread.interrupt();
            playbackThread.join();
        } catch (InterruptedException e) {
            Gdx.app.error("AudioThread", "Thread had problem with ending",e);
        }

        if (device != null)
            device.dispose();
        if (decoder != null)
            decoder.dispose();
        if(fft != null)
            fft.dispose();

        // delete the temp file
        if(Gdx.files.external("tmp/audio-spectrum.mp3").exists())
            Gdx.files.external("tmp/audio-spectrum.mp3").delete();
    }


    private float avg(int pos, int nb) {
        int sum = 0;
        for (int i = 0; i < nb; i++) {
            sum += spectrum[pos + i];
        }

        return (float) (sum / nb);
    }
}

