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

    // project duration
    double duration;

    // project current position
    double position;

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
    SEAudioStream getAudioStream(Context context) {
        duration = 0;

        // each time audio stream is created the project is read from sdcard anew
        SDCardUtils.readProject(this);

        return new SEProjectAudioStream(context, this);
    }

    List<SERecord> getRecords() {
        return records;
    }

    void addRecord(SERecord record) {
        records.add(record);
        int index = records.indexOf(record);

        // set references to the records
        if (index >= 1) {
            records.get(index - 1).nextRecord = record;
            record.prevRecord = records.get(index - 1);
        }

        duration += record.duration;
    }

    void addRecord(SERecord record, double position) {
        // define project current record and its position
        final SERecord cur = getCurrentRecord();

        // define the neighbour records
        int i = records.indexOf(cur);
        SERecord prev = i > 0 ? records.get(i - 1) : null;
        SERecord next = i < (records.size() - 1) ? records.get(i + 1) : null;

        // perform the split itself
        final SERecord aRecord = new SERecord(this);
        aRecord.soundPath = SDCardUtils.getSoundPath(context);
        aRecord.position = 0;
        aRecord.duration = cur.position;
        aRecord.prevRecord = prev;
        aRecord.nextRecord = record;

        final SERecord bRecord = new SERecord(this);
        bRecord.soundPath = SDCardUtils.getSoundPath(context);
        bRecord.position = 0;
        bRecord.duration = cur.duration - cur.position;
        bRecord.prevRecord = record;
        bRecord.nextRecord = next;

        // update the project with new records list
        List<SERecord> records = this.records;
        removeAllRecords();
        for (SERecord r : records) {
            addRecord(r);

            if (records.indexOf(r) == i) {
                addRecord(aRecord);
                addRecord(record);
                addRecord(bRecord);
            }
        }

        // in a background thread 1) update the neighbour records
        // 2) delete cur sound file from sd card
        new Thread() {
            @Override
            public void run() {
                int format = SpeexWrapper.getFormat(cur.soundPath);
                SpeexWrapper.write(aRecord.soundPath,
                        SpeexWrapper.read(cur.soundPath, 0, aRecord.duration, format), format);
                SpeexWrapper.write(bRecord.soundPath,
                        SpeexWrapper.read(cur.soundPath, aRecord.duration, cur.duration, format), format);

                File file = new File(cur.soundPath);
                if (file.exists()) {
                    file.delete();
                }
            }
        }.start();
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
    	return SDCardUtils.writeProject(this);
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
                record.position = record.duration - (duration - position);
                return record;
            }
        }

        return null;
    }
}
