package org.tiborsmith.wormkout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.AudioDevice;
import com.badlogic.gdx.audio.analysis.KissFFT;
import com.badlogic.gdx.audio.io.Mpg123Decoder;
import com.badlogic.gdx.graphics.Color;


/**
 * Created by tibor on 3.8.14.
 */
public class MyAudio {
    private Wormkout game;
    public MyAudio (Wormkout game){ this.game = game; }

    public boolean playing;
    public AudioDevice device;
    private KissFFT fft;
    private Mpg123Decoder decoder;
    private Thread playbackThread;


    private short[] samples = new short[2048];
    private float[] spectrum = new float[1025];
    private float[] topValues = new float[512];



    private int NB_BARS = 32;

    /**
     * Need to get better color generation
     * @param delta real time connection
     * @param colors colors for gates
     */
    public void generateColors(float delta,Color[] colors){

        int nb = 2;
        for (int i = 0; i < 3*NB_BARS; i += 3) {
            float sum = avg(i*nb,nb)+avg(i*nb+nb,nb)+avg(i*nb+2*nb,nb);
            if (sum>0.0f){
                for (int j = 0; j < 3; j++) {
                    topValues[i+j] -= 1.618f*delta;
                    topValues[i+j] = (avg(i*nb+j*nb,nb)/sum > topValues[i+j]) ? avg(i*nb+j*nb,nb)/sum : topValues[i+j];

                }
            }
        }

        for (int i = 0; i < NB_BARS; i++) {
            colors[i].set(topValues[3*i],topValues[3*i+1],topValues[3*i+2],1.0f);
            // almost black => use randomized color
            if(colors[i].r+colors[i].g+colors[i].b<0.0000001f){
                //colors[i].set(2*(float)Math.random(),2*(float)Math.random(),0.01f+2*(float)Math.random(),1.0f);
                colors[i].set(Color.DARK_GRAY);
            }


            if (colors[i].r > colors[i].g && colors[i].g > colors[i].b){
                if (colors[i].r - colors[i].g <0.15)
                    colors[i].g = colors[i].r;
                else
                    colors[i].g *=0.5;
                colors[i].b *=0.25;
                colors[i].mul(0.618f/colors[i].r);
            }
            else if (colors[i].r > colors[i].b && colors[i].g < colors[i].b){
                colors[i].g *=0.25;
                if (colors[i].r - colors[i].b <0.15)
                    colors[i].b = colors[i].r;
                else
                    colors[i].b *=0.5;
                colors[i].mul(0.618f/colors[i].r);
            }
            else if (colors[i].g > colors[i].r && colors[i].r > colors[i].b){
                if (colors[i].g - colors[i].r <0.15)
                    colors[i].r = colors[i].g;
                else
                    colors[i].r *=0.5;
                colors[i].b *=0.25;
                colors[i].mul(0.618f/colors[i].g);
            }
            else if (colors[i].g > colors[i].b && colors[i].r < colors[i].b){
                colors[i].r *=0.25;
                if (colors[i].g - colors[i].b <0.15)
                    colors[i].b = colors[i].g;
                else
                    colors[i].b *=0.5;
                colors[i].mul(0.618f/colors[i].g);
            }
            else if (colors[i].b > colors[i].r && colors[i].r > colors[i].g){
                if (colors[i].b - colors[i].r <0.15)
                    colors[i].r = colors[i].b;
                else
                    colors[i].r *=0.5;
                colors[i].g *=0.25;
                colors[i].mul(0.618f/colors[i].b);
            }
            else if (colors[i].b > colors[i].g && colors[i].r < colors[i].g){
                colors[i].r *=0.25;
                if (colors[i].b - colors[i].g <0.15)
                    colors[i].g = colors[i].b;
                else
                    colors[i].g *=0.5;
                colors[i].mul(0.618f/colors[i].b);
            }
            else {
                float max = colors[i].r;
                if (colors[i].g>max) {
                    max = colors[i].g;
                }
                if (colors[i].b>max)
                    max = colors[i].b;
                colors[i].mul(0.618f/max);
            }

            colors[i].clamp();


        }

    }


    private float avg(int pos, int nb) {
        int sum = 0;
        for (int i = 0; i < nb; i++) {
            sum += spectrum[pos + i];
        }

        return (float) (sum / nb);
    }



    public void startMusic(){
        // fast fourier transform
        fft = new KissFFT(2048);

        for (int i = 0; i < topValues.length; i++) {
            topValues[i] = 0;
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

