package com.wiseapps.davacon;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import com.wiseapps.davacon.core.soundeditor.SEProject;
import com.wiseapps.davacon.logging.LoggerFactory;

import static com.wiseapps.davacon.SoundRecorder.*;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 4/13/14
 *         Time: 9:50 AM
 */
public class SoundRecorderActivity extends Activity {
    private static final String TAG = SoundRecorderActivity.class.getSimpleName();

    private SEProject project;

    private SoundRecorder recorder;

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
        if (recorder != null) {
            recorder.stop();
        }

        super.onDestroy();
    }

    private void initData() {
//        // TODO init project here! - "read from sd card" if already exists, otherwise init from scratch
//        project = SDCardUtils.getProjectFromSDCard(getContext());
//
//        if (project == null) {
//            // TODO project = new SEProject();
//            SDCardUtils.saveProjectToSDCard(getContext(), project);
//        }
    }

    private void initWidgets() {
        buttonRecord = (ImageButton) findViewById(R.id.record);
    }

    public void rewind(View view) {
        // TODO implements
    }

    public void record(View view) {
        if (recorder == null) {
            recorder = new SoundRecorder();
            recorder.addHandler(new SoundRecorderHandler());
        }

        if (!recorder.isRecording()) {
            // TODO init SERecord object

            recorder.start();
        } else {
            recorder.stop();
        }
    }

    public void forward(View view) {
        // TODO implement
    }

    public void start(View view) {
        // TODO implement
    }

    public void play(View view) {
        // TODO implement
    }

    public void end(View view) {
        // TODO implement
    }

    private Context getContext() {
        return this;
    }


    private class SoundRecorderHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RECORDING_STARTED: {
                    LoggerFactory.obtainLogger(TAG).
                            d("SoundRecorderHandler.handleMessage# MSG_RECORDING_STARTED");
                    buttonRecord.setImageDrawable(
                            getResources().getDrawable(R.drawable.button02_2_record_enabled));

                    break;
                }
                case MSG_DATA_RECORDED: {
                    LoggerFactory.obtainLogger(TAG).
                            d("SoundRecorderHandler.handleMessage# MSG_DATA_RECORDED");
                    // TODO update progress, write to file
                    // TODO save record's data
                    break;
                }
                case MSG_RECORDING_STOPPED: {
                    LoggerFactory.obtainLogger(TAG).
                            d("SoundRecorderHandler.handleMessage# MSG_RECORDING_STOPPED");
                    buttonRecord.setImageDrawable(
                            getResources().getDrawable(R.drawable.button02_1_record_disabled));

                    break;
                }
                case MSG_RECORDING_ERROR: {
                    LoggerFactory.obtainLogger(TAG).
                            d("SoundRecorderHandler.handleMessage# MSG_RECORDING_ERROR");
                    Toast.makeText(getContext(), "" + msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                }
            }

            super.handleMessage(msg);
        }
    }
}
