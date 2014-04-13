package com.wiseapps.davacon.core.soundeditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Inherited from the SEAudioStream class concrete representation of an audio stream
 */
public class SEProjectAudioStream extends SEAudioStream {

    private List<SERecordAudioStream> streams;

	public SEProjectAudioStream(List<SERecord> records) {
        streams = new ArrayList<SERecordAudioStream>();

        for (SERecord record : records) {
            streams.add(record.getAudioStream());
        }
	}

    @Override
    protected void open() {
    }

    @Override
    protected void close() {
    }

    @Override
    protected void clear() {
        streams.clear();
    }

    @Override
    protected void seekToSamplePosition(int position) {
    }

    @Override
    protected void seekToSecond(int second) {
        // TODO implement
    }

    @Override
    protected byte[] readSamplesWithCount(int count) {
        throw new IllegalStateException();
    }

    @Override
    protected byte[] readSamplesFromChannel(int channels, int count) {
        throw new IllegalStateException();
    }

    @Override
    protected void readSamples(byte[] samples, int count) {
        // TODO implement
    }

    @Override
    protected void writeSamples(byte[] samples) {
    }

    @Override
    protected void writeSamples(byte[] data, int count) {
    }
}
