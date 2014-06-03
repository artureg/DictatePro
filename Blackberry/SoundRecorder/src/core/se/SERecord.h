/*
 * SERecord.h
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko (timothy.kovalenko@wise-apps.com)
 */

#ifndef SERECORD_H_
#define SERECORD_H_

//#include "core/se/SEAudioStream.h"
//#include "core/se/SERecordAudioStream.h"
#include "SERecordAudioStream.h"


class SERecordAudioStream;
/**
 * This class is a helper class that represents a sub-record
 */
class SERecord {

//Q_OBJECT

//Q_PROPERTY( int audioStream READ readAudioStream );

typedef struct
{
	unsigned int start; /** Sound start position in milliseconds */
	unsigned int duration; /** Sound duration from start position in milliseconds */
}
SERecordSoundRange;

public:
	SERecord();
	virtual ~SERecord();

	/** URL for source sound location */
	char* soundUrl;

	/** Range in sound for current record */
	SERecordSoundRange soundRange;

	/** Record audio stream */
    SERecordAudioStream*  audioStream();

private:
    SERecordAudioStream *m_audio_stream;

};

#endif /* SERECORD_H_ */
