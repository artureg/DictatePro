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
import com.wiseapps.davacon.core.SoundFileHandler;
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
 *
 *
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/20/14
 *         Time: 9:23 AM
 */
public class ProcessTrackActivity extends StreamingCapableActivity /*PlayingCapableActivity*/ {
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

    /**
     * Called when the activity is starting. Here track is initialized,
     * UI initialization takes place.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.process_track);

        initData();
        initWidgets();
    }


    /**
     * Initialize the contents of the screen's options menu.
     * Menu items are placed in to <var>menu</var>.
     *
     * <p>Despite the Split menu item is initialized it remains hidden
     * until the actual playback takes place.</p>
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     *         if you return false it will not be shown.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.process_track, menu);

        menuSplit = menu.findItem(R.id.split);
        if (menuSplit != null) {
            menuSplit.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Called whenever an item in the options menu is selected.
     *
     * <p>In case Split menu is called the track is splitted,
     * the user is forwarded to the {@link com.wiseapps.davacon.MainActivity MainActivity} screen.<p/>
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     *         proceed, true to consume it here.
     */
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

    /**
     * Helper method to initialize track.
     */
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

    /**
     * Helper method to initialize UI.
     */
    private void initWidgets() {
        textDuration = (TextView) findViewById(R.id.text_duration);
        textDuration.setTypeface(FontUtils.getRobotoRegular(this));
        if (sf != null) {
            textDuration.setText(
                    String.format(getResources().getString(R.string.process_track_duration),
                            DurationUtils.format(0), DurationUtils.format(sf.getDuration())));
        }

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

    /**
     * Records the track.
     *
     * <p>In case the track already exists, reresords it.</p>
     *
     * @param view The clicked view.
     */
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

    /**
     * Helper method to start recording.
     *
     * @return boolean Returns true if track recording has successfully started, false otherwise.
     */
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

    /**
     * Helper method to stop recording.
     */
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

    /**
     * Plays the track.
     *
     * @param view The clicked view.
     */
    public void play(View view) {
        doPlay();
    }

    /**
     * Callback method to update the screens data and UI after the media player has been prepared.
     */
    @Override
    void onPlayerPreparedSuccessfully() {
    }

    /**
     * Callback method to update the screens data and UI in case an error occured
     * during the media player preparation.
     */
    @Override
    void onPlayerPreparationFailed() {
        Toast.makeText(this,
                getResources().getString(R.string.prompt_player_preparation_failed), Toast.LENGTH_SHORT).show();
    }

    /**
     * Callback method to update the screens data and UI after the track playback has started.
     */
    @Override
    void onPlayerStarted() {
        super.onPlayerStarted();

        if (menuSplit != null) {
            menuSplit.setVisible(false);
        }

        progressBar.setMax((int) sf.getDuration());
        progressBar.setVisibility(View.VISIBLE);

        buttonPlay.setImageDrawable(getResources().
                getDrawable(R.drawable.ic_action_pause));
    }

    /**
     * Callback method to update the screens data and UI after the track playback is in progress.
     */
    @Override
    void onPlayerInProgress(int currentPosition) {
        LoggerFactory.obtainLogger(TAG).
                d("onPlayerInProgress# currentPosition = " + currentPosition);

        progressBar.setProgress(currentPosition);

        textDuration.setText(
                String.format(getResources().getString(R.string.process_track_duration),
                        DurationUtils.format(currentPosition), DurationUtils.format(sf.getDuration())));
    }

    /**
     * Callback method to update the screens data and UI after the track playback has been paused.
     */
    @Override
    void onPlayerPaused() {
        super.onPlayerPaused();

        if (menuSplit != null) {
            menuSplit.setVisible(true);
        }

        buttonPlay.setImageDrawable(getResources().
                getDrawable(R.drawable.ic_action_play));
    }

    /**
     * Callback method to update the screens data and UI after the track playback has completed.
     */
    @Override
    void onPlayerCompleted() {
        super.onPlayerCompleted();

        textDuration.setText(
                String.format(getResources().getString(R.string.process_track_duration),
                        DurationUtils.format(0), DurationUtils.format(sf.getDuration())));

        progressBar.setVisibility(View.GONE);

        buttonPlay.setImageDrawable(getResources().
                getDrawable(R.drawable.ic_action_play));
    }

    /**
     * Callback method to set the track to play.
     */
    @Override
    SoundFile getSoundFile() {
        return sf;
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key. Current activity is finished.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        setResult(Activity.RESULT_OK);
        finish();
    }

    /**
     * Async task to record a track.
     */
    private class RecordTask extends AsyncTask<Void, Integer, Void> {
        private SoundFile sf;

        private double startSeconds = 0,
                endSeconds = 180;
        private long currentTimeMillis;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            currentTimeMillis = System.currentTimeMillis();

            textDuration.setText(
                    String.format(getResources().getString(R.string.process_track_duration),
                            DurationUtils.format(startSeconds * 1000), DurationUtils.format(endSeconds * 1000)));
            textDuration.setVisibility(View.VISIBLE);

            progressBar.setMax((int) endSeconds);
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
                        startSeconds += 0.1;
                        this.currentTimeMillis = currentTimeMillis;

                        publishProgress((int) startSeconds);
                    }
                }
            } catch (Exception e) {
                LoggerFactory.obtainLogger(TAG).e(e.getMessage(), e);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            textDuration.setText(
                    String.format(getResources().getString(R.string.process_track_duration),
                            DurationUtils.format(startSeconds * 1000), DurationUtils.format(endSeconds * 1000)));
            progressBar.setProgress((int)startSeconds);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            if (sf != null) {
                try {
                    sf.consume();
                } catch (IOException e) {
                    LoggerFactory.obtainLogger(TAG).e(e.getMessage(), e);

                    textDuration.setText("");
                    textDuration.setVisibility(View.GONE);
                }

                ProcessTrackActivity.this.sf = sf;

                buttonPlay.setImageDrawable(getResources().
                        getDrawable(R.drawable.ic_action_play));
                buttonPlay.setVisibility(View.VISIBLE);

                textDuration.setText(
                        String.format(getResources().getString(R.string.process_track_duration),
                                DurationUtils.format(0), DurationUtils.format(sf.getDuration())));

                progressBar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Async task to split a track.
     */
    private class SplitTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
//                SoundFileHandler.split(
//                        ProcessTrackActivity.this, sf, getCurrentPosition());
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
