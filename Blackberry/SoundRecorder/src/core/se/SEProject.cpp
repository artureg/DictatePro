/*
 * SEProject.cpp
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#include "SEProject.h"

SEProject::SEProject():
		changed(false) {}

SEProject::~SEProject() {}

char SEProject::getProjectPath() {
	return projectPath;
}

//SEAudioStream SEProject::getAudioStream() {
//	SEProjectAudioStream stream;
//	//stream.initWithRecord(this);
//
//	return stream;
//}

QList<SERecord> SEProject::getRecords() {
	return records;
}

void SEProject::addRecord(SERecord &record) {
	records.append(record);
}

//void SEProject::moveRecord(SERecord record, int index) {
//
//}
//
//void SEProject::removeRecord(SERecord record) {
//	records.removeOne(record);
//}
//
//void SEProject::removeAllRecords() {
//	records.clear();
//}
//
//bool SEProject::isChanged() {
//	return changed;
//}
//
//bool SEProject::save() {
//
//	return false;
//}
//
//bool SEProject::saveAsync() {
//
//	return false;
//}
