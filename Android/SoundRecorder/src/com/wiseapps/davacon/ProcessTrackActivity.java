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
import com.wiseapps.davacon.core.WAVFileWriter;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.utils.FileUtils;
import com.wiseapps.davacon.utils.FontUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.media.AudioRecord.*;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/20/14
 *         Time: 9:23 AM
 */
public class ProcessTrackActivity extends Activity {
    private static final String TAG = ProcessTrackActivity.class.getSimpleName();

    private static final String EXTRA_IS_RECORDING = "isRecording";

    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORDER_BUFFER_SIZE =
            AudioRecord.getMinBufferSize(8000, RECORDER_CHANNELS, AudioFormat.ENCODING_PCM_16BIT);

    private Object track;

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

    private String fileName;
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
            isRecording = true;

            buttonRecord.setImageDrawable(getResources().
                    getDrawable(R.drawable.ic_action_stop));

            startRecord();
        } else {
            isRecording = false;

            buttonRecord.setImageDrawable(getResources().
                    getDrawable(R.drawable.ic_action_mic));

            stopRecord();
        }
    }

    public void play(View view) {
        if (mPlayer == null) {
            isPlaying = true;

            buttonPlay.setImageDrawable(getResources().
                    getDrawable(R.drawable.ic_action_stop));

            startPlay();
        } else {
            isPlaying = false;

            buttonPlay.setImageDrawable(getResources().
                    getDrawable(R.drawable.ic_action_play));

            stopPlay();
        }
    }

    private void startRecord() {
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, RECORDER_BUFFER_SIZE);

        if (mRecorder.getState() == STATE_INITIALIZED) {
            mRecorder.startRecording();
        }

        mTask = new RecordTask();
        mTask.execute();
    }

    private void stopRecord() {
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

    private void startPlay() {
        mPlayer = new MediaPlayer();

        try {
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            LoggerFactory.obtainLogger(TAG).e(e.getMessage(), e);
        }
    }

    private void stopPlay() {
        mPlayer.release();
        mPlayer = null;
    }

    private class RecordTask extends AsyncTask<Void, Void, Void> {
        private WAVFileWriter writer;

        @Override
        protected Void doInBackground(Void... voids) {
            fileName = FileUtils.getFilename(getApplicationContext());

            try {
                writer = new WAVFileWriter(new File(fileName));

                byte data[] = new byte[RECORDER_BUFFER_SIZE];

                int read;
                while(isRecording) {
                    read = mRecorder.read(data, 0, RECORDER_BUFFER_SIZE);
                    if (read != AudioRecord.ERROR_INVALID_OPERATION) {
                        writer.write(data);
                    }
                }
            } catch (Exception e) {
                LoggerFactory.obtainLogger(TAG).e(e.getMessage(), e);
            }

//            FileOutputStream out = null;
//            try {
//                byte data[] = new byte[RECORDER_BUFFER_SIZE];
//
//                out = new FileOutputStream(fileName);
//
//                int read;
//                while(isRecording) {
//                    read = mRecorder.read(data, 0, RECORDER_BUFFER_SIZE);
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
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            // TODO update progress + text duration
        }
    }
}
