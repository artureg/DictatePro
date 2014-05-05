package com.wiseapps.davacon;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.*;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import com.wiseapps.davacon.core.se.AudioStream;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.utils.DurationUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/16/14
 *         Time: 3:22 PM
 */
public class TestActivity extends Activity {
    private static final String TAG = TestActivity.class.getSimpleName();

    private TextView textDuration;
    private SeekBar seekPosition;

    private SeekBar seekVolume;
    private Button buttonPlay;

    private boolean running;

    // both values should be in bytes
    private int position, duration = 54400;

    private AudioManager audioManager;
    private MediaButtonReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.test);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        receiver = new MediaButtonReceiver();
        registerReceiver(receiver,
                new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));

        initWidgets();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);

        super.onDestroy();
    }

    private void initWidgets() {
        textDuration = (TextView) findViewById(R.id.duration);
        seekPosition = (SeekBar) findViewById(R.id.position);
        // TODO add change listener

        buttonPlay = (Button) findViewById(R.id.btn_play);

        seekVolume = (SeekBar) findViewById(R.id.volume);
        seekVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }

                LoggerFactory.obtainLogger(TAG).
                        d("onProgressChanged# progress = " + progress);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_PLAY_SOUND);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        updatePositionText(0, 0);
        updatePositionSeekBar(0, 0);
    }

    private void updatePositionText(long position, long duration) {
        double dP = DurationUtils.secondsFromBytes(position);
        double dD = DurationUtils.secondsFromBytes(duration);

        textDuration.setText(
                String.format(getResources().getString(R.string.process_track_duration),
                        new DecimalFormat("0.0").format(dP), new DecimalFormat("0.0").format(dD)));
    }

    private void updatePositionSeekBar(long position, long duration) {
        seekPosition.setProgress((int) position);
        seekPosition.setMax((int) duration);
    }

    public void play(View view) {
        TestSoundPlayer player = new TestSoundPlayer();
        player.addHandler(playerStateListener);
        player.start();
    }

    private SESoundPlayerStateListener playerStateListener = new SESoundPlayerStateListener() {
        @Override
        public void onPlayingStarted() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    position = 1600;
                    duration = 54400;

                    LoggerFactory.obtainLogger(TAG).
                            d("onPlayingStarted# position = " + position + ", duration = " + duration);

                    updatePositionText(position, duration);
                    updatePositionSeekBar(position, duration);
                }
            });
        }

        @Override
        public void onPlayingInProgress() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    position += 1600;

                    LoggerFactory.obtainLogger(TAG).
                            d("onPlayingStarted# position = " + position + ", duration = " + duration);

                    updatePositionText(position, duration);
                    updatePositionSeekBar(position, duration);
                }
            });
        }

        @Override
        public void onPlayingPaused() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    position += 1600;

                    LoggerFactory.obtainLogger(TAG).
                            d("onPlayingStarted# position = " + position + ", duration = " + duration);

                    updatePositionText(position, duration);
                    updatePositionSeekBar(position, duration);                }
            });
        }

        @Override
        public void onPlayingError() {
        }
    };

    private class MediaButtonReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            seekVolume.setProgress(
                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        }
    }

    private class TestSoundPlayer {
        private static final int MIN_BUFFER_SIZE = 1600;
        private static final int MULT = 4;

        public static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
        public static final int SAMPLE_RATE_IN_HZ = 8000;
        public static final int CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO;
        public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
        public static final int MODE = AudioTrack.MODE_STREAM;

        private PlayingThread thread;
        private List<SESoundPlayerStateListener> listeners = new ArrayList<SESoundPlayerStateListener>();

        void start() {
            thread = new PlayingThread(true);
            thread.start();
        }

        void pause() {
            thread.pausePlaying();
        }

        void addHandler(SESoundPlayerStateListener listener) {
            listeners.add(listener);
        }

        void removeHandler(SESoundPlayerStateListener listener) {
            listeners.remove(listener);
        }

        private void sendMsgStarted() {
            for (SESoundPlayerStateListener listener : listeners) {
                if (listener != null) {
                    listener.onPlayingStarted();
                }
            }
        }

        private void sendMsgInProgress() {
            for (SESoundPlayerStateListener listener : listeners) {
                if (listener != null) {
                    listener.onPlayingInProgress();
                }
            }
        }

        private void sendMsgPaused() {
            for (SESoundPlayerStateListener listener : listeners) {
                if (listener != null) {
                    listener.onPlayingPaused();
                }
            }
        }

        private void sendMsgError() {
            for (SESoundPlayerStateListener listener : listeners) {
                if (listener != null) {
                    listener.onPlayingError();
                }
            }
        }

        private class PlayingThread extends Thread {
            private boolean running;

            private AudioTrack audioTrack;

            private PlayingThread(boolean running) {
                this.running = running;
            }

            @Override
            public void run() {
                open();
                work();
                close();
            }

            private void open() {
                int minBufferSize = MIN_BUFFER_SIZE;

                audioTrack = new AudioTrack(STREAM_TYPE, SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_OUT, AUDIO_FORMAT, minBufferSize, MODE);
                audioTrack.setPositionNotificationPeriod((int)(SAMPLE_RATE_IN_HZ * 0.1));   // we'll notify each 0.1 seconds
                audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
//                    int count = 0;

                    @Override
                    public void onMarkerReached(AudioTrack track) {
                    }

                    @Override
                    public void onPeriodicNotification(AudioTrack track) {
//                LoggerFactory.obtainLogger(TAG).
//                                w("onPeriodicNotification# " + (++count) + " : " + track.getPlaybackHeadPosition());
                        sendMsgInProgress();
                    }
                });

                if (audioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                    sendMsgError();
                    return;
                }

                audioTrack.play();

                sendMsgStarted();
            }

            private void work() {
                int minBufferSize = MIN_BUFFER_SIZE * MULT;

                InputStream in = null;

                try {
                    int available = getAssets().open("1398328501793").available();
                    LoggerFactory.obtainLogger(TAG).w("available# " +
                            available);
                    LoggerFactory.obtainLogger(TAG).w("available in seconds# " + DurationUtils.secondsFromBytes(available));

                    in = getAssets().open("1398328501793");

                    byte data[] = new byte[minBufferSize];

                    int len = 0;
                    while (running && ((len = in.read(data)) != -1)) {
                        audioTrack.write(data, 0, len);
                    }

                    sendMsgPaused();
                } catch (Exception e) {
                    LoggerFactory.obtainLogger(TAG).
                            d("work# catch");
                    LoggerFactory.obtainLogger(TAG).
                            e(e.getMessage(), e);

                    sendMsgError();
                } finally {
                    LoggerFactory.obtainLogger(TAG).
                            d("work# finally");
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e) {
                            LoggerFactory.obtainLogger(TAG).
                                    e(e.getMessage(), e);
                        }
                    }
                }
            }

            private void close() {
                audioTrack.stop();
                audioTrack.release();
            }

            void pausePlaying() {
                running = false;
            }
        }
    }

//    private class TestSoundRcorder {
//        private RecordingThread thread;
//
//        private class RecordingThread extends Thread {
//            private boolean running;
//
//            private AudioRecord audioRecord;
//
//            private RecordingThread(boolean running) {
//                this.running = running;
//            }
//
//            @Override
//            public void run() {
//                open();
//                work();
//                close();
//            }
//
//            private void open() {
//                int minBufferSize = MIN_BUFFER_SIZE;
//
//                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
//                        SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG_IN, AUDIO_FORMAT, minBufferSize);
//                audioRecord.setPositionNotificationPeriod((int) (SAMPLE_RATE_IN_HZ * 0.1)); // notify each 0.1 second
//                audioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
//                    @Override
//                    public void onMarkerReached(AudioRecord recorder) {
//                    }
//
//                    @Override
//                    public void onPeriodicNotification(AudioRecord recorder) {
//                        long delta = DurationUtils.secondsToBytes(0.1);
//
//                        stream.updatePosition(delta);
//                        stream.updateDuration(delta);
//
//                        position += delta;
//                        duration += delta;
//
//                        sendMsgInProgress();
//
//                        LoggerFactory.obtainLogger(TAG).
//                                d("onPeriodicNotification# position = " + position +
//                                        ", duration = " + duration);
//                    }
//                });
//
//                if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
//                    sendMsgError();
//                    return;
//                }
//
//                stream.open(AudioStream.Mode.WRITE);
//                sendMsgStarted();
//
//                audioRecord.startRecording();
//            }
//
//            private void work() {
////            int minBufferSize = MIN_BUFFER_SIZE * MULT;
//                int minBufferSize = MIN_BUFFER_SIZE;
//
//                OutputStream out = null;
//
//                try {
//                    out = stream.getOutputStream();
//
//                    byte[] data = new byte[minBufferSize];
//                    while(running) {
//                        audioRecord.read(data, 0, data.length);
//                        out.write(data);
//                    }
//
//                    sendMsgStopped();
//                } catch (Exception e) {
//                    LoggerFactory.obtainLogger(TAG).
//                            e(e.getMessage(), e);
//
//                    sendMsgError();
//                } finally {
//                    stream.finalizePosition();
//                    stream.finalizeDuration();
//
//                    if (out != null) {
//                        try {
//                            out.flush();
//                            out.close();
//                        } catch (Exception e) {
//                            LoggerFactory.obtainLogger(TAG).
//                                    e(e.getMessage(), e);
//                        }
//                    }
//                }
//            }
//
//            private void close() {
//                stream.close();
//
//                audioRecord.stop();
//                audioRecord.release();
//            }
//
//
//            private void stopRecording() {
//                running = false;
//            }
//        }
//    }

    interface SESoundPlayerStateListener {
        void onPlayingStarted();

        void onPlayingInProgress();

        void onPlayingPaused();

        void onPlayingError();
    }
}
