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
#include <QList>

namespace bb {
namespace cascades {

class SEProject: public QObject {

Q_OBJECT

public:
	SEProject();
	virtual ~SEProject();

	/**
	 * @return path to the project
	 */
	QString getProjectPath();

	/**
	 * Saves project, i.e. includes file contents of all records into a single file.
	 * @return true if saved successfully, false otherwise
	 */
	bool save();

	/**
	 * Saves project async, i.e. includes file contents of all records into a single file.
	 * TODO add listener
	 *
	 * @return true if saved successfully, false otherwise
	 */
	bool saveAsync();

protected:

    QString projectPath;
    bool changed;
    QList<SERecord> records;

    /**
     * Method to build (internally) and provide project audio stream.
     * @return project audio stream
     */
    SEAudioStream getAudioStream();

	QList<SERecord> getRecords();

	void addRecord(SERecord record);

	void moveRecord(SERecord record, int index);

	void removeRecord(SERecord record);

	void removeAllRecords();

    bool isChanged();

};

} /* namespace cascades */
} /* namespace bb */
#endif /* SEPROJECT_H_ */
