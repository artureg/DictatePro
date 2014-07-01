#ifndef SEPROJECTAUDIOSTREAM_H
#define SEPROJECTAUDIOSTREAM_H

#include "SEAudioStream.h"

class SEProject;

class SEProjectAudioStream: public SEAudioStream {
	Q_OBJECT
public:
	explicit SEProjectAudioStream(SEProject* project, QObject *parent = 0);

	bool readData(QByteArray& byteArray, long position, long duration);

private:
	SEProject* project;
};

#endif // SEPROJECTAUDIOSTREAM_H
