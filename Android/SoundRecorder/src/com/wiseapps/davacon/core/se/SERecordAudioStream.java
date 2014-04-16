package com.wiseapps.davacon.core.se;

import android.content.Context;
import com.wiseapps.davacon.speex.SpeexWrapper;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 11:56 AM
 *
 * TODO in ideal case we assume that we need just to call read/write method
 * TODO of the SpeexWRapper class = this means we don't care about header
 * TODO (assume it is written in the lib)
 */
class SERecordAudioStream extends SEAudioStream {

    private final SEProject project;

    SERecordAudioStream(final SEProject project, Context context) {
        super(context);

        this.project = project;
    }

    @Override
    public void open(Mode mode) {
        this.mode = mode;
    }

    @Override
    public void close() {
        // TODO update project with new records through SDCardUtils class
        // TODO now we have reference to project so everything should go ok
    }

    @Override
    public void clear() {
    }

    @Override
    public void write(byte[] data) {
        // TODO actual file path
        String filePath = "";

        int format = SpeexWrapper.getFormat(filePath);
        SpeexWrapper.write(filePath, data, format);     // TODO probably we need to handle the result somehow
    }

    @Override
    public byte[] read(double position, double duration) {
        // TODO actual file path
        String filePath = "";

        int format = SpeexWrapper.getFormat(filePath);
        return SpeexWrapper.read(filePath, position, duration, format);     // TODO probably we need to handle the result somehow
    }

    @Override
    Mode getMode() {
        return mode;
    }
}
