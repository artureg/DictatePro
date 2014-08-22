#ifndef SERECORD_H
#define SERECORD_H

#include <QObject>
#include "SERecordAudioStream.h"

class SEProject;

struct SERecordRange {
	long position;
	long duration;
};

SERecordRange SERecordRangeMake(long position, long duration);

class SERecord: public QObject {
	Q_OBJECT
public:
    explicit SERecord(QString soundFile, SERecordRange range, QObject *parent = 0);
    explicit SERecord(SEProject* project, QObject *parent = 0);

	void setSoundFile(QString soundFile);
	QString getSoundFile();

	void setRange(SERecordRange range);
	SERecordRange getRange();

	void clear();

	SERecordAudioStream* getStream();

private:
	QString p_soundFile;
	SERecordRange p_range;
	SERecordAudioStream* stream;
};

#endif // SERECORD_H
