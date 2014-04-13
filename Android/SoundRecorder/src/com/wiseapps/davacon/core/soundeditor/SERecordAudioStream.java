package com.wiseapps.davacon.core.soundeditor;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.wiseapps.davacon.SoundRecorder;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.wiseapps.davacon.SoundRecorder.*;

/**
 * Stream with record 
 */
public class SERecordAudioStream extends SEAudioStream {
    private static final String TAG = SERecordAudioStream.class.getSimpleName();

    private static final String DEFAULT_CHUNK_ID = "RIFF";
    private static final int DEFAULT_CHUNK_SIZE = 0;
    private static final String DEFAULT_FORMAT = "WAVE";

    private static final String DEFAULT_SUBCHUNK_1_ID = "fmt ";
    private static final int DEFAULT_SUBCHUNK_1_SIZE = 16;
    private static final short DEFAULT_AUDIO_FORMAT = 1;
    private static final short DEFAULT_NUM_CHANNELS = 1;    // AudioFormat.CHANNEL_IN_MONO
    private static final int DEFAULT_SAMPLE_RATE = 8000;
    private static final int DEFAULT_BYTE_RATE = 8000;
    private static final short DEFAULT_BLOCK_ALIGN = 1;
    private static final short DEFAULT_BITS_PER_SAMPLE = 16;    // Required bits = 8

    private static final String DEFAULT_SUBCHUNK_2_ID = "data";
    private static final int DEFAULT_SUBCHUNK_2_SIZE = 0;

    private static final int IDX_CHUNK_SIZE = 4;
    private static final int IDX_SUBCHUNK_2_SIZE = 40;
    private static final int IDX_DATA = 44;

    private final Context context;

    private SERecorderStateListener listener;

    private SoundRecorder recorder;

    private RandomAccessFile raf;

    private int totalNumberOfBytes;

	public SERecordAudioStream(Context context) {
        this.context = context;

        recorder = new SoundRecorder();
        recorder.addHandler(new SoundRecorderHandler());
	}

	/**
     * Start recording sound
     */
	public void startRecording() {
        recorder.start();
    }

    public boolean isRecording() {
        return recorder.isRecording();
    }

	/**
     * Stop recording sound
     */
	public void stopRecording() {
        recorder.stop();
    }

    public void setListener(SERecorderStateListener listener) {
        this.listener = listener;
    }

    private class SoundRecorderHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RECORDING_STARTED: {
                    LoggerFactory.obtainLogger(TAG).
                            d("SoundRecorderHandler.handleMessage# MSG_RECORDING_STARTED");

                    try {
                        raf = new RandomAccessFile(
                                new File(FileUtils.getFilename(context, System.currentTimeMillis() + ".wav")), "rw");
                        raf.setLength(0);

                        // Set RIFF-header section
                        raf.writeBytes(DEFAULT_CHUNK_ID);
                        raf.writeInt(Integer.reverseBytes(0));
                        raf.writeBytes(DEFAULT_FORMAT);

                        // Set fmt-subchunk
                        raf.writeBytes(DEFAULT_SUBCHUNK_1_ID);
                        raf.writeInt(Integer.reverseBytes(DEFAULT_SUBCHUNK_1_SIZE));
                        raf.writeShort(Short.reverseBytes(DEFAULT_AUDIO_FORMAT));
                        raf.writeShort(Short.reverseBytes(DEFAULT_NUM_CHANNELS));
                        raf.writeInt(Integer.reverseBytes(DEFAULT_SAMPLE_RATE));
                        raf.writeInt(Integer.reverseBytes(DEFAULT_BYTE_RATE));
                        raf.writeShort(Short.reverseBytes(DEFAULT_BLOCK_ALIGN));
                        raf.writeShort(Short.reverseBytes(DEFAULT_BITS_PER_SAMPLE));

                        // Set data-subchunk
                        raf.writeBytes(DEFAULT_SUBCHUNK_2_ID);
                        raf.writeInt(Integer.reverseBytes(0));

                        raf.seek(IDX_DATA);
                        totalNumberOfBytes = 0;
                    } catch (Exception e) {
                        LoggerFactory.obtainLogger(TAG).
                                e("startRecording# ", e);

                        if (listener != null) {
                            listener.errorOccured(e.getMessage());
                        }
                    }

                    if (listener != null) {
                        listener.recordingStarted();
                    }

                    break;
                }
                case MSG_DATA_RECORDED: {
                    LoggerFactory.obtainLogger(TAG).
                            d("SoundRecorderHandler.handleMessage# MSG_DATA_RECORDED");

                    try {
                        byte[] data = (byte[]) msg.obj;
                        raf.write(data);

                        totalNumberOfBytes += data.length;
                    } catch (IOException e) {
                        LoggerFactory.obtainLogger(TAG).
                                e("startRecording# ", e);

                        if (listener != null) {
                            listener.errorOccured(e.getMessage());
                        }

                        return;
                    }

                    if (listener != null) {
                        listener.dataRecorded(0);   // TODO implement duration
                    }

                    break;
                }
                case MSG_RECORDING_STOPPED: {
                    LoggerFactory.obtainLogger(TAG).
                            d("SoundRecorderHandler.handleMessage# MSG_RECORDING_STOPPED");

                    try {
                        raf.seek(IDX_CHUNK_SIZE); // Write size to RIFF header (ChunkSize)
                        raf.writeInt(Integer.reverseBytes(36 + totalNumberOfBytes));

                        raf.seek(IDX_SUBCHUNK_2_SIZE); // Write size to Subchunk2Size field
                        raf.writeInt(Integer.reverseBytes(totalNumberOfBytes));

                        raf.close();
                    } catch (Exception e) {
                        LoggerFactory.obtainLogger(TAG).
                                e("startRecording# ", e);

                        if (listener != null) {
                            listener.errorOccured(e.getMessage());
                        }
                    }

                    if (listener != null) {
                        listener.recordingStopped();
                    }

                    break;
                }
                case MSG_RECORDING_ERROR: {
                    LoggerFactory.obtainLogger(TAG).
                            d("SoundRecorderHandler.handleMessage# MSG_RECORDING_ERROR");

                    if (listener != null) {
                        listener.errorOccured("" + msg.obj);
                    }

                    break;
                }
            }

            super.handleMessage(msg);
        }
    }
}