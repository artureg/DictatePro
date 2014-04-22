package com.wiseapps.davacon.core.se;

import android.content.Context;
import com.wiseapps.davacon.logging.LoggerFactory;
import com.wiseapps.davacon.speex.SpeexWrapper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.wiseapps.davacon.core.se.SEProjectEngine.MIN_BUFFER_SIZE;

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
        if (records.size() == 0 || position == duration) {
            addRecord(record);
            return;
        }

        SERecord current = getCurrentRecord();
        int index = getCurrentRecordIndex();

        SERecord aRecord = new SERecord(this);
        aRecord.soundPath = current.soundPath;
        aRecord.start = current.start;
        aRecord.duration = current.position;
        aRecord.prevRecord = current.prevRecord;
        aRecord.nextRecord = record;

        SERecord bRecord = new SERecord(this);
        bRecord.soundPath = current.soundPath;
        bRecord.start = current.start + current.position;
        bRecord.duration = current.duration - current.position;
        bRecord.prevRecord = record;
        bRecord.nextRecord = current.nextRecord;

        record.prevRecord = aRecord;
        record.nextRecord = bRecord;

        records.set(index, aRecord);
        records.add(index + 1, record);
        records.add(index + 2, bRecord);
    }

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
        AudioStream stream = getAudioStream();

        // creating just a fake record to have possibility to use its stream's capabilities
        SERecord record = new SERecord(this);
//        record.soundPath = SDCardUtils.getPathToSave(context);
        record.soundPath = SDCardUtils.getSoundPath(context);

        InputStream in = null;
        OutputStream out = null;

        try {
            in = stream.getInputStream();
            out = record.getAudioStream().getOutputStream();

            long duration = 0;

            byte data[] = new byte[MIN_BUFFER_SIZE];
            while (in.read(data) != -1) {
                out.write(data);
                duration += data.length;
            }

            record.duration = duration;

            removeAllRecords();
            addRecord(record);

            return true;
        } catch (Exception e) {
            LoggerFactory.obtainLogger(TAG).
                    e(e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    LoggerFactory.obtainLogger(TAG).
                            e(e.getMessage(), e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    LoggerFactory.obtainLogger(TAG).
                            e(e.getMessage(), e);
                }
            }
        }

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

    void updateRecordPositions() {
        int index = getCurrentRecordIndex();

        int i = 0;
        long duration = 0;
        for (SERecord record : records) {
            record.position = 0;

            duration += record.duration;

            if (i == index) {
                record.position = record.duration - (duration - position);
            }

            ++i;
        }
    }
}
