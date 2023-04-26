package com.annandroidapps.medium.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    //Variable to play with number of instances of MediaPlayer being created
    //static int countPlayers = 0;

    /** Handles playback of all the sound files */
    private MediaPlayer mMediaPlayer;

    /** Handles audio focus when playing a sound file */
    private AudioManager mAudioManager;
    int result;
    int length;

    private MediaPlayer.OnCompletionListener mCompletitionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            releaseMediaPlayer();
        }
    };

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                mMediaPlayer.pause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                mMediaPlayer.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                length = mMediaPlayer.getCurrentPosition();
                Log.d("loss focus", "loss of focus");
                releaseMediaPlayer();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        /* Request audio focus so in order to play the audio file. The app needs to play a
            audio file, so we will request audio focus for unknown duration
           with AUDIOFOCUS_GAIN*/
        int result = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //for API >= 26
            result = mAudioManager.requestAudioFocus((new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)).build());
        } else {
            result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }


        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //create player
            mMediaPlayer = MediaPlayer.create(this, R.raw.play_file);
            //start playing
            Log.d("OnCreate method", "OnCreate player created");
            mMediaPlayer.start();
            //listen for completition of playing
            mMediaPlayer.setOnCompletionListener(mCompletitionListener);
        }

    }

    /**
     * Clean up the media player by releasing its resources.
     */
    private void releaseMediaPlayer() {

        // If the media player is not null, then it may be currently playing a sound.
        if (mMediaPlayer != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            mMediaPlayer.release();
            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            mMediaPlayer = null;

            // Regardless of whether or not we were granted audio focus, abandon it. This also
            // unregisters the AudioFocusChangeListener so we don't get anymore callbacks.

            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.play_file);
            Log.d("OnRestart", "OnReStart player created");
            Log.d("OnRestart", "length=" + length);
            mMediaPlayer.seekTo(length);

            mMediaPlayer.start();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        length =mMediaPlayer.getCurrentPosition();
        releaseMediaPlayer();
        Log.d("onStop", "OnStop player destroyed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy", "Ondestroy called");
    }
}