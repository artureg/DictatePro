package com.wiseapps.davacon.core.se;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/18/14
 *         Time: 9:13 PM
 */
public class ProjectAudioStream extends AudioStream {

    private final SEProject project;

    ProjectAudioStream(final SEProject project) {
        this.project = project;
    }

    @Override
    void open(Mode mode) {
        if (project.getRecords() == null) {
            throw new IllegalStateException();
        }

        this.mode = mode;
    }

    @Override
    void close() {
    }

    @Override
    void clear() {
    }

    @Override
    InputStream getInputStream() throws Exception {
        int curIdx = project.getCurrentRecordIndex();

        List<InputStream> recordStreams = new ArrayList<InputStream>();
        for (int i = curIdx; i < project.getRecords().size(); i++) {
            recordStreams.add(project.getRecords().get(i).getAudioStream().getInputStream());
        }

        return new SequenceInputStream(Collections.enumeration(recordStreams));
    }

    @Override
    OutputStream getOutputStream() throws Exception {
        // ProjectAudioStream class is used for reading purposes only
        // This method is intentionally left blanc.
        throw new IllegalStateException();
    }

    @Override
    void updatePosition(long position) {
        project.position += position;

        int index = project.getCurrentRecordIndex();

        int i = 0;
        long duration = 0;
        for (SERecord record : project.getRecords()) {
            record.position = 0;

            duration += record.duration;

            if (i == index) {
                record.position = duration - project.position;
                if (record.position == record.duration) {
                    record.position = 0;
                }
            }

            ++i;
        }
    }

    @Override
    void updateDuration(long duration) {
    }

    @Override
    Mode getMode() {
        return mode;
    }
}
