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

}

QList<SERecord> bb::cascades::SEProject::getRecords() {
	return records;
}

void bb::cascades::SEProject::addRecord(SERecord record) {
	records.append(record);
}

void bb::cascades::SEProject::moveRecord(SERecord record, int index) {

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

	return false;
}

bool bb::cascades::SEProject::saveAsync() {

	return false;
}
