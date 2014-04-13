package com.wiseapps.davacon.core.soundeditor;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 *	This class provides entry point to records CRUD operations, i.e.:
 *	<ul>
 *	<li>addition of a sub-record to the existing record; </li>
 *	<li>deletion of a sub-record from the existing record; </li>
 *	<li>deletion of the whole record; </li>
 *	<li>splitting of an existing record into two sub-records according to the current position; </li>
 *	<li>movement of sub-records amoun each other in scope of the existing record; </li>
 *	<li>saving the record to sd-card (creation of a file). </li>
 *	</ul>
 */
public class SEProject {

    private final Context context;
    private String projectFilename;

    // TODO add fields from .plist file
    private boolean isChanged;

    private List<SERecord> records;

    private SEAudioStream audioStream;

    public SEProject(Context context) {
        this.context = context;
    }

    public SEProject(Context context, String projectFilename) {
        this(context);

        this.projectFilename = projectFilename;
        this.records = SDCardUtils.getRecordsFromSDCard(context, projectFilename);

        audioStream = new SEProjectAudioStream(records);
    }

    public SEAudioStream getAudioStream() {
        // TODO rebuild
        return audioStream;
    }

	/**
	 * Split record in time position
	 * @param position
	 * @return SERecord object
	 */
	public SERecord splitRecordInPosition( long position ) {

		// TODO Add implementation!
		return null;
	}

	/**
	 * Delete record from project
	 * @param record
	 */
	public void deleteRecord( SERecord record ) {

		// TODO Add implementation!

	}

	/**
	 * Change records order
	 * @param record
	 * @param index
	 */
	public void moveRecord( SERecord record, long index ) {

		// TODO Add implementation!

	}

	/**
	 * Remove all records from project including all
	 * sound that are saved to the project sound folder
	 */
	public void removeAllRecords() {

		// TODO Add implementation!

	}

    /**
     * Add record to project
     * @param record
     */
    public void addRecord(SERecord record) {
        records.add(record);

        // TODO update SEProjectAudioStream
    }

    /**
	 * Save project
	 */
	public void saveProject() {

		// TODO Add implementation!

	}

	/**
	 * Save project in asynchronously (in another thread)
	 */
	public void saveProjectAsynchronouslyWithCompletion() {

		// TODO Add implementation!

	}

    public boolean isChanged() {
        return isChanged;
    }

    public List<SERecord> getRecords() {
        return records;
    }

    Context getContext() {
        return context;
    }
}
