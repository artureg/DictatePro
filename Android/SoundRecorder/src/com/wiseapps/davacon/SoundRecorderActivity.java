package com.wiseapps.davacon;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import com.wiseapps.davacon.core.se.*;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.utils.FileUtils;

import java.io.File;
import java.io.FilenameFilter;

import static com.wiseapps.davacon.core.se.SEAudioStreamEngine.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/13/14
 *         Time: 9:50 AM
 */
public class SoundRecorderActivity extends Activity {
    private static final String TAG = SoundRecorderActivity.class.getSimpleName();

    private SEProject project;
    private SEAudioStreamEngine engine;

    private ImageButton buttonRecord;

    private SeekBar volumeBar;

    private AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sound_recorder);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        registerReceiver(new MediaButtonReceiver(),
                new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));

        initData();
        initWidgets();
    }

    @Override
    protected void onDestroy() {
        // TODO release engine correctly!

        if (project != null) {
            SDCardUtils.writeProject(project);
        }

        super.onDestroy();
    }

    private void initData() {
        try {
            File root = FileUtils.getRoot(this);

            String[] filenames = root.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".plist");
                }
            });

            if (filenames != null && filenames.length != 0) {
                project = SDCardUtils.readProject(getContext(), filenames[0]);
                return;
            }

            project = SDCardUtils.readProject(getContext());
        } catch (Exception e) {
            LoggerFactory.obtainLogger(TAG).
                    e("initData# ", e);
        }

        if (project != null) {
            engine = new SEProjectEngine(getContext(), project);
            engine.addPlayerStateListener(playerStateListener);
            engine.addRecorderStateListener(recorderStateListener);
        }
    }

    private void initWidgets() {
        buttonRecord = (ImageButton) findViewById(R.id.record);

        volumeBar = (SeekBar) findViewById(R.id.volume);
        volumeBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, progress, AudioManager.FLAG_PLAY_SOUND);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void rewind(View view) {
        // TODO implements
    }

    public void record(View view) {
        if (engine.getState() == State.READY) {
            engine.startRecording();
            return;
        }

        if (engine.getState() == State.RECORDING_IN_PROGRESS) {
            engine.stopRecording();
        }
    }

    public void forward(View view) {
        // TODO implement
    }

    public void start(View view) {
        // TODO implement
    }

    public void play(View view) {
        if (engine.getState() == State.READY) {
            engine.startPlaying();
            return;
        }

        if (engine.getState() == State.PLAYING_IN_PROGRESS) {
            engine.pausePlaying();
        }
    }

    public void end(View view) {
        // TODO implement
    }

    private Context getContext() {
        return this;
    }

    private SEPlayerStateListener playerStateListener = new SEPlayerStateListener() {
        @Override
        public void playingStarted() {
            // TODO implement
        }

        @Override
        public void playingPaused() {
            // TODO implement
        }

        @Override
        public void playingInProgress(double position) {
            // TODO implement
        }

        @Override
        public void playingStopped() {
            // TODO implement
        }

        @Override
        public void onError(String errorMessage) {
            // TODO implement
        }
    };

    private SERecorderStateListener recorderStateListener = new SERecorderStateListener() {
        @Override
        public void recordingStarted() {
            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_2_record_enabled));
        }

        @Override
        public void recordingInProgress(double position) {
            // TODO update progress
        }

        @Override
        public void recordingStopped() {
            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_1_record_disabled));
        }

        @Override
        public void onError(String errorMessage) {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    };

    private class MediaButtonReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            volumeBar.setProgress(
                    audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
        }
    }
}
