/*
 * SEProject.h
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef SEPROJECT_H_
#define SEPROJECT_H_

#include <qobject.h>
#include "SERecord.h"
#include "SEAudioStream.h"
//#include "SEProjectAudioStream.h"
#include <QList>

//using namespace bb::cascades;

/**
 * This class provides entry point to record’s CRUD operations, i.e.
 * addition of a sub-record to the existing record
 * deletion of a sub-record from the existing record
 * deletion of the whole record
 * splitting of an existing record into two sub-records according to the current position
 * movement of sub-records amoun each other in scope of the existing record
 * saving the record to sd-card (creation of a file)
 *
 */
class SEProject {

public:
	SEProject();
	virtual ~SEProject();

	/**
	 * @return path to the project
	 */
	char getProjectPath();

	/**
	 * Saves project, i.e. includes file contents of all records into a single file.
	 * @return true if saved successfully, false otherwise
	 */
//	bool save();

	/**
	 * Saves project async, i.e. includes file contents of all records into a single file.
	 *
	 * @return true if saved successfully, false otherwise
	 */
//	bool saveAsync();
    char projectPath;
    bool changed;
    QList<SERecord> records;

    /**
     * Method to build (internally) and provide project audio stream.
     * @return project audio stream
     */
    //SEAudioStream getAudioStream();

	QList<SERecord> getRecords();

	void addRecord(SERecord &record);

//	void moveRecord(SERecord record, int index);
//
//	void removeRecord(SERecord record);
//
//	void removeAllRecords();
//
//    bool isChanged();

};

#endif /* SEPROJECT_H_ */
