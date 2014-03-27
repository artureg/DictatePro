package com.wiseapps.davacon;

import android.app.Activity;
import android.media.AudioRecord;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.wiseapps.davacon.core.CheapWAV;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.utils.DurationUtils;
import com.wiseapps.davacon.utils.FileUtils;
import com.wiseapps.davacon.utils.FontUtils;

import java.io.File;
import java.io.IOException;

import static android.media.AudioRecord.*;
import static com.wiseapps.davacon.ActivityNavigator.*;
import static com.wiseapps.davacon.core.CheapWAV.*;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/20/14
 *         Time: 9:23 AM
 *
 *         TODO what's going on when leaving a screen during recording (see iPhone)
 *
 *         TODO handle onDestroy with recording (consult iPhone)
 *         TODO handle recorder release once the recording is finished
 */
public class ProcessTrackActivity extends PlayingCapableActivity {
    private static final String TAG = ProcessTrackActivity.class.getSimpleName();

    private TextView textDuration;
    private ImageButton buttonRecord;
    private ImageButton buttonPlay;
    private ProgressBar progressBar;

    private MenuItem menuSplit;

    private CheapWAV wav;

    private AudioRecord mRecorder;
    private boolean isRecording;

    private RecordTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.process_track);

        initData();
        initWidgets();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.process_track, menu);

        menuSplit = menu.findItem(R.id.split);
        if (menuSplit != null) {
            menuSplit.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.split: {
                new SplitTask().execute();
                return true;
            }
        }

        return false;
    }

    private void initData() {
        Bundle bundle = getIntent().getBundleExtra(BUNDLE);
        if (bundle != null) {
            wav = (CheapWAV) bundle.getSerializable(EXTRA_TRACK);
        }
    }

    private void initWidgets() {
        textDuration = (TextView) findViewById(R.id.text_duration);
        textDuration.setTypeface(FontUtils.getRobotoRegular(this));

        buttonRecord = (ImageButton) findViewById(R.id.button_record);
        if (isRecording) {
            buttonRecord.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_stop));
        }

        buttonPlay = (ImageButton) findViewById(R.id.button_play);
        if (wav != null) {
            buttonPlay.setImageDrawable(getResources().
                    getDrawable(R.drawable.ic_action_play));
            buttonPlay.setVisibility(View.VISIBLE);
        }

        progressBar = (ProgressBar) findViewById(R.id.progress);
    }

    public void record(View view) {
        if (mRecorder == null) {

            if (startRecording()) {
                isRecording = true;

                buttonRecord.setImageDrawable(getResources().
                        getDrawable(R.drawable.ic_action_stop));
            } else {
                Toast.makeText(this,
                        getResources().getString(R.string.prompt_recording_failed), Toast.LENGTH_SHORT).show();
            }
        } else {
            isRecording = false;

            buttonRecord.setImageDrawable(getResources().
                    getDrawable(R.drawable.ic_action_mic));

            stopRecording();
        }
    }

    private boolean startRecording() {
        mRecorder = new AudioRecord(RECORDER_AUDIO_SOURCE,
                RECORDER_SAMPLE_RATE_IN_HZ, RECORDER_CHANNEL_CONFIG, RECORDER_AUDIO_FORMAT, RECORDER_BUFFER_SIZE_IN_BYTES);
        LoggerFactory.obtainLogger(TAG).d("startRecording# audioSource = " + mRecorder.getAudioSource());
        LoggerFactory.obtainLogger(TAG).d("startRecording# sampleRateInHz = " + mRecorder.getSampleRate());
        LoggerFactory.obtainLogger(TAG).d("startRecording# channelConfig = " + mRecorder.getChannelConfiguration());
        LoggerFactory.obtainLogger(TAG).d("startRecording# audioFormat = " + mRecorder.getAudioFormat());
        LoggerFactory.obtainLogger(TAG).d("startRecording# bufferSizeInBytes = " + RECORDER_BUFFER_SIZE_IN_BYTES);

        LoggerFactory.obtainLogger(TAG).d("startRecording# mRecorder.getState() = " + mRecorder.getState());

        if (mRecorder.getState() == STATE_INITIALIZED) {
            mRecorder.startRecording();

            mTask = new RecordTask();
            mTask.execute();

            return true;
        }

        return false;
    }

    private void stopRecording() {
        if (mRecorder.getState() == STATE_INITIALIZED) {
            mRecorder.stop();
        }

        mRecorder.release();
        mRecorder = null;

        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    public void play(View view) {
        doPlay();
    }

    @Override
    void onPlayerPreparedSuccessfully(int duration) {
        textDuration.setText(
                String.format(getResources().getString(R.string.process_track_duration),
                        0, DurationUtils.format(duration)));

//        buttonPlay.setImageDrawable(getResources().
//                getDrawable(R.drawable.ic_action_play));
//        buttonPlay.setVisibility(View.VISIBLE);
    }

    @Override
    void onPlayerPreparationFailed() {
//        textDuration.setText("");
//        buttonPlay.setImageDrawable(getResources().
//                getDrawable(R.drawable.ic_action_play));
//        buttonPlay.setVisibility(View.GONE);

        Toast.makeText(this,
                getResources().getString(R.string.prompt_player_preparation_failed), Toast.LENGTH_SHORT).show();
    }

    @Override
    void onPlayerStarted() {
        super.onPlayerStarted();

        if (menuSplit != null) {
            menuSplit.setVisible(false);
        }

        buttonPlay.setImageDrawable(getResources().
                getDrawable(R.drawable.ic_action_pause));
    }

    @Override
    void onPlayerInProgress(int currentPosition, int duration) {
        LoggerFactory.obtainLogger(TAG).
                d(String.format("onPlayerInProgress# currentPosition = %d, duration = %d", currentPosition, duration));

        textDuration.setText(
                String.format(getResources().getString(R.string.process_track_duration),
                        DurationUtils.format(currentPosition), DurationUtils.format(duration)));
    }

    @Override
    void onPlayerPaused() {
        super.onPlayerPaused();

        if (menuSplit != null) {
            menuSplit.setVisible(true);
        }

        buttonPlay.setImageDrawable(getResources().
                getDrawable(R.drawable.ic_action_play));
    }

    @Override
    CheapWAV getWav() {
        return wav;
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    private class RecordTask extends AsyncTask<Void, Void, Void> {
        private CheapWAV wav;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                wav = new CheapWAV(new File(FileUtils.getFilename(getApplicationContext())),
                        RECORDER_AUDIO_FORMAT, RECORDER_CHANNEL_CONFIG, RECORDER_SAMPLE_RATE_IN_HZ);

                byte data[] = new byte[RECORDER_BUFFER_SIZE_IN_BYTES];

                int read;
                while(isRecording) {
                    read = mRecorder.read(data, 0, RECORDER_BUFFER_SIZE_IN_BYTES);
                    LoggerFactory.obtainLogger(TAG).d("RecordTask.doInBackground# data = " + data.length);
                    LoggerFactory.obtainLogger(TAG).d("RecordTask.doInBackground# read result = " + read);

                    if (read != AudioRecord.ERROR_INVALID_OPERATION) {
                        wav.write(data);
                    }
                }
            } catch (Exception e) {
                LoggerFactory.obtainLogger(TAG).e(e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            if (wav != null) {
                try {
                    wav.write(null, true);
                } catch (IOException e) {
                    LoggerFactory.obtainLogger(TAG).e(e.getMessage(), e);
                }

                ProcessTrackActivity.this.wav = wav;

                buttonPlay.setImageDrawable(getResources().
                        getDrawable(R.drawable.ic_action_play));
                buttonPlay.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            // TODO update progress + text duration
        }
    }

    private class SplitTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                CheapWAV.split(
                        ProcessTrackActivity.this, wav, getCurrentPosition());
                return true;
            } catch (Exception e) {
                LoggerFactory.obtainLogger(TAG).e(e.getMessage(), e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if (!result) {
                Toast.makeText(ProcessTrackActivity.this,
                        getResources().getString(R.string.prompt_split_failed), Toast.LENGTH_SHORT).show();
                return;
            }

            if (wav.file.delete()) {
                LoggerFactory.obtainLogger(TAG).
                        d(String.format("onPostExecute# File %s deleted successully",
                                wav.file.getAbsolutePath()));
            } else {
                LoggerFactory.obtainLogger(TAG).
                        d(String.format("onPostExecute# File %s deletion failed",
                                wav.file.getAbsolutePath()));
            }

            setResult(Activity.RESULT_OK);
            finish();
        }
    }
}
