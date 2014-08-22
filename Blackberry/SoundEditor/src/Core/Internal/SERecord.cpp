#include "SERecord.h"
#include "src/Core/SEProject.h"
#include "SERecordAudioStream.h"
#include <QDir>

SERecordRange SERecordRangeMake(long position, long duration) {
    SERecordRange range;
    range.position = position;
    range.duration = duration;
    return range;
}

SERecord::SERecord(QString soundFile, SERecordRange range, QObject *parent) :
    QObject(parent) {
	p_soundFile = soundFile;
	stream = NULL;
	p_range = range;
}

SERecord::SERecord(SEProject* project, QObject *parent) :
    QObject(parent) {
    p_soundFile = project->getProjectSoundPath() + "/" + QString::number(random()%9999999) + ".wav";
    stream = NULL;
}

SERecordAudioStream* SERecord::getStream() {
	if (!stream) {
		stream = new SERecordAudioStream(this, this);
	}
	return stream;
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

void SERecord::clear() {
	this->stream->close();
}
