package com.wiseapps.davacon.core.mock;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/18/14
 *         Time: 5:03 PM
 */
public class MockProject {

    final Context context;

    String projectPath;

    private List<MockRecord> records = new ArrayList<MockRecord>();

    // project duration
    double duration;

    // project current position
    double position;

    public MockProject(Context context) {
        this.context = context;

        this.position = 0;
        this.duration = 0;
    }

    /**
     * Method to build (internally) and provide project audio stream.
     *
     * @return project audio stream
     */
    MockAudioStream getAudioStream(Context context) {
        removeAllRecords();

        // each time audio stream is created the project is read from sdcard anew
        MockSDCardUtils.readProject(this);

        return new MockProjectAudioStream(context, this);
    }

    List<MockRecord> getRecords() {
        return records;
    }

    void addRecord(MockRecord record) {
        records.add(record);
        int index = records.indexOf(record);

        // set references to neighbour records
        if (index >= 1) {
            records.get(index - 1).nextRecord = record;
            record.prevRecord = records.get(index - 1);
        }

        duration += record.duration;
    }

    void splitRecord(MockRecord record) {
        // define project current record and its position
        final MockRecord cur = getCurrentRecord();
        if (cur == null) {
            addRecord(record);
            return;
        }

        // define the neighbour records
        int i = records.indexOf(cur);
        MockRecord prev = i > 0 ? records.get(i - 1) : null;
        MockRecord next = i < (records.size() - 1) ? records.get(i + 1) : null;

        // perform the split itself
        MockRecord aRecord = new MockRecord(this);
        aRecord.soundPath = MockSDCardUtils.getSoundPath(context);
        aRecord.start = 0;
        aRecord.duration = cur.position;
        aRecord.prevRecord = prev;
        aRecord.nextRecord = record;

        MockRecord bRecord = new MockRecord(this);
        bRecord.soundPath = MockSDCardUtils.getSoundPath(context);
        bRecord.start = cur.position;
        bRecord.duration = cur.duration - cur.position;
        bRecord.prevRecord = record;
        bRecord.nextRecord = next;

        // update the project with new records list
        // TODO start here
        List<MockRecord> records = this.records;
        removeAllRecords();
        for (MockRecord r : records) {
            addRecord(r);

            if (records.indexOf(r) == i) {
                addRecord(aRecord);
                addRecord(record);
                addRecord(bRecord);
            }
        }

        MockSDCardUtils.writeProject(this);
    }

    void moveRecord(MockRecord record, int index) {
        // TODO implement
    }

    void removeAllRecords() {
        records.clear();

        duration = 0;
    }

    void removeRecord(MockRecord record) {
        records.remove(record);

        duration -= record.duration;
    }

    public boolean isChanged() {
        return false;
    }

    /**
     *
     * @return path to the project
     */
    public String getProjectPath() {
        return projectPath;
    }

    /**
     * Saves project, i.e. includes file contents of all records into a single file.
     *
     * @return true if saved successfully, false otherwise
     */
    public boolean save() {
        MockProject project = new MockProject(context);
        project.projectPath = projectPath;

        MockSDCardUtils.readProject(project);

        boolean result = false;

        project = null;

        return result;
    }

    /**
     * Saves project async, i.e. includes file contents of all records into a single file.
     *
     * TODO add listener
     *
     * @return true if saved successfully, false otherwise
     */
    public boolean saveAsync() {
        // TODO implement, use MockSDCardUtils.writeProject(this); and thread
        return false;
    }

    MockRecord getCurrentRecord() {
        double duration = 0;
        for (MockRecord record : records) {
            duration += record.duration;

            if (duration >= position) {
//                record.setPosition(record.getDuration() - record.start - (duration - position));
                return record;
            }
        }

        return null;
    }
}
