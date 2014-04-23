package com.wiseapps.davacon.core.se;

import android.content.Context;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 11:55 AM
 */
public class SERecord {
    long start;
    String soundPath;

    final SEProject project;

    SERecord prevRecord, nextRecord;

    // record current position in bytes
    long position;

    // record duration in bytes
    long duration;

    SERecord(SEProject project) {
        this.project = project;
    }

//    SEAudioStream getAudioStream(Context context) {
//        return new SERecordAudioStream(this, context);
//    }

    AudioStream getAudioStream() {
        return new RecordAudioStream(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SERecord record = (SERecord) o;

        if (Double.compare(record.duration, duration) != 0) return false;
        if (Double.compare(record.start, start) != 0) return false;
        if (soundPath != null ? !soundPath.equals(record.soundPath) : record.soundPath != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(start);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(duration);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (soundPath != null ? soundPath.hashCode() : 0);
        return result;
    }
}
