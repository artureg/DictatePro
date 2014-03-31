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
import com.wiseapps.davacon.core.SoundFile;
import com.wiseapps.davacon.core.wav.SoundFileHandler;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.utils.DurationUtils;
import com.wiseapps.davacon.utils.FileUtils;
import com.wiseapps.davacon.utils.FontUtils;

import java.io.File;
import java.io.IOException;

import static android.media.AudioRecord.*;
import static com.wiseapps.davacon.ActivityNavigator.*;
import static com.wiseapps.davacon.core.wav.WAVFile.*;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/20/14
 *         Time: 9:23 AM
 *
 *
 * TODO 1.progress bar 2.re-record the file 3.correct splitting 4. test cases
 * TODO 5. docs 6. check with Igor's sample file 6.crash tests 7. convertion
 *
 *         TODO what's going on when leaving a screen during recording (see iPhone)
 *
 *         TODO handle onDestroy with recording (consult iPhone)
 *         TODO handle recorder release once the recording is finished
 */
public class ProcessTrackActivity extends PlayingCapableActivity {
    private static final String TAG = ProcessTrackActivity.class.getSimpleName();

    private SoundFile sf;

    private TextView textDuration;
    private ImageButton buttonRecord;
    private ImageButton buttonPlay;
    private ProgressBar progressBar;

    private MenuItem menuSplit;

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
            File track = (File) bundle.getSerializable(EXTRA_TRACK);
            if (track == null) {
                return;
            }

            try {
                sf = SoundFile.create(track);
            } catch (IOException e) {
                LoggerFactory.obtainLogger(TAG).
                        e(String.format("initData# Couldn't read %s", track.getAbsolutePath()), e);
            }
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
        if (sf != null) {
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
    }

    @Override
    void onPlayerPreparationFailed() {
        Toast.makeText(this,
                getResources().getString(R.string.prompt_player_preparation_failed), Toast.LENGTH_SHORT).show();
    }

    @Override
    void onPlayerStarted(int duration) {
        super.onPlayerStarted(duration);

        if (menuSplit != null) {
            menuSplit.setVisible(false);
        }

        textDuration.setVisibility(View.VISIBLE);

        progressBar.setMax(duration);
        progressBar.setVisibility(View.VISIBLE);

        buttonPlay.setImageDrawable(getResources().
                getDrawable(R.drawable.ic_action_pause));
    }

    @Override
    void onPlayerInProgress(int currentPosition, int duration) {
        LoggerFactory.obtainLogger(TAG).
                d(String.format("onPlayerInProgress# currentPosition = %d, duration = %d", currentPosition, duration));

        progressBar.setProgress(currentPosition);

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
    void onPlayerCompleted() {
        super.onPlayerCompleted();

        textDuration.setText("");
        textDuration.setVisibility(View.GONE);

        progressBar.setVisibility(View.GONE);

        buttonPlay.setImageDrawable(getResources().
                getDrawable(R.drawable.ic_action_play));
    }

    @Override
    SoundFile getSoundFile() {
        return sf;
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    private class RecordTask extends AsyncTask<Void, Integer, Void> {
        private SoundFile sf;

        private static final int MAX = 180;

        private int startMillis= 0,
                endMillis = MAX * 1000; // 180 seconds
        private long currentTimeMillis;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            currentTimeMillis = System.currentTimeMillis();

            textDuration.setText(
                    String.format(getResources().getString(R.string.process_track_duration),
                            DurationUtils.format(startMillis), DurationUtils.format(endMillis)));
            textDuration.setVisibility(View.VISIBLE);

            progressBar.setMax(endMillis);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // rewrite if exists
                sf = SoundFile.create(new File(ProcessTrackActivity.this.sf != null ?
                        ProcessTrackActivity.this.sf.getFile().getAbsolutePath() : FileUtils.getFilename(getApplicationContext())));

                byte data[] = new byte[RECORDER_BUFFER_SIZE_IN_BYTES];

                int read;
                while(isRecording) {
                    read = mRecorder.read(data, 0, RECORDER_BUFFER_SIZE_IN_BYTES);
                    LoggerFactory.obtainLogger(TAG).d("RecordTask.doInBackground# data = " + data.length);
                    LoggerFactory.obtainLogger(TAG).d("RecordTask.doInBackground# read result = " + read);

                    if (read != AudioRecord.ERROR_INVALID_OPERATION) {
                        sf.write(data);
                    }

                    long currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis - this.currentTimeMillis > 100) {
                        startMillis += 100;
                        this.currentTimeMillis = currentTimeMillis;

                        publishProgress(startMillis);
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

            if (sf != null) {
                try {
                    sf.consume();
                } catch (IOException e) {
                    LoggerFactory.obtainLogger(TAG).e(e.getMessage(), e);
                }

                ProcessTrackActivity.this.sf = sf;

                buttonPlay.setImageDrawable(getResources().
                        getDrawable(R.drawable.ic_action_play));
                buttonPlay.setVisibility(View.VISIBLE);

                textDuration.setText("");
                textDuration.setVisibility(View.GONE);

                progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            textDuration.setText(
                    String.format(getResources().getString(R.string.process_track_duration),
                            DurationUtils.format(startMillis), DurationUtils.format(endMillis)));
            progressBar.setProgress(startMillis / 100);
        }
    }

    private class SplitTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                SoundFileHandler.split(
                        ProcessTrackActivity.this, sf, getCurrentPosition());
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

            if (sf.getFile().delete()) {
                LoggerFactory.obtainLogger(TAG).
                        d(String.format("onPostExecute# File %s deleted successully",
                                sf.getFile().getAbsolutePath()));
            } else {
                LoggerFactory.obtainLogger(TAG).
                        d(String.format("onPostExecute# File %s deletion failed",
                                sf.getFile().getAbsolutePath()));
            }

            setResult(Activity.RESULT_OK);
            finish();
        }
    }
}
