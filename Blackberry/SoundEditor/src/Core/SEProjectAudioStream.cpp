#include "SEProjectAudioStream.h"
#include "SEProject.h"
#include "Internal/SERecord.h"

SEProjectAudioStream::SEProjectAudioStream(SEProject* project, QObject *parent) :
        SEAudioStream("", parent) {
    this->project = project;
}

long SEProjectAudioStream::getDuration() {
	long duration = 0;
	for (int i = 0; i < project->getRecords().count(); i++) {
		duration += project->getRecords()[i]->getRange().duration;
	}
	return duration;
}

bool SEProjectAudioStream::open(TSEAudioStreamMode mode) {
	this->mode = mode;
	return true;
}

void SEProjectAudioStream::close() {
	this->mode = kSEAudioStreamModeNone;
}

bool SEProjectAudioStream::readData(QByteArray &byteArray, long position, long duration) {
	long pos = 0;
	long eDuration = duration;
	for (int i = 0; i < project->getRecords().count(); i++) {
		SERecord* record = project->getRecords()[i];
		if (record->getStream()->getMode() != kSEAudioStreamModeRead) {
			record->getStream()->close();
			record->getStream()->open(kSEAudioStreamModeRead);
	    }
	    if (eDuration <= 0) {
	    	break;
	    }
	    long recordDuration = record->getStream()->getDuration();
	    if (pos + recordDuration > position) {
	    	long lPos = position - pos;
	    	long lDuration = (eDuration > recordDuration)?recordDuration:eDuration;
	    	record->getStream()->readData(byteArray, lPos, lDuration);
	        eDuration -= lDuration;
	    }
	    pos += record->getStream()->getDuration();
	}
	return true;
}


