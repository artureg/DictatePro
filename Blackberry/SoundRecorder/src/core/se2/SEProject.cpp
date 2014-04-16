/*
 * SEProject.cpp
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#include "SEProject.h"

namespace bb {
namespace cascades {

} /* namespace cascades */
} /* namespace bb */

bb::cascades::SEProject::SEProject():
		changed(false) {}

bb::cascades::SEProject::~SEProject() {}

QString bb::cascades::SEProject::getProjectPath() {
	return projectPath;
}

SEAudioStream bb::cascades::SEProject::getAudioStream() {
	// TODO implement
}

QList<SERecord> bb::cascades::SEProject::getRecords() {
	return records;
}

void bb::cascades::SEProject::addRecord(SERecord record) {
	records.append(record);
}

void bb::cascades::SEProject::moveRecord(SERecord record, int index) {
	// TODO implement
}

void bb::cascades::SEProject::removeRecord(SERecord record) {
	records.removeOne(record);
}

void bb::cascades::SEProject::removeAllRecords() {
	records.clear();
}

bool bb::cascades::SEProject::isChanged() {
	return changed;
}

bool bb::cascades::SEProject::save() {
	// TODO implement
	return false;
}

bool bb::cascades::SEProject::saveAsync() {
	// TODO implement, use SDCardUtils.writeProject(this); and thread
	return false;
}
