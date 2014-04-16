package com.wiseapps.davacon.core.se;

import android.content.Context;

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

    List<SERecord> records = new ArrayList<SERecord>();

    boolean isChanged;

    public SEProject(Context context) {
        this.context = context;
    }

    /**
     * Method to build (internally) and provide project audio stream.
     *
     * @return project audio stream
     */
    SEAudioStream getAudioStream(Context context) {
        if (isChanged) {
            SDCardUtils.readProject(context);
        }

        return new SEProjectAudioStream(context, this).
                initialize(records);
    }

    List<SERecord> getRecords() {
        return records;
    }

    void addRecord(SERecord record) {
        records.add(record);
//        SDCardUtils.updateProjectToSDCard(context, this);
    }

    void moveRecord(SERecord record, int index) {
        // TODO implement
    }

    void removeRecord(SERecord record) {
       records.remove(record);
//       SDCardUtils.updateProjectToSDCard(context, this);
    }

    void removeAllRecords() {
    	records.clear();
//    	SDCardUtils.updateProjectToSDCard(context, this);
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
}
