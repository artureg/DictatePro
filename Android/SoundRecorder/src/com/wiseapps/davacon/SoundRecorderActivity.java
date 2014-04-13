package com.wiseapps.davacon;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import com.wiseapps.davacon.core.soundeditor.*;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.utils.FileUtils;

import java.io.File;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 4/13/14
 *         Time: 9:50 AM
 */
public class SoundRecorderActivity extends Activity {
    private static final String TAG = SoundRecorderActivity.class.getSimpleName();

    private SEProject project;
    private SEAudioStreamPlayer player;

    private ImageButton buttonRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sound_recorder);

        initData();
        initWidgets();
    }

    @Override
    protected void onDestroy() {
        // TODO release audio player
//        if (recorder != null) {
//            recorder.stop();
//        }

        super.onDestroy();
    }

    private void initData() {
        try {
            File root = FileUtils.getRoot(this);

            File[] files = root.listFiles();
            if (files != null && files.length != 0) {
                project = new SEProject(getContext(), files[0].getName());
                return;
            }

            project = new SEProject(getContext());
        } catch (Exception e) {
            LoggerFactory.obtainLogger(TAG).
                    e("initData# ", e);
        }
    }

    private void initWidgets() {
        buttonRecord = (ImageButton) findViewById(R.id.record);
    }

    public void rewind(View view) {
        // TODO implements
    }

    public void record(View view) {
        SERecord record = new SERecord(project);
        record.getAudioStream().setListener(recorderStateListener);

        if (!record.getAudioStream().isRecording()) {
            record.getAudioStream().startRecording();
        } else {
            record.getAudioStream().stopRecording();
        }
    }

    public void forward(View view) {
        // TODO implement
    }

    public void start(View view) {
        // TODO implement
    }

    public void play(View view) {
        if (player == null) {
            player = new SEAudioStreamPlayer();
            player.setListener(playerStateListener);
        }
        player.initWithStream(project.getAudioStream());

        if (player.getState() == SEAudioStreamPlayer.State.PLAYING) {
            // TODO implement
            return;
        }

        if (player.getState() == SEAudioStreamPlayer.State.PAUSED) {
            // TODO implement
            return;
        }

        if (player.getState() == SEAudioStreamPlayer.State.STOPPED) {
            // TODO implement
            return;
        }
    }

    public void end(View view) {
        // TODO implement
    }

    private Context getContext() {
        return this;
    }

    private SEPlayerStateListener playerStateListener = new SEPlayerStateListener() {
        @Override
        public void audioStreamPlayerDidStartPlaying(SEAudioStreamPlayer player) {
            // TODO implement
        }

        @Override
        public void audioStreamPlayerDidPause(SEAudioStreamPlayer player) {
            // TODO implement
        }

        @Override
        public void audioStreamPlayerDidContinue(SEAudioStreamPlayer player) {
            // TODO implement
        }

        @Override
        public void audioStreamPlayer(SEAudioStreamPlayer player, long position, long duration) {
            // TODO implement
        }

        @Override
        public void audioStreamPlayerDidFinishPlaying(SEAudioStreamPlayer player, boolean stopped) {
            // TODO implement
        }
    };

    private SERecorderStateListener recorderStateListener = new SERecorderStateListener() {
        @Override
        public void recordingStarted(SERecordAudioStream stream) {
            LoggerFactory.obtainLogger(TAG).
                    d("SoundRecorderHandler.handleMessage# MSG_RECORDING_STARTED");

            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_2_record_enabled));
        }

        @Override
        public void dataRecorded(SERecordAudioStream stream, long duration) {
            LoggerFactory.obtainLogger(TAG).
                    d("SoundRecorderHandler.handleMessage# MSG_DATA_RECORDED");

            // TODO update progress
        }

        @Override
        public void recordingStopped(SERecordAudioStream stream) {
            LoggerFactory.obtainLogger(TAG).
                    d("SoundRecorderHandler.handleMessage# MSG_RECORDING_STOPPED");

            buttonRecord.setImageDrawable(
                    getResources().getDrawable(R.drawable.button02_1_record_disabled));
        }

        @Override
        public void errorOccured(String errorMessage) {
            LoggerFactory.obtainLogger(TAG).
                    d("SoundRecorderHandler.handleMessage# MSG_RECORDING_ERROR");

            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    };

//    private class SoundRecorderHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MSG_RECORDING_STARTED: {
//                    LoggerFactory.obtainLogger(TAG).
//                            d("SoundRecorderHandler.handleMessage# MSG_RECORDING_STARTED");
//                    buttonRecord.setImageDrawable(
//                            getResources().getDrawable(R.drawable.button02_2_record_enabled));
//
//                    break;
//                }
//                case MSG_DATA_RECORDED: {
//                    LoggerFactory.obtainLogger(TAG).
//                            d("SoundRecorderHandler.handleMessage# MSG_DATA_RECORDED");
//                    // TODO update progress, write to file
//                    // TODO save record's data
//                    break;
//                }
//                case MSG_RECORDING_STOPPED: {
//                    LoggerFactory.obtainLogger(TAG).
//                            d("SoundRecorderHandler.handleMessage# MSG_RECORDING_STOPPED");
//                    buttonRecord.setImageDrawable(
//                            getResources().getDrawable(R.drawable.button02_1_record_disabled));
//
//                    break;
//                }
//                case MSG_RECORDING_ERROR: {
//                    LoggerFactory.obtainLogger(TAG).
//                            d("SoundRecorderHandler.handleMessage# MSG_RECORDING_ERROR");
//                    Toast.makeText(getContext(), "" + msg.obj, Toast.LENGTH_SHORT).show();
//                    break;
//                }
//            }
//
//            super.handleMessage(msg);
//        }
//    }
}
