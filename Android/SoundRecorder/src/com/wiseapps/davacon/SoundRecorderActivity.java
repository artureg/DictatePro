package com.wiseapps.davacon;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.wiseapps.davacon.core.se.*;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.utils.DurationUtils;

import java.text.DecimalFormat;

import static com.wiseapps.davacon.core.se.SEAudioStreamEngine.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/13/14
 *         Time: 9:50 AM
 */
public class SoundRecorderActivity extends Activity {
    private static final String TAG = SoundRecorderActivity.class.getSimpleName();

    private static final int MSG_PROGRESS_UPDATE = 0;

    private SEProject project;
    private SEAudioStreamEngine engine;

    private ImageButton
            buttonRewind, buttonRecord, buttonForward,
            buttonStart, buttonPlay, buttonEnd,
            buttonExport, buttonSave;

    private TextView textDuration;

    private SeekBar seekVolume;
    private SeekBar seekPosition;

    private ProgressDialog dialog;

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
    protected void onResume() {
        super.onResume();

        if (dialog != null) {
            dialog.show();
        }
    }

    @Override
    protected void onPause() {
        if (dialog != null) {
            dialog.dismiss();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        destroyEngine();

        if (project != null) {
            SDCardUtils.writeProject(project);
        }

        super.onDestroy();
    }

    private void initData() {
        project = new SEProject(getContext());

        SDCardUtils.readProject(project);

        if (project != null) {
            initEngine();
        }
    }

    private void initEngine() {
        engine = new SEProjectEngine(getContext(), project);
        engine.addPlayerStateListener(playerStateListener);
        engine.addRecorderStateListener(recorderStateListener);
    }

    private void destroyEngine() {
        ((SEProjectEngine) engine).release();
        engine = null;
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
                if (!fromUser) {
                    return;
                }

                if (engine.getState() != State.READY) {
                    return;
                }

                engine.setCurrentTime(DurationUtils.secondsToBytes((double) progress / 10));

                textDuration.setText(
                        String.format(getResources().getString(R.string.process_track_duration),
                                DurationUtils.secondsFromBytes(engine.getCurrentTime()),
                                DurationUtils.secondsFromBytes(engine.getDuration())));
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
        updateProgress(DurationUtils.secondsFromBytes(engine.getCurrentTime()),
                DurationUtils.secondsFromBytes(engine.getDuration()));
    }

    private void updateProgress(double position, double duration) {
        LoggerFactory.obtainLogger(TAG).d("updateProgress# position = " + position +
                ", duration = " + duration);

        seekPosition.setProgress((int) position * 10);
        seekPosition.setMax((int) duration * 10);

        textDuration.setText(
                String.format(getResources().getString(R.string.process_track_duration),
                        new DecimalFormat("#.#").format(position), new DecimalFormat("#.#").format(duration)));
    }

    public void rewind(View view) {
        if (engine.getState() != State.READY) {
            return;
        }

        engine.setCurrentTime(engine.getCurrentTime() - DurationUtils.secondsToBytes(1));
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

        engine.setCurrentTime(engine.getCurrentTime() + DurationUtils.secondsToBytes(1));
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

    public void encode(View view) {
        if (engine.getState() != State.READY) {
            return;
        }

        new EncodeProjectTask().execute();
    }

    public void delete(View view) {
        if (engine.getState() != State.READY) {
            return;
        }

        new DeleteProjectTask().execute();
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

    private SEPlayerStateListener playerStateListener = new SEPlayerStateAdapter() {

        @Override
        public void playingStarted() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    buttonRewind.setImageDrawable(
                            getResources().getDrawable(R.drawable.button06_0_rewind_disabled));
                    buttonRecord.setImageDrawable(
                            getResources().getDrawable(R.drawable.button02_2_record_enabled));
                    buttonForward.setImageDrawable(
                            getResources().getDrawable(R.drawable.button07_0_forward_disabled));

                    buttonStart.setImageDrawable(
                            getResources().getDrawable(R.drawable.button05_0_start_disabled));
                    buttonPlay.setImageDrawable(
                            getResources().getDrawable(R.drawable.button03_1));
                    buttonEnd.setImageDrawable(
                            getResources().getDrawable(R.drawable.button08_0_end_disabled));

                    buttonExport.setImageDrawable(
                            getResources().getDrawable(R.drawable.send_0));
                    buttonSave.setImageDrawable(
                            getResources().getDrawable(R.drawable.save_0));

                    progressHandler = new ProgressHandler(false, DurationUtils.secondsFromBytes(engine.getCurrentTime()),
                            DurationUtils.secondsFromBytes(engine.getDuration()));
                    progressHandler.sendMessage(progressHandler.obtainMessage(MSG_PROGRESS_UPDATE));
                }
            });
        }

        @Override
        public void playingPaused() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    buttonRewind.setImageDrawable(
                            getResources().getDrawable(R.drawable.rewind_selector));
                    buttonRecord.setImageDrawable(
                            getResources().getDrawable(R.drawable.button02_2_record_enabled));
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

                    progressHandler.removeMessages(MSG_PROGRESS_UPDATE);
                    progressHandler = null;
                }
            });
        }

        @Override
        public void onError(final String errorMessage) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    buttonRewind.setImageDrawable(
                            getResources().getDrawable(R.drawable.rewind_selector));
                    buttonRecord.setImageDrawable(
                            getResources().getDrawable(R.drawable.button02_2_record_enabled));
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

                    progressHandler.removeMessages(MSG_PROGRESS_UPDATE);
                    progressHandler = null;
                }
            });
        }
    };

    private SERecorderStateListener recorderStateListener = new SERecorderStateAdapter() {
        @Override
        public void recordingStarted() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    buttonRewind.setImageDrawable(
                            getResources().getDrawable(R.drawable.button06_0_rewind_disabled));
                    buttonRecord.setImageDrawable(
                            getResources().getDrawable(R.drawable.button04_2));
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

                    progressHandler = new ProgressHandler(true, DurationUtils.secondsFromBytes(engine.getCurrentTime()),
                            DurationUtils.secondsFromBytes(engine.getDuration()));
                    progressHandler.sendMessage(progressHandler.obtainMessage(MSG_PROGRESS_UPDATE));
                }
            });
        }

        @Override
        public void recordingStopped() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    buttonRewind.setImageDrawable(
                            getResources().getDrawable(R.drawable.rewind_selector));
                    buttonRecord.setImageDrawable(
                            getResources().getDrawable(R.drawable.button02_2_record_enabled));
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

                    progressHandler.removeMessages(MSG_PROGRESS_UPDATE);
                    progressHandler = null;
                }
            });
        }

        @Override
        public void onError(final String errorMessage) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    buttonRewind.setImageDrawable(
                            getResources().getDrawable(R.drawable.rewind_selector));
                    buttonRecord.setImageDrawable(
                            getResources().getDrawable(R.drawable.button02_2_record_enabled));
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

                    progressHandler.removeMessages(MSG_PROGRESS_UPDATE);
                    progressHandler = null;
                }
            });
        }
    };

    private class SaveProjectTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = new ProgressDialog(getContext());

            dialog.show();

            dialog.setMessage(getResources().getString(R.string.project_save_in_progress));
            dialog.setCancelable(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return project.save();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }

            updateProgress();

            Toast.makeText(getContext(),
                    aBoolean ? "Project saved successfully!" : "Saving project failed...",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class DeleteProjectTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = new ProgressDialog(getContext());

            dialog.show();

            dialog.setMessage(getResources().getString(R.string.project_deletion_in_progress));
            dialog.setCancelable(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean result = SDCardUtils.deleteProject(project);

            project = new SEProject(getContext());
            SDCardUtils.readProject(project);

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }

            destroyEngine();
            initEngine();

            updateProgress();

            Toast.makeText(getContext(),
                    aBoolean ? "Project deleted successfully!" : "Deletion of project failed...",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class EncodeProjectTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return null;
        }
    }

    private class MediaButtonReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            seekVolume.setProgress(
                    audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM));
        }
    }

    private ProgressHandler progressHandler;
    private class ProgressHandler extends Handler {
        private final boolean recording;

        private double position;
        private double duration;

        private ProgressHandler(boolean recording, double position, double duration) {
            this.recording = recording;

            this.position = position;
            this.duration = duration;
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_PROGRESS_UPDATE: {
                    updateProgress(position, recording ? Math.max(duration, 30) : duration);

                    position += 0.1;
                    duration += recording ? 0.1 : 0;
                    sendMessageDelayed(obtainMessage(MSG_PROGRESS_UPDATE), 100);

                    break;
                }
            }
        }
    }

    public SEAudioStreamEngine getEngine() {
        return engine;
    }

    public SEProject getProject() {
        return project;
    }
}
