package com.wiseapps.davacon.core.se;

import android.content.Context;
import com.wiseapps.davacon.logging.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 11:55 AM
 */
public class SEProject {
    private final static String TAG = SEProject.class.getSimpleName();

    final Context context;

    String projectPath;

    private List<SERecord> records = new ArrayList<SERecord>();

    // project duration in bytes
    public long duration;

    // project current position in bytes
    public long position;

    public SEProject(Context context) {
        this.context = context;

        this.position = 0;
        this.duration = 0;
    }

    AudioStream getAudioStream() {
        return new ProjectAudioStream(this);
    }

    public List<SERecord> getRecords() {
        return records;
    }

    void addRecord(SERecord record) {
        addRecord(records.size() == 0 ? 0 : records.size(), record);
    }

    void addRecord(int position, SERecord record) {
        records.add(position, record);
        int index = records.indexOf(record);

        // set references to neighbour records
        if (index >= 1) {
            records.get(index - 1).nextRecord = record;
            record.prevRecord = records.get(index - 1);
        }

        duration += record.duration;
    }

    void splitRecord(SERecord record) {
        if (position == 0) {
            addRecord(0, record);
            return;
        }

        // we are at the stream end
        if (/*records.size() == 0 || */position >= duration) {
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

    /**
     * Returns whether the project has been changed.
     *
     * @return true if project has been changed, false otherwise
     */
    public boolean isChanged() {
        return false;
    }

    /**
     * Returns path to the project.
     *
     * @return path to the project
     */
    public String getProjectPath() {
        return projectPath;
    }

    /**
     * Saves project, i.e. includes contents of all records into a single record.
     *
     * @return true if saved successfully, false otherwise
     */
    public boolean save() {
        return doSave();
    }

    /**
     * Saves project async, see {@link #save}.
     * An interested in an operation's status part should implement the
     * {@link com.wiseapps.davacon.core.se.SEProject.SaveProjectStatusListener} interface.
     *
     * @param listener an interested in a current operation's status part
     */
    public void saveAsync(final SaveProjectStatusListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = doSave();

                if (listener != null) {
                    if (success) {
                        listener.onOperationCompletedSuccessfully();
                    } else {
                        listener.onOperationFailed();
                    }
                }
            }
        }).start();
    }

    private boolean doSave() {
        position = 0;
        updateRecordPositions();

        AudioStream stream = getAudioStream();

        // creating just a fake record to have possibility to use its stream's capabilities
        SERecord record = new SERecord(this);
        record.soundPath = SDCardUtils.getSoundPath(context);

        InputStream in = null;
        OutputStream out = null;

        try {
            in = stream.getInputStream();
            out = record.getAudioStream().getOutputStream();

            long duration = 0;

            byte data[] = new byte[SESoundPlayer.getMinBufferSize()];
            int length;
            while ((length = in.read(data)) != -1) {
            	
                out.write(data, 0 , length);
                duration += length; // data.length;
            }

            if(duration == 0) return false;
            
            record.duration = duration;

            SDCardUtils.deleteRecords(this);

            removeAllRecords();
            addRecord(record);

            SDCardUtils.writeProject(this);

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

    SERecord getCurrentRecord() {
        double duration = 0;
        for (SERecord record : records) {
            duration += record.duration;

            if (duration >= position) {
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

    /**
     * Interface interested in a project save operation part should implement.
     */
    public interface SaveProjectStatusListener {
        void onOperationCompletedSuccessfully();
        void onOperationFailed();
    }
}
