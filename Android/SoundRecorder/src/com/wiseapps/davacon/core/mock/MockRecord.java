package com.wiseapps.davacon.core.mock;

import android.content.Context;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/18/14
 *         Time: 5:03 PM
 */
public class MockRecord {
    double start;
    String soundPath;

    final MockProject project;

    MockRecord prevRecord, nextRecord;

    // record current position
    double position;

    // record duration
    double duration;

    byte[] mockData = new byte[0];

    MockRecord(MockProject project) {
        this.project = project;
    }

    MockAudioStream getAudioStream(Context context) {
        return new MockRecordAudioStream(this, context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MockRecord record = (MockRecord) o;

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
