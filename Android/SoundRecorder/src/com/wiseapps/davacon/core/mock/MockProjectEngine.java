package com.wiseapps.davacon.core.mock;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import com.wiseapps.davacon.core.se.SEAudioStreamEngine;

import static com.wiseapps.davacon.core.mock.MockSoundRecorder.*;
import static com.wiseapps.davacon.core.mock.MockSoundPlayer.*;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/18/14
 *         Time: 5:09 PM
 */
public class MockProjectEngine extends SEAudioStreamEngine {
    private static final String TAG = MockProjectEngine.class.getSimpleName();

    static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;

    static final int SAMPLE_RATE_IN_HZ = 8000;
    static final int CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO;
    static final int CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO;
    static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    static final int MIN_BUFFER_SIZE =
            AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_IN, AUDIO_FORMAT);

    static final short BITS_PER_SAMPLE = 8;

    static final int MODE = AudioTrack.MODE_STREAM;

    private final Context context;

    private final MockProject project;

    private MockSoundRecorder recorder;
    private MockSoundPlayer player;

    public MockProjectEngine(Context context, final MockProject project) {
        super();

        this.context = context;

        this.project = project;
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
            player = new MockSoundPlayer(project.getAudioStream(context));
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
        player = null;
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
        player = null;
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

        MockRecord record = new MockRecord(project);
        record.soundPath = MockSDCardUtils.getSoundPath(context);
        project.splitRecord(record);

        recorder = new MockSoundRecorder(record.getAudioStream(context));
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
        recorder = null;
    }

    @Override
    public void setCurrentTime(long currentTime) {
//        // received -1, this means that we must go to end
//        if (currentTime == -1) {
//            currentTime = project.duration;
//        }
//
//        // rewind : project stream start has been reached
//        if (currentTime < 0) {
//            currentTime = 0;
//        }
//
//        // forward : project stream end has been reached
//        if (currentTime > project.duration) {
//            currentTime = project.duration;
//        }
//
//        // all other cases are not special,
//        // project position is set equal to what we receive
//
//        project.position = currentTime;
//
//        state = State.READY;
//        if (player != null) {
//            player.removeHandler(playerHandler);
//
//            player.stop();
//            player = null;
//        }
    }

    @Override
    public long getCurrentTime() {
//        return project.position;
        return 0;
    }

    @Override
    public long getDuration() {
//        return project.duration;
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

    public void release() {
        state = State.READY;

        if (player != null) {
            player.removeHandler(playerHandler);

            player.stop();
            player = null;
        }

        if (recorder != null) {
            recorder.removeHandler(recorderHandler);
            recorder.stop();
            recorder = null;
        }
    }

    private Handler recorderHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MSG_RECORDING_STARTED: {
//                    notifyRecorderStateChanged(Event.RECORDING_STARTED);
//                    state = State.RECORDING_IN_PROGRESS;
//                    break;
//                }
//                case MSG_RECORDING_IN_PROGRESS: {
//                    notifyRecorderStateChanged(Event.RECORDING_IN_PROGRESS);
//                    state = State.RECORDING_IN_PROGRESS;
//                    break;
//                }
//                case MSG_RECORDING_STOPPED: {
//                    notifyRecorderStateChanged(Event.RECORDING_STOPPED);
//                    state = State.READY;
//                    break;
//                }
//                case MSG_RECORDING_ERROR: {
//                    notifyRecorderStateChanged(Event.OPERATION_ERROR);
//                    state = State.READY;
//                    break;
//                }
//            }

            super.handleMessage(msg);
        }
    };

    private Handler playerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case MSG_PLAYING_STARTED: {
//                    notifyRecorderStateChanged(Event.PLAYING_STARTED);
//                    state = State.PLAYING_IN_PROGRESS;
//                    break;
//                }
//                case MSG_PLAYING_IN_PROGRESS: {
//                    notifyRecorderStateChanged(Event.RECORDING_IN_PROGRESS);
//                    state = State.PLAYING_IN_PROGRESS;
//                    break;
//                }
//                case MSG_PLAYING_PAUSED: {
//                    notifyRecorderStateChanged(Event.PLAYING_PAUSED);
//                    state = State.READY;
//                    break;
//                }
//                case MSG_PLAYING_STOPPED: {
//                    notifyRecorderStateChanged(Event.PLAYING_STOPPED);
//                    state = State.READY;
//
//                    project.position = 0;
//
//                    break;
//                }
//                case MSG_PLAYING_ERROR: {
//                    notifyRecorderStateChanged(Event.OPERATION_ERROR);
//                    state = State.READY;
//                    break;
//                }
//            }

            super.handleMessage(msg);
        }
    };
}
