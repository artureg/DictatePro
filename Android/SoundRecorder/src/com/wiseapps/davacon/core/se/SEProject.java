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

    final Context context;

    String projectPath;
    boolean isChanged;

    private List<SERecord> records = new ArrayList<SERecord>();

    private double duration;
    private double position;

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
        if (isChanged) {
            SDCardUtils.readProject(this);
            isChanged = false;
        }

        return new SEProjectAudioStream(context, this);
    }

    List<SERecord> getRecords() {
        return records;
    }

    void addRecord(SERecord record) {
        // here we insert to some mid position
        if (position != 0) {
            InputStream in = null;
            OutputStream out = null;

            SERecord cur = getCurrentRecord();

            int i = records.indexOf(cur);
            SERecord prev = i > 0 ? records.get(i - 1) : null;
            SERecord next = i < (records.size() - 1) ? records.get(i + 1) : null;

//                int format = SpeexWrapper.getFormat(cur.soundPath);
            int format = 0;

            SERecord a = new SERecord(this);
            a.soundPath = SDCardUtils.getSoundPath(context);
            a.duration = cur.position;
            a.prevRecord = prev;
            a.nextRecord = cur;
            SpeexWrapper.write(a.soundPath,
                    SpeexWrapper.read(cur.soundPath, 0, a.duration, format), format);

            SERecord b = new SERecord(this);
            b.soundPath = SDCardUtils.getSoundPath(context);
            b.duration = cur.duration - (cur.position + 1);
            b.prevRecord = cur;
            b.nextRecord = next;
            SpeexWrapper.write(b.soundPath,
                    SpeexWrapper.read(cur.soundPath, cur.position, b.duration, format), format);

//            SpeexWrapper.write(a = {0, cur.position});
//            SpeexWrapper.write(b = {cur.position + 1, cur.duration});

            // TODO delete cur from sd card

            // update project records
            List<SERecord> records = this.records;

            removeAllRecords();

            for (SERecord r : records) {
                records.add(r);

                if (records.indexOf(r) == i) {
                    records.add(a);
                    records.add(record);
                    records.add(b);
                }
            }
            this.records = records;
        } else { // here we insert to the end
            records.add(record);

            // set reference to the next record
            if (records.size() > 1) {
                records.get(records.size() - 1).nextRecord = record;
            }

            // set reference to the prev record
            if (records.size() > 2) {
                records.get(records.size() - 1).prevRecord = records.get(records.size() - 2);
            }
        }
        duration += record.duration;

        isChanged = true;
    }

    void moveRecord(SERecord record, int index) {
        // TODO implement

        isChanged = true;
    }

    void removeAllRecords() {
    	records.clear();
        duration = 0;

        isChanged = true;
    }

    void removeRecord(SERecord record) {
        records.remove(record);
        duration -= record.duration;

        isChanged = true;
    }

    public boolean isChanged() {
        return isChanged;
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

    double getDuration() {
        return duration;
    }
    public void setDuration(double duration) {
        this.duration = duration;
    }

    double getPosition() {
        return position;
    }
    void setPosition(double position) {
        this.position = position;
    }

    SERecord getCurrentRecord() {
        double duration = 0;
        for (SERecord record : records) {
            duration += record.duration;

            if (duration >= position) {
                record.position = duration - position;

                return record;
            }
        }

        return null;
    }
}
