#ifndef SESPEEXAUDIOSTREAM_H
#define SESPEEXAUDIOSTREAM_H

#include "src/Core/SEAudioStream.h"

class WaveSpeexFile;

class SESpeexAudioStream : public SEAudioStream
{
    Q_OBJECT
public:
    explicit SESpeexAudioStream(QString path, QObject *parent = 0);

    long getDuration();

    bool open(TSEAudioStreamMode mode);
    void close();

    bool readData(QByteArray& byteArray, long position, long duration);
    bool writeData(QByteArray& byteArray);

private:
    WaveSpeexFile*  file;
    QByteArray      data;
    long            duration;
};

#endif // SESPEEXAUDIOSTREAM_H
