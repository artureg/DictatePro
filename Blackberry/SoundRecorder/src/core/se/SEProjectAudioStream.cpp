/*
 * SEProjectAudioStream.cpp
 *
 *  Created on: 20.05.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#include "SEProjectAudioStream.h"

SEProjectAudioStream::SEProjectAudioStream() {}

SEProjectAudioStream::~SEProjectAudioStream() {}

void SEProjectAudioStream::initWithProject(SEProject &_project) {
	project = &_project;
}

bool SEProjectAudioStream::open(SEAudioStreamMode _mode) {
	mode = _mode;
	return false;
}

void SEProjectAudioStream::close() {}

bool SEProjectAudioStream::clear() {
	return false;
}

bool SEProjectAudioStream::write(char *data) {

	return false;
}

unsigned int SEProjectAudioStream::read(char *data, unsigned int position, unsigned int duration) {

    unsigned int pos = 0;
    unsigned int dur = duration;
     unsigned int size;
    QList<SERecord> records = project->getRecords();
    for (int i; i < records.size(); i++) {
        SERecord record = records[i];
        if (record.audioStream()->mode != modeRead) {
            record.audioStream()->clear();
            record.audioStream()->open(modeRead);
        }
        if (dur <= 0) {
            break;
        }
        unsigned int recordDuration = record.soundRange.duration;
        if (pos + recordDuration > position) {
            unsigned int lPos = position - pos;
            unsigned int lDuration = (dur > recordDuration) ? recordDuration:dur;
            size = record.audioStream()->read(data, lPos, lDuration);
            dur -= lDuration;
        }
        pos += record.audioStream.duration;
    }

    return size;
}
