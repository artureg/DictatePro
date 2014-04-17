package com.wiseapps.davacon;

import android.app.Activity;
import android.content.Context;
import android.media.*;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import com.wiseapps.davacon.core.se.SDCardUtils;
import com.wiseapps.davacon.core.se.SEProject;
import com.wiseapps.davacon.core.se.SERecord;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.speex.SpeexWrapper;

import java.io.File;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/16/14
 *         Time: 3:22 PM
 */
public class TestActivity extends Activity {
    private static final String TAG = TestActivity.class.getSimpleName();

    static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;

    static final int SAMPLE_RATE_IN_HZ = 8000;
    static final int CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO;
    static final int CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO;
    static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    static final int MODE = AudioTrack.MODE_STREAM;

    static final short BITS_PER_SAMPLE = 8;

    private static final String APP_PATH = "/Android/data/";
    private static final String PROJECT_NAME = "project.xml";

    private Button record, play, stop;

    private enum PlayerState {
        PLAYING, PAUSED, STOPPED
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test);

        record = (Button) findViewById(R.id.btn_record);
        play = (Button) findViewById(R.id.btn_play);
        stop = (Button) findViewById(R.id.btn_stop);
    }

    public void write(View view) {
//        SEProject project = new SEProject(this);
//        project.setProjectPath(getProjectPath(this) + "/" + PROJECT_NAME);
//
//        SERecord record = new SERecord(0, 5, "Path1");
//        project.addRecord(record);
//
//        record = new SERecord(0, 5, "Path2");
//        project.addRecord(record);
//
//        record = new SERecord(0, 5, "Path3");
//        project.addRecord(record);
//
//        SDCardUtils.writeProject(project);
    }

    public void read(View view) {
//        SEProject project = SDCardUtils.readProject(this);
//        LoggerFactory.obtainLogger(TAG).
//                d("read# " + project);
    }

    private RecordingThread recorder;
    public void record(View view) {
        String filePath = getProjectPath(this) + "/test.wav";

        if (recorder == null) {
            recorder = new RecordingThread(filePath);
            recorder.start();

            record.setText("Stop Recording");
        } else {
            recorder.stopRecording();

            record.setText("Start Recording");
        }
    }

    private PlayingThread player;
    public void play(View view) {
        String filePath = getProjectPath(this) + "/test.wav";

        if (player == null) {
            player = new PlayingThread(filePath);
            player.start();

            play.setText("Pause");
            stop.setVisibility(View.VISIBLE);
        } else {
            player.pausePlaying();

            play.setText("Play");
            stop.setVisibility(View.VISIBLE);
        }
    }

    public void stop(View view) {
        player.stopPlaying();
        player = null;

        play.setText("Play");
        stop.setVisibility(View.GONE);
    }

    private static String getProjectPath(Context context) {
        if (context == null || context.getApplicationContext() == null) {
            throw new IllegalArgumentException();
        }

        String path =  Environment.getExternalStorageDirectory() +
                APP_PATH + context.getApplicationContext().getPackageName();

        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        return path;
    }

    private class PlayingThread extends Thread {
        private final String filePath;

        private PlayerState state = PlayerState.PLAYING;

        private PlayingThread(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void run() {
            int minBufferSize =
                    AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_OUT, AUDIO_FORMAT);

            AudioTrack audioTrack = new AudioTrack(STREAM_TYPE, SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_OUT, AUDIO_FORMAT,
                    minBufferSize, MODE);

            int format = SpeexWrapper.getFormat(filePath);

            int offset = 0;
            byte[] data = SpeexWrapper.read(filePath, offset, 1, format);
            while(state != PlayerState.STOPPED) {
                if (state == PlayerState.PAUSED) {
                    continue;
                }

                audioTrack.write(data, 0, data.length);
                audioTrack.play();

                data = SpeexWrapper.read(filePath, offset, 1, format);

                ++offset;
            }

            audioTrack.stop();
            audioTrack.release();
        }

        void pausePlaying() {
            state = PlayerState.PAUSED;
        }

        void stopPlaying() {
            state = PlayerState.STOPPED;
        }
    }

    private class RecordingThread extends Thread {
        private final String filePath;

        private boolean running = true;

        private RecordingThread(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void run() {
            int minBufferSize =
                    AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_IN, AUDIO_FORMAT);

            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_IN, AUDIO_FORMAT, minBufferSize);

            byte[] data = new byte[minBufferSize];

//            int i = 0;
            while(running) {
                LoggerFactory.obtainLogger(TAG).
                        d("RecordingThread.run# running");

                audioRecord.read(data, 0, data.length);

                int format = SpeexWrapper.getFormat(filePath);
                SpeexWrapper.write(filePath, data, format);

//                ++i;
//                if (i == 10) {
//                    return;
//                }
            }
            LoggerFactory.obtainLogger(TAG).
                    d("RecordingThread.run# resume running");

            audioRecord.stop();
            audioRecord.release();
        }

        private void stopRecording() {
            LoggerFactory.obtainLogger(TAG).
                    d("RecordingThread.stopRecording# ");
            running = false;
        }
    }
}
