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
    private MediaButtonReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sound_recorder);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        receiver = new MediaButtonReceiver();
        registerReceiver(receiver,
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

        unregisterReceiver(receiver);

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
        seekVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_PLAY_SOUND);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        seekPosition = (SeekBar) findViewById(R.id.position);
        seekPosition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                LoggerFactory.obtainLogger(TAG).
                        d("onProgressChanged# progress = " + progress +
                                ", currentTime = " + engine.getCurrentTime());

                if (!fromUser) {
                    return;
                }

                if (engine.getState() != State.READY) {
                    return;
                }

//                engine.setCurrentTime(DurationUtils.secondsToBytes((double) progress / 10));
                engine.setCurrentTime(progress);

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

        updatePositionSeekBar(engine.getCurrentTime(), engine.getDuration());
        updatePositionText(engine.getCurrentTime(), engine.getDuration());
    }

    private void updatePositionSeekBar(long position, long duration) {
        seekPosition.setProgress((int) position);
                seekPosition.setMax((int) duration);
    }

    private void updatePositionText(long position, long duration) {
        double dP = DurationUtils.secondsFromBytes(position);
        double dD = DurationUtils.secondsFromBytes(duration);

        textDuration.setText(
                String.format(getResources().getString(R.string.process_track_duration),
                        new DecimalFormat("0.0").format(dP), new DecimalFormat("0.0").format(dD)));
    }

    public void rewind(View view) {
        if (engine.getState() != State.READY) {
            return;
        }

        engine.setCurrentTime(engine.getCurrentTime() - DurationUtils.secondsToBytes(1));

        updatePositionSeekBar(engine.getCurrentTime(), engine.getDuration());
        updatePositionText(engine.getCurrentTime(), engine.getDuration());
    }

    public void record(View view) {
        if (engine.getState() == State.READY) {
            engine.startRecording();
            updateStateRecordingInProgress();
            return;
        }

        if (engine.getState() == State.RECORDING_IN_PROGRESS) {
            engine.stopRecording();
            updateStateReady();
        }
    }

    public void forward(View view) {
        if (engine.getState() != State.READY) {
            return;
        }

        engine.setCurrentTime(engine.getCurrentTime() + DurationUtils.secondsToBytes(1));

        updatePositionSeekBar(engine.getCurrentTime(), engine.getDuration());
        updatePositionText(engine.getCurrentTime(), engine.getDuration());
    }

    public void start(View view) {
        if (engine.getState() != State.READY) {
            return;
        }

        engine.setCurrentTime(Long.MIN_VALUE);

        updatePositionSeekBar(engine.getCurrentTime(), engine.getDuration());
        updatePositionText(engine.getCurrentTime(), engine.getDuration());
    }

    public void play(View view) {
        if (engine.getState() == State.READY) {
            if (engine.getCurrentTime() >= engine.getDuration()) {
                return;
            }

            engine.startPlaying();
            updateStatePlayingInProgress();
            return;
        }

        if (engine.getState() == State.PLAYING_IN_PROGRESS) {
            engine.pausePlaying();
            updateStateReady();
        }
    }

    public void end(View view) {
        if (engine.getState() != State.READY) {
            return;
        }

        engine.setCurrentTime(Long.MAX_VALUE);

        updatePositionSeekBar(engine.getCurrentTime(), engine.getDuration());
        updatePositionText(engine.getCurrentTime(), engine.getDuration());
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

    private void updateStateReady() {
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
    }

    private void updateStateRecordingInProgress() {
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
        }

    private void updateStatePlayingInProgress() {
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

//        LoggerFactory.obtainLogger(TAG).d("playingStarted# engine.getCurrentTime = " +
//                engine.getCurrentTime());
//        LoggerFactory.obtainLogger(TAG).d("playingStarted# engine.getDuration = " +
//                engine.getDuration());
                    }

    private Context getContext() {
        return this;
                    }

    private SEPlayerStateListener playerStateListener = new SEPlayerStateAdapter() {
        @Override
        public void playingStarted(final int position, final int duration) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updatePositionSeekBar(position, duration);
                    updatePositionText(position, duration);
                }
            });
        }

        @Override
        public void playingInProgress(final int position, final int duration) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updatePositionSeekBar(position, duration);
                    updatePositionText(position, duration);
                }
            });
        }

        @Override
        public void playingPaused(final int position, final int duration) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updatePositionSeekBar(position, duration);
                    updatePositionText(position, duration);

                    updateStateReady();
                    }
            });
                    }

        @Override
        public void onError(final int position, final int duration, final String errorMessage) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updatePositionSeekBar(position, duration);
                    updatePositionText(position, duration);

                    Toast.makeText(getContext(),
                            errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private SERecorderStateListener recorderStateListener = new SERecorderStateAdapter() {
        @Override
        public void recordingStarted(final int position, final int duration) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updatePositionSeekBar(position, duration);
                    updatePositionText(position, duration);
                }
            });
        }

        @Override
        public void recordingInProgress(final int position, final int duration) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updatePositionSeekBar(position, duration);
                    updatePositionText(position, duration);
                    }
            });
                    }

        @Override
        public void recordingStopped(final int position, final int duration) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updatePositionSeekBar(position, duration);
                    updatePositionText(position, duration);
                }
            });
        }

        @Override
        public void onError(final int position, final int duration, final String errorMessage) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updatePositionSeekBar(position, duration);
                    updatePositionText(position, duration);

                    Toast.makeText(getContext(),
                            errorMessage, Toast.LENGTH_SHORT).show();
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

            updatePositionSeekBar(engine.getCurrentTime(), engine.getDuration());
            updatePositionText(engine.getCurrentTime(), engine.getDuration());

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

            updatePositionSeekBar(engine.getCurrentTime(), engine.getDuration());
            updatePositionText(engine.getCurrentTime(), engine.getDuration());

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
                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
    }

    public SEAudioStreamEngine getEngine() {
        return engine;
    }

    public SEProject getProject() {
        return project;
    }
}
