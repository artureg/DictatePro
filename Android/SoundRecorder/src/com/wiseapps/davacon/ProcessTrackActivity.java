package com.wiseapps.davacon;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.wiseapps.davacon.core.WAVFileWriter;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.utils.FileUtils;
import com.wiseapps.davacon.utils.FontUtils;

import java.io.File;
import java.io.IOException;

import static android.media.AudioRecord.*;
import static com.wiseapps.davacon.ActivityNavigator.*;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/20/14
 *         Time: 9:23 AM
 */
public class ProcessTrackActivity extends Activity {
    private static final String TAG = ProcessTrackActivity.class.getSimpleName();

    private static final String EXTRA_IS_RECORDING = "isRecording";

    private static final int RECORDER_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private static final int RECORDER_SAMPLE_RATE_IN_HZ = 44100;
    private static final int RECORDER_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORDER_BUFFER_SIZE_IN_BYTES =
            AudioRecord.getMinBufferSize(RECORDER_SAMPLE_RATE_IN_HZ, RECORDER_CHANNEL_CONFIG, RECORDER_AUDIO_FORMAT);

    // TODO change to WAVFile
    private File track;

    private TextView textDuration;
    private ImageButton buttonRecord;
    private ImageButton buttonPlay;
    private ProgressBar progressBar;

    // TODO handle onDestroy with recording (consult iPhone)
    // TODO handle recorder release once the recording is finished

    private AudioRecord mRecorder;
    private boolean isRecording;

    private MediaPlayer mPlayer;
    private boolean isPlaying;

    private RecordTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.process_track);

        initData(savedInstanceState);
        initWidgets();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.process_track, menu);

        if (track != null) {
            MenuItem menuSplit = menu.findItem(R.id.split);
            if (menuSplit != null) {
                menuSplit.setVisible(false);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.split: {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(EXTRA_IS_RECORDING, isRecording);
        super.onSaveInstanceState(outState);
    }

    private void initData(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isRecording = savedInstanceState.getBoolean(EXTRA_IS_RECORDING, false);
        }

        Bundle bundle = getIntent().getBundleExtra(BUNDLE);
        track = (File) bundle.getSerializable(EXTRA_TRACK);
    }

    private void initWidgets() {
        textDuration = (TextView) findViewById(R.id.text_duration);
        textDuration.setTypeface(FontUtils.getRobotoRegular(this));

        buttonRecord = (ImageButton) findViewById(R.id.button_record);
        if (isRecording) {
            buttonRecord.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_stop));
        }

        buttonPlay = (ImageButton) findViewById(R.id.button_play);

        if (track != null) {
            findViewById(R.id.button_play).setVisibility(View.VISIBLE);
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

    public void play(View view) {
        if (mPlayer == null) {
            isPlaying = true;

            buttonPlay.setImageDrawable(getResources().
                    getDrawable(R.drawable.ic_action_pause));

            startPlaying();
        } else {
            isPlaying = false;

            buttonPlay.setImageDrawable(getResources().
                    getDrawable(R.drawable.ic_action_play));

            stopPlaying();
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

    private void startPlaying() {
        mPlayer = new MediaPlayer();

        try {
            mPlayer.setDataSource(track.getAbsolutePath());
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            LoggerFactory.obtainLogger(TAG).e(e.getMessage(), e);
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private class RecordTask extends AsyncTask<Void, Void, Void> {
        private WAVFileWriter writer;

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                writer = new WAVFileWriter(new File(FileUtils.getFilename(getApplicationContext())));

                byte data[] = new byte[RECORDER_BUFFER_SIZE_IN_BYTES];

                int read;
                while(isRecording) {
                    read = mRecorder.read(data, 0, RECORDER_BUFFER_SIZE_IN_BYTES);
                    LoggerFactory.obtainLogger(TAG).d("RecordTask.doInBackground# data = " + data.length);
                    LoggerFactory.obtainLogger(TAG).d("RecordTask.doInBackground# read result = " + read);

                    if (read != AudioRecord.ERROR_INVALID_OPERATION) {
                        writer.write(data);
                    }
                }
            } catch (Exception e) {
                LoggerFactory.obtainLogger(TAG).e(e.getMessage(), e);
            }

//            FileOutputStream out = null;
//            try {
//                byte data[] = new byte[RECORDER_BUFFER_SIZE_IN_BYTES];
//
//                out = new FileOutputStream(fileName);
//
//                int read;
//                while(isRecording) {
//                    read = mRecorder.read(data, 0, RECORDER_BUFFER_SIZE_IN_BYTES);
//                    if (read != AudioRecord.ERROR_INVALID_OPERATION) {
//                        out.write(data);
//                    }
//                }
//
//                out.flush();
//                out.close();
//            } catch (Exception e) {
//                LoggerFactory.obtainLogger(TAG).e(e.getMessage(), e);
//            } finally {
//                if (out != null) {
//                    try {
//                        out.close();
//                    } catch (Exception e) {
//                        LoggerFactory.obtainLogger(TAG).e(e.getMessage(), e);
//                    }
//                }
//            }

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            if (writer != null) {
                try {
                    writer.consume();
                } catch (IOException e) {
                    LoggerFactory.obtainLogger(TAG).e(e.getMessage(), e);
                }

                track = writer.getWav().getFile();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            // TODO update progress + text duration
        }
    }
}
