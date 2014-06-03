/*
 * SERecordAudioStream.h
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef SERECORDAUDIOSTREAM_H_
#define SERECORDAUDIOSTREAM_H_

#include "core/se/SEAudioStream.h"
#include "core/se/SERecord.h"
#include "core/SPEEXConverter/WaveSpeexFile.h"
class SERecord;
class SERecordAudioStream: public SEAudioStream {

private:
	WaveFile *waveFile;
    WaveSpeexFile *waveSpeexFile;
    const SERecord* record;
	unsigned int currenPositionInByte;

public:

	SERecordAudioStream();
	virtual ~SERecordAudioStream();

    void initWithRecord(const SERecord *record);

	/**
	 * Opens the stream
	 */
    bool open(SEAudioStreamMode mode);

	/**
	 * Closes the stream
	 */
	 void close();

	/**
	 * Clears the stream
	 */
	bool clear();

	/**
	 * Writes the data to the end of the stream
	 *
	 * @param data data to be appended
	 */
	bool write(char* data);

	/**
	 * Reads data from the stream
	 *
	 * @param data data to be read
	 * @param position position to start reading from
	 * @param duration duration of the data to be read
	 */
	unsigned int read(char* data, unsigned int position, unsigned int duration);

};

#endif /* SERECORDAUDIOSTREAM_H_ */
