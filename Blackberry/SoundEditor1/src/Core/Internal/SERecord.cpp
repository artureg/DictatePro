#include "SERecord.h"

SERecord::SERecord(QString soundFile, SERecordRange range, QObject *parent) :
		QObject(parent) {
	p_soundFile = soundFile;
	p_range = range;
}

void SERecord::setSoundFile(QString soundFile) {
	p_soundFile = soundFile;
}

QString SERecord::getSoundFile() {
	return p_soundFile;
}

void SERecord::setRange(SERecordRange range) {
	p_range = range;
}

SERecordRange SERecord::getRange() {
	return p_range;
}
