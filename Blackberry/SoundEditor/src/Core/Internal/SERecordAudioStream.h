#ifndef SERECORDAUDIOSTREAM_H
#define SERECORDAUDIOSTREAM_H

#include "src/Core/SEAudioStream.h"

class SERecord;

class SERecordAudioStream : public SEAudioStream {
    Q_OBJECT
public:
    explicit SERecordAudioStream(SERecord* record, QObject *parent = 0);

    SERecord* getRecord();

    long getDuration();

    bool readData(QByteArray& byteArray, long position, long duration);

private:
    SERecord* record;

signals:

public slots:

};

#endif // SERECORDAUDIOSTREAM_H
