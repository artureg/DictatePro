package com.wiseapps.davacon.core.se;

import android.content.Context;

import java.util.List;

/**
 * @author varya.bzhezinskaya@wise-apps.com
 *         Date: 4/14/14
 *         Time: 11:55 AM
 *
 * Set of public methods could not be changed!!!
 */
public class SEProject {

    final String projectPath;
    boolean isChanged;

    public SEProject(String projectPath) {
        this.projectPath = projectPath;
    }

    /**
     * Method to build (internally) and provide project audio stream.
     *
     * @return project audio stream
     */
    SEAudioStream getAudioStream(Context context) {
        return new SEProjectAudioStream(this, context).
                initialize(SDCardUtils.getRecordsFromSDCard(context, this));
    }

    List<SERecord> getRecords() {
        // TODO implement
        return null;
    }

    void addRecord(SERecord record) {
        // TODO implement
    }

    void moveRecord(SERecord record, int index) {
        // TODO implement
    }

    void removeRecord(SERecord record) {
        // TODO implement
    }

    void removeAllRecords() {
        // TODO implement
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
        // TODO implement
        return false;
    }
}
