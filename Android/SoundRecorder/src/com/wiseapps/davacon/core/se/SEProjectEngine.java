package com.wiseapps.davacon.core.se;

import android.content.Context;
import android.media.*;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import com.wiseapps.davacon.logging.LoggerFactory;

import static com.wiseapps.davacon.core.se.SEAudioStream.*;
import static com.wiseapps.davacon.core.se.SESoundRecorder.*;
import static com.wiseapps.davacon.core.se.SESoundPlayer.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 11:52 AM
 *
 * Set of public methods could not be changed!!!
 */
public class SEProjectEngine extends SEAudioStreamEngine {
    private static final String TAG = SEProjectEngine.class.getSimpleName();

    static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;

    static final int SAMPLE_RATE_IN_HZ = 8000;
    static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    static final int MIN_BUFFER_SIZE =
            AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT);

    static final int MODE = AudioTrack.MODE_STREAM;

    private final Context context;

    private final SEProject project;

    private SEAudioStream streamWrite;

    private SESoundRecorder recorder;
    private SESoundPlayer player;

    public SEProjectEngine(Context context, final SEProject project) {
        super();

        this.context = context;

        this.project = project;

        streamWrite = new SERecord().getAudioStream(project, context);
    }

    /**
     * Plays the stream.
     * Playing is stream-based. In case the stream is encoded to format we can't play as is,
     * it is decoded with the native library method.
     *
     * After the native method returns don't forget to update observer's state!
     * @see com.wiseapps.davacon.core.se.SEPlayerStateListener
     */
    public void startPlaying() {
        if (project == null) {
            throw new IllegalStateException();
        }

        if (player == null) {
            player = new SESoundPlayer(project.getAudioStream(context));
            player.addHandler(playerHandler);
        }

        player.start();
    }

    /**
     * Pauses stream playing
     */
    public void pausePlaying() {
        if (project == null) {
            throw new IllegalStateException();
        }

        if (player == null) {
            throw new IllegalStateException();
        }

        player.pause();
    }

    /**
     * Stops stream playing
     */
    public void stopPlaying() {
        if (project == null) {
            throw new IllegalStateException();
        }

        if (player == null) {
            throw new IllegalStateException();
        }

        player.stop();
    }

    /**
     * Starts recording.
     * Recording is stream-based. In case the stream should be recorder in some specified format,
     * it is encoded with the native library method.
     *
     * After the native method returns don't forget to update observer's state!
     * @see com.wiseapps.davacon.core.se.SERecorderStateListener
     */
    public void startRecording() {
        if (project == null) {
            throw new IllegalStateException();
        }

        if (recorder != null) {
            throw new IllegalStateException();
        }

        recorder = new SESoundRecorder(streamWrite);
        recorder.addHandler(recorderHandler);
        recorder.start();
    }

    /**
     * Stops recording
     */
    public void stopRecording() {
        if (project == null) {
            throw new IllegalStateException();
        }

        if (recorder == null) {
            throw new IllegalStateException();
        }

        recorder.stop();
    }

    @Override
    public void setCurrentTime(double currentTime) {
        // TODO implement
    }

    @Override
    public double getCurrentTime() {
        // TODO implement
        return 0;
    }

//    /**
//     * Atomically encodes the stream (without streaming and progress update)
//     */
//    public void encode() {
//        if (project == null) {
//            throw new IllegalStateException();
//        }
//
//        // TODO implement
//        // TODO use native public static int encode(String wavFilePathStr, String compressedFilePathStr);
//    }
//
//    /**
//     * Atomically decodes the stream (without streaming and progress update)
//     */
//    public void decode() {
//        if (project == null) {
//            throw new IllegalStateException();
//        }
//
//        // TODO implement
//        // TODO use native public static int decode(String compressedFilePathStr, String wavFilePathStr);
//    }

    private Handler recorderHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RECORDING_STARTED: {
                    notifyRecorderStateChanged(Event.RECORDING_STARTED);
                    state = State.RECORDING_IN_PROGRESS;
                    break;
                }
                case MSG_RECORDING_IN_PROGRESS: {
                    // TODO publish progress after one second passed
//                    notifyRecorderStateChanged(Event.RECORDING_IN_PROGRESS);
                    state = State.RECORDING_IN_PROGRESS;
                    break;
                }
                case MSG_RECORDING_STOPPED: {
                    notifyRecorderStateChanged(Event.RECORDING_STOPPED);
                    state = State.READY;
                    break;
                }
                case MSG_RECORDING_ERROR: {
                    notifyRecorderStateChanged(Event.OPERATION_ERROR);
                    state = State.READY;
                    break;
                }
            }

            super.handleMessage(msg);
        }
    };

    private Handler playerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PLAYING_STARTED: {
                    notifyRecorderStateChanged(Event.PLAYING_STARTED);
                    state = State.PLAYING_IN_PROGRESS;
                    break;
                }
                case MSG_PLAYING_IN_PROGRESS: {
                    // TODO publish progress after one second passed
//                    notifyRecorderStateChanged(Event.RECORDING_IN_PROGRESS);
                    state = State.PLAYING_IN_PROGRESS;
                    break;
                }
                case MSG_PLAYING_PAUSED: {
                    notifyRecorderStateChanged(Event.PLAYING_PAUSED);
                    state = State.READY;
                    break;
                }
                case MSG_PLAYING_STOPPED: {
                    notifyRecorderStateChanged(Event.PLAYING_STOPPED);
                    state = State.READY;
                    break;
                }
                case MSG_PLAYING_ERROR: {
                    notifyRecorderStateChanged(Event.OPERATION_ERROR);
                    state = State.READY;
                    break;
                }
            }

            super.handleMessage(msg);
        }
    };
}
