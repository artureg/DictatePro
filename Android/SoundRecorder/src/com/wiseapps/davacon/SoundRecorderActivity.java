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
import android.widget.TextView;
import android.widget.Toast;
import com.wiseapps.davacon.core.se.*;

import static com.wiseapps.davacon.core.se.SEAudioStreamEngine.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/13/14
 *         Time: 9:50 AM
 *
 *         TODO 1) record to the end + playback of the whole track - done
 *         TODO 2) record with a split
 *         TODO 3) duration in seconds
 *         TODO 4) project save and save async
 *         TODO 5) project decode
 */
public class SoundRecorderActivity extends Activity {
    private static final String TAG = SoundRecorderActivity.class.getSimpleName();

    private SEProject project;
    private SEAudioStreamEngine engine;

    private ImageButton
            buttonRewind, buttonRecord, buttonForward,
            buttonStart, buttonPlay, buttonEnd,
            buttonExport, buttonSave;

    private TextView textDuration;

    private SeekBar seekVolume;
    private SeekBar seekPosition;

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
        project = new SEProject(getContext());

        SDCardUtils.readProject(project);

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

        buttonExport = (ImageButton) findViewById(R.id.export);
        buttonSave = (ImageButton) findViewById(R.id.save);

        textDuration = (TextView) findViewById(R.id.duration);

        seekVolume = (SeekBar) findViewById(R.id.volume);
        seekVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM));
        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        seekVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));

        seekPosition = (SeekBar) findViewById(R.id.position);
        seekPosition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        updateProgress(0);
    }

    private void updateProgress(int progress) {
        int position = (int) engine.getCurrentTime() + progress;
        int duration = (int) engine.getDuration();

        seekPosition.setProgress(position);
        seekPosition.setMax(duration);

        textDuration.setText(
                String.format(getResources().getString(R.string.process_track_duration),
                        position, duration));
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

        engine.setCurrentTime(Long.MIN_VALUE);
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

        engine.setCurrentTime(Long.MAX_VALUE);
        updateProgress();
    }

    public void export(View view) {
        if (engine.getState() != State.READY) {
            return;
        }

        // TODO implement
    }

    public void save(View view) {
        if (engine.getState() != State.READY) {
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
        }

        @Override
        public void playingPaused() {
            buttonRewind.setImageDrawable(
                    getResources().getDrawable(R.drawable.rewind_selector));
            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_1_record_disabled));
            buttonForward.setImageDrawable(
                    getResources().getDrawable(R.drawable.forward_selector));

            buttonStart.setImageDrawable(
                    getResources().getDrawable(R.drawable.start_selector));
            buttonPlay.setImageDrawable(
                    getResources().getDrawable(R.drawable.button01_1_play_enabled));
            buttonEnd.setImageDrawable(
                    getResources().getDrawable(R.drawable.end_selector));

            buttonExport.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_1));
            buttonSave.setImageDrawable(
                    getResources().getDrawable(R.drawable.save_1));
        }

        @Override
        public void playingInProgress(int progress) {
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

            updateProgress(progress);
        }

        @Override
        public void playingStopped() {
            buttonRewind.setImageDrawable(
                    getResources().getDrawable(R.drawable.rewind_selector));
            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_1_record_disabled));
            buttonForward.setImageDrawable(
                    getResources().getDrawable(R.drawable.forward_selector));

            buttonStart.setImageDrawable(
                    getResources().getDrawable(R.drawable.start_selector));
            buttonPlay.setImageDrawable(
                    getResources().getDrawable(R.drawable.button01_1_play_enabled));
            buttonEnd.setImageDrawable(
                    getResources().getDrawable(R.drawable.end_selector));

            buttonExport.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_1));
            buttonSave.setImageDrawable(
                    getResources().getDrawable(R.drawable.save_1));
        }

        @Override
        public void onError(String errorMessage) {
            buttonRewind.setImageDrawable(
                    getResources().getDrawable(R.drawable.rewind_selector));
            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_1_record_disabled));
            buttonForward.setImageDrawable(
                    getResources().getDrawable(R.drawable.forward_selector));

            buttonStart.setImageDrawable(
                    getResources().getDrawable(R.drawable.start_selector));
            buttonPlay.setImageDrawable(
                    getResources().getDrawable(R.drawable.button01_1_play_enabled));
            buttonEnd.setImageDrawable(
                    getResources().getDrawable(R.drawable.end_selector));

            buttonExport.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_1));
            buttonSave.setImageDrawable(
                    getResources().getDrawable(R.drawable.save_1));

            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
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
        }

        @Override
        public void recordingInProgress(int progress) {
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

            updateProgress(progress);
        }

        @Override
        public void recordingStopped() {
            buttonRewind.setImageDrawable(
                    getResources().getDrawable(R.drawable.rewind_selector));
            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_1_record_disabled));
            buttonForward.setImageDrawable(
                    getResources().getDrawable(R.drawable.forward_selector));

            buttonStart.setImageDrawable(
                    getResources().getDrawable(R.drawable.start_selector));
            buttonPlay.setImageDrawable(
                    getResources().getDrawable(R.drawable.button01_1_play_enabled));
            buttonEnd.setImageDrawable(
                    getResources().getDrawable(R.drawable.end_selector));

            buttonExport.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_1));
            buttonSave.setImageDrawable(
                    getResources().getDrawable(R.drawable.save_1));
        }

        @Override
        public void onError(String errorMessage) {
            buttonRewind.setImageDrawable(
                    getResources().getDrawable(R.drawable.rewind_selector));
            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_1_record_disabled));
            buttonForward.setImageDrawable(
                    getResources().getDrawable(R.drawable.forward_selector));

            buttonStart.setImageDrawable(
                    getResources().getDrawable(R.drawable.start_selector));
            buttonPlay.setImageDrawable(
                    getResources().getDrawable(R.drawable.button01_1_play_enabled));
            buttonEnd.setImageDrawable(
                    getResources().getDrawable(R.drawable.end_selector));

            buttonExport.setImageDrawable(
                    getResources().getDrawable(R.drawable.send_1));
            buttonSave.setImageDrawable(
                    getResources().getDrawable(R.drawable.save_1));

            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    };

    private class MediaButtonReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            seekVolume.setProgress(
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
