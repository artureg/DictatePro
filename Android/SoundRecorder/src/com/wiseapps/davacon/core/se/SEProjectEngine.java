package com.wiseapps.davacon.core.se;

import android.content.Context;
import android.media.*;
import android.os.Handler;
import android.os.Message;

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

    public static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;

    public static final int SAMPLE_RATE_IN_HZ = 8000;
    public static final int CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO;
    public static final int CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO;
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    public static final int MIN_BUFFER_SIZE =
            AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_IN, AUDIO_FORMAT);

    public static final short BITS_PER_SAMPLE = 16;

    public static final int MODE = AudioTrack.MODE_STREAM;

    private final Context context;

    private final SEProject project;

    private SESoundRecorder recorder;
    private SESoundPlayer player;

    public SEProjectEngine(Context context, final SEProject project) {
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
            player = new SESoundPlayer(project.getAudioStream());
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

        if (player != null) {
            player.pause();
            player = null;
        }
    }

    /**
     * Stops stream playing
     */
    public void stopPlaying() {
        if (project == null) {
            throw new IllegalStateException();
        }

        if (player != null) {
            player.stop();
            player = null;
        }
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
            return;
        }

        SERecord record = new SERecord(project);
        record.soundPath = SDCardUtils.getSoundPath(context);
        project.splitRecord(record);
//        project.addRecord(record);

        recorder = new SESoundRecorder(record.getAudioStream());
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

        if (recorder != null) {
            SDCardUtils.writeProject(project);

            recorder.stop();
            recorder = null;
        }
    }

    @Override
    public void setCurrentTime(long currentPosition) {
        if (currentPosition == Long.MIN_VALUE) {
            project.position = 0;
        }

        // received -1, this means that we must go to end
        if (currentPosition == Long.MAX_VALUE) {
            project.position = project.duration;
        }

        // rewind : project stream start has been reached
        if (currentPosition < 0) {
            project.position = 0;
        }

        // forward : project stream end has been reached
        if (currentPosition > project.duration) {
            project.position = project.duration;
        }

        project.updateRecordPositions();

        state = State.READY;
        if (player != null) {
            player.removeHandler(playerHandler);

            player.stop();
            player = null;
        }

        // TODO update position seek bar
    }

    @Override
    public long getCurrentTime() {
        return project.position;
    }

    @Override
    public long getDuration() {
        return project.duration;
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
            if (recorderStateListeners == null) {
                return;
            }

            switch (msg.what) {
                case MSG_RECORDING_STARTED: {
                    for (SERecorderStateListener listener : recorderStateListeners) {
                        if (listener != null) {
                            listener.recordingStarted();
                        }
                    }

                    state = State.RECORDING_IN_PROGRESS;

                    break;
                }
                case MSG_RECORDING_IN_PROGRESS: {
                    for (SERecorderStateListener listener : recorderStateListeners) {
                        if (listener != null) {
                            listener.recordingInProgress(
                                    msg.obj != null ? (Integer) msg.obj : 0);
                        }
                    }

                    state = State.RECORDING_IN_PROGRESS;

                    break;
                }
                case MSG_RECORDING_STOPPED: {
                    for (SERecorderStateListener listener : recorderStateListeners) {
                        if (listener != null) {
                            listener.recordingStopped();
                        }
                    }

                    state = State.READY;

                    break;
                }
                case MSG_RECORDING_ERROR: {
                    for (SERecorderStateListener listener : recorderStateListeners) {
                        if (listener != null) {
                            listener.recordingStopped();
                        }
                    }

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
            if (playerStateListeners == null) {
                return;
            }

            switch (msg.what) {
                case MSG_PLAYING_STARTED: {
                    for (SEPlayerStateListener listener : playerStateListeners) {
                        if (listener != null) {
                            listener.playingStarted();
                        }
                    }

                    state = State.PLAYING_IN_PROGRESS;

                    break;
                }
                case MSG_PLAYING_IN_PROGRESS: {
                    for (SEPlayerStateListener listener : playerStateListeners) {
                        if (listener != null) {
                            listener.playingInProgress(msg.obj != null ? (Integer) msg.obj : 0);
                        }
                    }

                    state = State.PLAYING_IN_PROGRESS;

                    break;
                }
                case MSG_PLAYING_PAUSED: {
                    for (SEPlayerStateListener listener : playerStateListeners) {
                        if (listener != null) {
                            listener.playingPaused();
                        }
                    }

                    state = State.READY;

                    break;
                }
                case MSG_PLAYING_STOPPED: {
                    for (SEPlayerStateListener listener : playerStateListeners) {
                        if (listener != null) {
                            listener.playingStopped();
                        }
                    }

                    state = State.READY;

                    project.position = 0;

                    break;
                }
                case MSG_PLAYING_ERROR: {
                    for (SEPlayerStateListener listener : playerStateListeners) {
                        if (listener != null) {
                            listener.onError("Playing failed");
                        }
                    }

                    state = State.READY;

                    break;
                }
            }

            super.handleMessage(msg);
        }
    };
}
