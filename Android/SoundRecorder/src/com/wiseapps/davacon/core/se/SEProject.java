package com.wiseapps.davacon.core.se;

import android.content.Context;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.speex.SpeexWrapper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 11:55 AM
 *
 * Set of public methods could not be changed!!!
 */
public class SEProject {
    private final static String TAG = SEProject.class.getSimpleName();

//    private static final int DURATION_SECONDS = 1;

    final Context context;

    String projectPath;

    private List<SERecord> records = new ArrayList<SERecord>();

    // project duration in bytes
    long duration;

    // project current position in bytes
    long position;

    public SEProject(Context context) {
        this.context = context;

        this.position = 0;
        this.duration = 0;
    }

    /**
     * Method to build (internally) and provide project audio stream.
     *
     * @return project audio stream
     */
//    SEAudioStream getAudioStream(Context context) {
//        duration = 0;
//
//        // each time audio stream is created the project is read from sdcard anew
//        SDCardUtils.readProject(this);
//
//        return new SEProjectAudioStream(context, this);
//    }
    AudioStream getAudioStream() {
//        removeAllRecords();
//
//        // each time audio stream is created the project is read from sdcard anew
//        SDCardUtils.readProject(this);

        return new ProjectAudioStream(this);
    }

    List<SERecord> getRecords() {
        return records;
    }

    void addRecord(SERecord record) {
        records.add(record);
        int index = records.indexOf(record);

        // set references to neighbour records
        if (index >= 1) {
            records.get(index - 1).nextRecord = record;
            record.prevRecord = records.get(index - 1);
        }

        duration += record.duration;
    }

    // for now new record is inserted after the current one
    void splitRecord(SERecord record) {
        if (records.size() == 0) {
            addRecord(record);
            return;
        }

        SERecord current = getCurrentRecord();
        int index = getCurrentRecordIndex();

        records.add(index + 1, record);

        // update references to neighbour records for current
        current.nextRecord = record;

        // update references to neighbour records for a newly inserted one
        record.prevRecord = current;
        if (index + 2 < records.size()) {
            record.nextRecord = records.get(index + 2);
        }

        duration += record.duration;
    }

//    void splitRecord(SERecord record) {
//        final SERecord cur = getCurrentRecord();
//
//        int i = records.indexOf(cur);
//
//        SERecord next = i < (records.size() - 1) ? records.get(i + 1) : null;
//
//
//
//        // define project current record and its position
//        final SERecord cur = getCurrentRecord();
//
//        // define the neighbour records
//        int i = records.indexOf(cur);
//        SERecord prev = i > 0 ? records.get(i - 1) : null;
//        SERecord next = i < (records.size() - 1) ? records.get(i + 1) : null;
//
//        // perform the split itself
//        SERecord aRecord = new SERecord(this);
//        aRecord.soundPath = SDCardUtils.getSoundPath(context);
//        aRecord.start = 0;
//        aRecord.duration = cur.position;
//        aRecord.prevRecord = prev;
//        aRecord.nextRecord = record;
//
//        SERecord bRecord = new SERecord(this);
//        bRecord.soundPath = SDCardUtils.getSoundPath(context);
//        bRecord.start = cur.position;
//        bRecord.duration = cur.duration - cur.position;
//        bRecord.prevRecord = record;
//        bRecord.nextRecord = next;
//
//        // update the project with new records list
//        List<SERecord> records = this.records;
//        removeAllRecords();
//        for (SERecord r : records) {
//            addRecord(r);
//
//            if (records.indexOf(r) == i) {
//                addRecord(aRecord);
//                addRecord(record);
//                addRecord(bRecord);
//            }
//        }
//    }

    void moveRecord(SERecord record, int index) {
        // TODO implement
    }

    void removeAllRecords() {
    	records.clear();

        duration = 0;
    }

    void removeRecord(SERecord record) {
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
//        SEProject project = new SEProject(context);
//        project.projectPath = projectPath;
//
//        SDCardUtils.readProject(project);
//
//        boolean result = false;
//
//        project = null;
//
//    	return result;

        // TODO implement
        return false;
    }

    /**
     * Saves project async, i.e. includes file contents of all records into a single file.
     *
     * TODO add listener
     *
     * @return true if saved successfully, false otherwise
     */
    public boolean saveAsync() {
        // TODO implement, use SDCardUtils.writeProject(this); and thread
        return false;
    }

    SERecord getCurrentRecord() {
        double duration = 0;
        for (SERecord record : records) {
            duration += record.duration;

            if (duration >= position) {
//                record.setPosition(record.getDuration() - record.start - (duration - position));
                return record;
            }
        }

        return null;
    }

    int getCurrentRecordIndex() {
        double duration = 0;
        for (int i = 0; i < records.size(); i++) {
            duration += records.get(i).duration;

            if (duration >= position) {
                return i;
            }
        }

        return 0;
    }
}
