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

    private ImageButton
            buttonRewind, buttonRecord, buttonForward,
            buttonStart, buttonPlay, buttonEnd,
            buttonEncode, buttonDecode;

    private SeekBar volumeBar;
    private SeekBar positionBar;

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
        ((SEProjectEngine) engine).release();

        if (project != null) {
            SDCardUtils.writeProject(project);
        }

        super.onDestroy();
    }

    private void initData() {
        try {
            project = new SEProject(getContext());

            File root = FileUtils.getRoot(this);

            String[] filenames = root.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".plist");
                }
            });

            if (filenames != null && filenames.length != 0) {
                SDCardUtils.readProject(project, filenames[0]);
                return;
            }

            SDCardUtils.readProject(project);
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
        buttonRewind = (ImageButton) findViewById(R.id.rewind);
        buttonRecord = (ImageButton) findViewById(R.id.record);
        buttonForward = (ImageButton) findViewById(R.id.forward);

        buttonStart = (ImageButton) findViewById(R.id.start);
        buttonPlay = (ImageButton) findViewById(R.id.play);
        buttonEnd = (ImageButton) findViewById(R.id.end);

        buttonEncode = (ImageButton) findViewById(R.id.encode);
        buttonDecode = (ImageButton) findViewById(R.id.decode);

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

        positionBar = (SeekBar) findViewById(R.id.position);
        positionBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (engine.getState() != State.READY) {
                    return;
                }

                engine.setCurrentTime(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        updateProgress();
    }

    private void updateProgress() {
        positionBar.setMax(((int) engine.getDuration()) + 1);
        positionBar.setProgress((int) engine.getCurrentTime());
    }

    public void rewind(View view) {
        if (engine.getState() != State.READY) {
            return;
        }

        engine.setCurrentTime(engine.getCurrentTime() - 1);
        updateProgress();
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
        if (engine.getState() != State.READY) {
            return;
        }

        engine.setCurrentTime(engine.getCurrentTime() + 1);
        updateProgress();
    }

    public void start(View view) {
        if (engine.getState() != State.READY) {
            return;
        }

        engine.setCurrentTime(0);
        updateProgress();
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
        if (engine.getState() != State.READY) {
            return;
        }

        engine.setCurrentTime(-1);
        updateProgress();
    }

    public void encode(View view) {
        // TODO async {1) build project file 2) encode to SPEEX}
    }

    public void decode(View view) {
        // TODO async {decode from SPEEX if file exists, play}
    }

    private Context getContext() {
        return this;
    }

    private SEPlayerStateListener playerStateListener = new SEPlayerStateListener() {
        @Override
        public void playingStarted() {
            buttonRewind.setImageDrawable(
                    getResources().getDrawable(R.drawable.button06_0_rewind_disabled));
            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_0));
            buttonForward.setImageDrawable(
                    getResources().getDrawable(R.drawable.button07_0_forward_disabled));

            buttonStart.setImageDrawable(
                    getResources().getDrawable(R.drawable.button05_0_start_disabled));
            buttonPlay.setImageDrawable(
                    getResources().getDrawable(R.drawable.button01_2));
            buttonEnd.setImageDrawable(
                    getResources().getDrawable(R.drawable.button08_0_end_disabled));

            buttonEncode.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_0));
            buttonDecode.setImageDrawable(
                    getResources().getDrawable(R.drawable.save_0));

            updateProgress();
        }

        @Override
        public void playingPaused() {
            buttonRewind.setImageDrawable(
                    getResources().getDrawable(R.drawable.button06_1_rewind_enabled));
            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_1_record_disabled));
            buttonForward.setImageDrawable(
                    getResources().getDrawable(R.drawable.button07_1_forward_enabled));

            buttonStart.setImageDrawable(
                    getResources().getDrawable(R.drawable.button05_1_start_enabled));
            buttonPlay.setImageDrawable(
                    getResources().getDrawable(R.drawable.button01_1_play_enabled));
            buttonEnd.setImageDrawable(
                    getResources().getDrawable(R.drawable.button08_1_end_enabled));

            buttonEncode.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_1));
            buttonDecode.setImageDrawable(
                    getResources().getDrawable(R.drawable.save_1));

            updateProgress();
        }

        @Override
        public void playingInProgress(double position) {
            buttonRewind.setImageDrawable(
                    getResources().getDrawable(R.drawable.button06_0_rewind_disabled));
            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_0));
            buttonForward.setImageDrawable(
                    getResources().getDrawable(R.drawable.button07_0_forward_disabled));

            buttonStart.setImageDrawable(
                    getResources().getDrawable(R.drawable.button05_0_start_disabled));
            buttonPlay.setImageDrawable(
                    getResources().getDrawable(R.drawable.button01_2));
            buttonEnd.setImageDrawable(
                    getResources().getDrawable(R.drawable.button08_0_end_disabled));

            buttonEncode.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_0));
            buttonDecode.setImageDrawable(
                    getResources().getDrawable(R.drawable.save_0));

            updateProgress();
        }

        @Override
        public void playingStopped() {
            buttonRewind.setImageDrawable(
                    getResources().getDrawable(R.drawable.button06_1_rewind_enabled));
            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_1_record_disabled));
            buttonForward.setImageDrawable(
                    getResources().getDrawable(R.drawable.button07_1_forward_enabled));

            buttonStart.setImageDrawable(
                    getResources().getDrawable(R.drawable.button05_1_start_enabled));
            buttonPlay.setImageDrawable(
                    getResources().getDrawable(R.drawable.button01_1_play_enabled));
            buttonEnd.setImageDrawable(
                    getResources().getDrawable(R.drawable.button08_1_end_enabled));

            buttonEncode.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_1));
            buttonDecode.setImageDrawable(
                    getResources().getDrawable(R.drawable.save_1));

            updateProgress();
        }

        @Override
        public void onError(String errorMessage) {
            buttonRewind.setImageDrawable(
                    getResources().getDrawable(R.drawable.button06_1_rewind_enabled));
            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_1_record_disabled));
            buttonForward.setImageDrawable(
                    getResources().getDrawable(R.drawable.button07_1_forward_enabled));

            buttonStart.setImageDrawable(
                    getResources().getDrawable(R.drawable.button05_1_start_enabled));
            buttonPlay.setImageDrawable(
                    getResources().getDrawable(R.drawable.button01_1_play_enabled));
            buttonEnd.setImageDrawable(
                    getResources().getDrawable(R.drawable.button08_1_end_enabled));

            buttonEncode.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_1));
            buttonDecode.setImageDrawable(
                    getResources().getDrawable(R.drawable.save_1));

            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();

            updateProgress();
        }
    };

    private SERecorderStateListener recorderStateListener = new SERecorderStateListener() {
        @Override
        public void recordingStarted() {
            buttonRewind.setImageDrawable(
                    getResources().getDrawable(R.drawable.button06_0_rewind_disabled));
            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_2_record_enabled));
            buttonForward.setImageDrawable(
                    getResources().getDrawable(R.drawable.button07_0_forward_disabled));

            buttonStart.setImageDrawable(
                    getResources().getDrawable(R.drawable.button05_0_start_disabled));
            buttonPlay.setImageDrawable(
                    getResources().getDrawable(R.drawable.button01_0_play_disabled));
            buttonEnd.setImageDrawable(
                    getResources().getDrawable(R.drawable.button08_0_end_disabled));

            buttonEncode.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_0));
            buttonDecode.setImageDrawable(
                    getResources().getDrawable(R.drawable.save_0));

            updateProgress();
        }

        @Override
        public void recordingInProgress(double position) {
            buttonRewind.setImageDrawable(
                    getResources().getDrawable(R.drawable.button06_0_rewind_disabled));
            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_2_record_enabled));
            buttonForward.setImageDrawable(
                    getResources().getDrawable(R.drawable.button07_0_forward_disabled));

            buttonStart.setImageDrawable(
                    getResources().getDrawable(R.drawable.button05_0_start_disabled));
            buttonPlay.setImageDrawable(
                    getResources().getDrawable(R.drawable.button01_0_play_disabled));
            buttonEnd.setImageDrawable(
                    getResources().getDrawable(R.drawable.button08_0_end_disabled));

            buttonEncode.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_0));
            buttonDecode.setImageDrawable(
                    getResources().getDrawable(R.drawable.save_0));

            updateProgress();
        }

        @Override
        public void recordingStopped() {
            buttonRewind.setImageDrawable(
                    getResources().getDrawable(R.drawable.button06_1_rewind_enabled));
            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_1_record_disabled));
            buttonForward.setImageDrawable(
                    getResources().getDrawable(R.drawable.button07_1_forward_enabled));

            buttonStart.setImageDrawable(
                    getResources().getDrawable(R.drawable.button05_1_start_enabled));
            buttonPlay.setImageDrawable(
                    getResources().getDrawable(R.drawable.button01_1_play_enabled));
            buttonEnd.setImageDrawable(
                    getResources().getDrawable(R.drawable.button08_1_end_enabled));

            buttonEncode.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_1));
            buttonDecode.setImageDrawable(
                    getResources().getDrawable(R.drawable.save_1));

            updateProgress();
        }

        @Override
        public void onError(String errorMessage) {
            buttonRewind.setImageDrawable(
                    getResources().getDrawable(R.drawable.button06_1_rewind_enabled));
            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_1_record_disabled));
            buttonForward.setImageDrawable(
                    getResources().getDrawable(R.drawable.button07_1_forward_enabled));

            buttonStart.setImageDrawable(
                    getResources().getDrawable(R.drawable.button05_1_start_enabled));
            buttonPlay.setImageDrawable(
                    getResources().getDrawable(R.drawable.button01_1_play_enabled));
            buttonEnd.setImageDrawable(
                    getResources().getDrawable(R.drawable.button08_1_end_enabled));

            buttonEncode.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_1));
            buttonDecode.setImageDrawable(
                    getResources().getDrawable(R.drawable.save_1));

            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();

            updateProgress();
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
