package com.wiseapps.davacon.core;

import android.media.AudioFormat;

import java.io.File;
import java.io.Serializable;

/**
 * @author varya.bzhezinskaya@gmail.com
 *         Date: 3/24/14
 *         Time: 1:05 PM
 */
public class WAVFile implements Serializable {
    private static final String TAG = WAVFile.class.getSimpleName();

    private final String mChunkID = "RIFF";
    private int mChunkSize;
    private final String mFormat = "WAVE";
    private final String mSubchunk1ID = "fmt ";
    private int mSubchunk1Size;
    private short mAudioFormat;
    private short mNumChannels;
    private int mSampleRate;
    private int mByteRate;
    private short mBlockAlign;
    private short mBitsPerSample;
    private final String mSubchunk2ID = "data";
    private int mSubchunk2Size;
    private byte[] data;

    private final File file;

    private WAVFile(File file) {
        this.file = file;
    }

    public String getChunkID() {
        return mChunkID;
    }

    public int getChunkSize() {
        return mChunkSize;
    }
    public void setChunkSize(int mChunkSize) {
        this.mChunkSize = mChunkSize;
    }

    public String getFormat() {
        return mFormat;
    }

    public String getSubchunk1ID() {
        return mSubchunk1ID;
    }

    public int getSubchunk1Size() {
        return mSubchunk1Size;
    }

    public short getAudioFormat() {
        return mAudioFormat;
    }

    public short getNumChannels() {
        return mNumChannels;
    }

    public int getSampleRate() {
        return mSampleRate;
    }

    public int getByteRate() {
        return mByteRate;
    }

    public short getBlockAlign() {
        return mBlockAlign;
    }

    public short getBitsPerSample() {
        return mBitsPerSample;
    }

    public String getSubchunk2ID() {
        return mSubchunk2ID;
    }

    public int getSubchunk2Size() {
        return mSubchunk2Size;
    }

    public File getFile() {
        return file;
    }

    public static class Builder {
//        private int mChunkSize;
        private int mSubchunk1Size;
        private short mAudioFormat;
        private short mNumChannels;
        private int mSampleRate;
//        private int mByteRate;
//        private short mBlockAlign;
        private short mBitsPerSample;
//        private int mSubchunk2Size;

        private byte[] data;

        private final File file;

        public Builder(File file) {
            this.file = file;
        }

        public Builder setEncoding(int encoding) {
            if (encoding != AudioFormat.ENCODING_PCM_16BIT) {
                throw new IllegalArgumentException();
            }

            this.mSubchunk1Size = 16;
            this.mAudioFormat = 1;
            this.mBitsPerSample = (short) 16;

            return this;
        }

        public Builder setChannelType(int channelType) {
            this.mNumChannels = (short) (channelType == AudioFormat.CHANNEL_IN_STEREO ? 2 : 1);
            return this;
        }

        public Builder setSampleRate(int sampleRate) {
            this.mSampleRate = sampleRate;
            return this;
        }

        //        public Builder setChunkSize(int mChunkSize) {
//            this.mChunkSize = mChunkSize;
//            return this;
//        }
//
//        public Builder setAudioFormat(short mAudioFormat) {
//            this.mAudioFormat = mAudioFormat;
//            return this;
//        }
//
//        public Builder setNumChannels() {
//            return this;
//        }
//
//        public Builder setBitsPerSample(short mBitsPerSample) {
//            this.mBitsPerSample = mBitsPerSample;
//            return this;
//        }
//
//        public Builder setSubchunk2Size(int mSubchunk2Size) {
//            this.mSubchunk2Size = mSubchunk2Size;
//            return this;
//        }
//
//        public Builder setData(byte[] data) {
//            this.data = data;
//            return this;
//        }

        public WAVFile build() {
            WAVFile wav = new WAVFile(file);

//            wav.mChunkSize = mChunkSize;
            wav.mSubchunk1Size = mSubchunk1Size;
            wav.mAudioFormat = mAudioFormat;

            wav.mNumChannels = mNumChannels;
            wav.mSampleRate = mSampleRate;

            // == SampleRate * NumChannels * BitsPerSample/8
            wav.mByteRate = mSampleRate * mNumChannels * mBitsPerSample / 8;

            // == NumChannels * BitsPerSample/8
            wav.mBlockAlign = (short) (mNumChannels * mBitsPerSample / 8);

            wav.mBitsPerSample = mBitsPerSample;

            // == NumSamples * NumChannels * BitsPerSample/8
            wav.mSubchunk2Size = mSubchunk1Size;

            wav.data = data;

            return wav;
        }
    }
}
