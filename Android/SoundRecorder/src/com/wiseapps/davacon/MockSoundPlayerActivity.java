package com.wiseapps.davacon;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;
import com.wiseapps.davacon.core.mock.MockProject;
import com.wiseapps.davacon.core.mock.MockProjectEngine;
import com.wiseapps.davacon.core.mock.MockSDCardUtils;
import com.wiseapps.davacon.core.se.SEAudioStreamEngine;
import com.wiseapps.davacon.core.se.SEPlayerStateListener;
import com.wiseapps.davacon.core.se.SERecorderStateListener;
import com.wiseapps.davacon.logging.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/18/14
 *         Time: 5:31 PM
 */
public class MockSoundPlayerActivity extends Activity {
    private static final String TAG = SoundRecorderActivity.class.getSimpleName();

    private MockProject project;
    private SEAudioStreamEngine engine;

    private ImageButton
            buttonRewind, buttonRecord, buttonForward,
            buttonStart, buttonPlay, buttonEnd,
            buttonExport, buttonSave;

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
        ((MockProjectEngine) engine).release();

        if (project != null) {
            MockSDCardUtils.writeProject(project);
        }

        super.onDestroy();
    }

    private void initData() {
        project = new MockProject(getContext());

        MockSDCardUtils.readProject(project);

        if (project != null) {
            engine = new MockProjectEngine(getContext(), project);
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

        buttonExport = (ImageButton) findViewById(R.id.export);
        buttonSave = (ImageButton) findViewById(R.id.save);

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
                if (engine.getState() != SEAudioStreamEngine.State.READY) {
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
        if (engine.getState() != SEAudioStreamEngine.State.READY) {
            return;
        }

        engine.setCurrentTime(engine.getCurrentTime() - 1);
        updateProgress();
    }

    public void record(View view) {
        if (engine.getState() == SEAudioStreamEngine.State.READY) {
            engine.startRecording();
            return;
        }

        if (engine.getState() == SEAudioStreamEngine.State.RECORDING_IN_PROGRESS) {
            engine.stopRecording();
        }
    }

    public void forward(View view) {
        if (engine.getState() != SEAudioStreamEngine.State.READY) {
            return;
        }

        engine.setCurrentTime(engine.getCurrentTime() + 1);
        updateProgress();
    }

    public void start(View view) {
        if (engine.getState() != SEAudioStreamEngine.State.READY) {
            return;
        }

        engine.setCurrentTime(0);
        updateProgress();
    }

    public void play(View view) {
        if (engine.getState() == SEAudioStreamEngine.State.READY) {
            engine.startPlaying();
            return;
        }

        if (engine.getState() == SEAudioStreamEngine.State.PLAYING_IN_PROGRESS) {
            engine.pausePlaying();
        }
    }

    public void end(View view) {
        if (engine.getState() != SEAudioStreamEngine.State.READY) {
            return;
        }

        engine.setCurrentTime(-1);
        updateProgress();
    }

    public void export(View view) {
        if (engine.getState() != SEAudioStreamEngine.State.READY) {
            return;
        }

        // TODO implement
    }

    public void save(View view) {
        if (engine.getState() != SEAudioStreamEngine.State.READY) {
            return;
        }

        new SaveProjectTask().execute();
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

            buttonExport.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_0));
            buttonSave.setImageDrawable(
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

            buttonExport.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_1));
            buttonSave.setImageDrawable(
                    getResources().getDrawable(R.drawable.save_1));

            updateProgress();
        }

        @Override
        public void playingInProgress(int position) {
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

            buttonExport.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_0));
            buttonSave.setImageDrawable(
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

            buttonExport.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_1));
            buttonSave.setImageDrawable(
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

            buttonExport.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_1));
            buttonSave.setImageDrawable(
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

            buttonExport.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_0));
            buttonSave.setImageDrawable(
                    getResources().getDrawable(R.drawable.save_0));

            updateProgress();
        }

        @Override
        public void recordingInProgress(int position) {
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

            buttonExport.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_0));
            buttonSave.setImageDrawable(
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

            buttonExport.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_1));
            buttonSave.setImageDrawable(
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

            buttonExport.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_1));
            buttonSave.setImageDrawable(
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

    private class SaveProjectTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return project.save();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            Toast.makeText(getContext(),
                    aBoolean ? "Project saved successfully!" : "Saving project failed!",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
