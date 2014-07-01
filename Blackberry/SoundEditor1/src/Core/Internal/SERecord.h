#ifndef SERECORD_H
#define SERECORD_H

#include <QObject>

struct SERecordRange {
	long position;
	long duration;
};

class SERecord: public QObject {
	Q_OBJECT
public:
	explicit SERecord(QString soundFile, SERecordRange range, QObject *parent =
			0);

	void setSoundFile(QString soundFile);
	QString getSoundFile();

	void setRange(SERecordRange range);
	SERecordRange getRange();

private:
	QString p_soundFile;
	SERecordRange p_range;
};

#endif // SERECORD_H
