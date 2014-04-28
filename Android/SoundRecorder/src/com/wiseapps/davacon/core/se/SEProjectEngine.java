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
    public static final int NUM_CHANNELS = 1;

    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    public static final int MIN_BUFFER_SIZE =
            AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_IN, AUDIO_FORMAT);

    public static final short BITS_PER_SAMPLE = 16;

    public static final int MODE = AudioTrack.MODE_STREAM;
   
    public static final int FILE_FORMAT_WAV = 0;
    public static final int FILE_FORMAT_SPEEX = 1;
    
    /**
     * FILE_FORMAT_WAV - wav, FILE_FORMAT_SPEEX - speex
     */
    public static final int FILE_FORMAT = FILE_FORMAT_WAV;

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
            int position = (int) getCurrentTime();
            int duration = (int) getDuration();

            player = new SESoundPlayer(project.getAudioStream(), position, duration);
            player.addHandler(playerStateListener);
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

        int position = (int) getCurrentTime();
        int duration = (int) getDuration();

        recorder = new SESoundRecorder(record.getAudioStream(), position, duration);
        recorder.addHandler(recorderStateListener);
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
        // goto start || rewind (with project stream start has been reached)
        if (currentPosition == Long.MIN_VALUE || currentPosition < 0) {
            project.position = 0;
            project.updateRecordPositions();

            state = State.READY;
            if (player != null) {
                player.removeHandler(playerStateListener);

                player.pause();
                player = null;
            }

            return;
        }

        // goto end || forward (with project stream end has been reached)
        if (currentPosition == Long.MAX_VALUE || currentPosition > project.duration) {
            project.position = project.duration;
            project.updateRecordPositions();

            state = State.READY;
            if (player != null) {
                player.removeHandler(playerStateListener);

                player.pause();
                player = null;
            }

            return;
        }

        project.position = currentPosition;
        project.updateRecordPositions();

        state = State.READY;
        if (player != null) {
            player.removeHandler(playerStateListener);

            player.pause();
            player = null;
        }
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
            player.removeHandler(playerStateListener);

            player.pause();
            player = null;
        }

        if (recorder != null) {
            recorder.removeHandler(recorderStateListener);
            recorder.stop();
            recorder = null;
        }
    }

    private SESoundRecorderStateListener recorderStateListener = new SESoundRecorderStateListener() {
        @Override
        public void onRecordingStarted(int position, int duration) {
            for (SERecorderStateListener listener : recorderStateListeners) {
                if (listener != null) {
                    listener.recordingStarted(position, duration);
                }
            }

            state = State.RECORDING_IN_PROGRESS;
        }

        @Override
        public void onRecordingInProgress(int position, int duration) {
            for (SERecorderStateListener listener : recorderStateListeners) {
                if (listener != null) {
                    listener.recordingInProgress(position, duration);
                }
            }

//            state = State.RECORDING_IN_PROGRESS;
        }

        @Override
        public void onRecordingStopped(int position, int duration) {
            for (SERecorderStateListener listener : recorderStateListeners) {
                if (listener != null) {
                    listener.recordingStopped(position, duration);
                }
            }

            state = State.READY;
        }

        @Override
        public void onRecordingError(int position, int duration) {
            for (SERecorderStateListener listener : recorderStateListeners) {
                if (listener != null) {
                    listener.onError(position, duration, "Recording failed!");
                }
            }

            state = State.READY;
        }
    };

    private SESoundPlayerStateListener playerStateListener = new SESoundPlayerStateListener() {
        @Override
        public void onPlayingStarted(int position, int duration) {
            for (SEPlayerStateListener listener : playerStateListeners) {
                if (listener != null) {
                    listener.playingStarted(position, duration);
                }
            }

            state = State.PLAYING_IN_PROGRESS;
        }

        @Override
        public void onPlayingInProgress(int position, int duration) {
            for (SEPlayerStateListener listener : playerStateListeners) {
                if (listener != null) {
                    listener.playingInProgress(position, duration);
                }
            }

//            state = State.PLAYING_IN_PROGRESS;
        }

        @Override
        public void onPlayingPaused(int position, int duration) {
            for (SEPlayerStateListener listener : playerStateListeners) {
                if (listener != null) {
                    listener.playingPaused(position, duration);
                }
            }

            state = State.READY;
        }

        @Override
        public void onPlayingError(int position, int duration) {
            for (SEPlayerStateListener listener : playerStateListeners) {
                if (listener != null) {
                    listener.onError(position, duration, "Playing failed!");
                }
            }

            state = State.READY;
        }
    };
}
