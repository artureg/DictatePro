#include "SERecordAudioStream.h"
#include "SERecord.h"

SERecordAudioStream::SERecordAudioStream(SERecord* record, QObject *parent) :
    SEAudioStream(record->getSoundFile(), parent) {
    this->record = record;
}

SERecord* SERecordAudioStream::getRecord() {
    return record;
}

long SERecordAudioStream::getDuration() {
    if (record->getRange().duration == 0) {
    	return SEAudioStream::getDuration();
    } else {
    	return record->getRange().duration;
    }
}

bool SERecordAudioStream::readData(QByteArray &byteArray, long position, long duration) {
    return SEAudioStream::readData(byteArray, record->getRange().position + position, duration);
}
