package com.wiseapps.davacon.core.soundeditor;

import java.util.ArrayList;

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
	
	/** Project name */
	private String name;
	
	/** Project file path */
	private String projectFilePath;
	
	/** Project sounds path */
	private String projectSoundsPath;
	
	/** List of records related to this project */
	private ArrayList<SERecord> records;

	/** Project audio preview stream */
	private SEAudioStreamPlayer audioStream;
	
	/** Check project if it is change (add or remove record affects that) */
	private boolean isChanged;
	
	
	public SEProject() {
		// TODO Auto-generated constructor stub
	}
	
	
	
	public ArrayList<SERecord> getRecords() {
		return records;
	}



	public void setRecords(ArrayList<SERecord> records) {
		this.records = records;
	}



	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProjectFilePath() {
		return projectFilePath;
	}

	public void setProjectFilePath(String projectFilePath) {
		this.projectFilePath = projectFilePath;
	}

	public String getProjectSoundsPath() {
		return projectSoundsPath;
	}

	protected boolean isChanged() {
		return isChanged;
	}
	
	public void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
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
	 * Add record to project
	 * @param record
	 */
	public void addRecord( SERecord record ) {

		// TODO Add implementation!
		
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

}
