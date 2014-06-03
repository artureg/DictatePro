/*
 * SEAudioStream.h
 *
 *  Created on: 16.04.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef SEAUDIOSTREAM_H_
#define SEAUDIOSTREAM_H_

#include <QObject>
#include <bb/cascades/Application>

/**
 * Representation of a stream
 */
class SEAudioStream  { //  public QObject

//	Q_OBJECT

public:

typedef enum {
	modeRead,
	modeWrite,
	modeUnknown
} SEAudioStreamMode;

typedef enum {
    formatWav,
    formatSpeex,
    formatUnknown
} SEAudioFormat;


protected:
    unsigned int currentPositionMills;
    unsigned int startPositionInMiils;
    unsigned int durationInMills;
    char *pathFile;

public:

    SEAudioStreamMode mode;

    SEAudioFormat format;

	SEAudioStream();
	virtual ~SEAudioStream();

	/**
	 * Opens the stream
	 */
    virtual bool open(SEAudioStreamMode mode);

	/**
	 * Closes the stream
	 */
	virtual void close();

	/**
	 * Clears the stream
	 */
	virtual bool clear();

	/**
	 * Writes the data to the end of the stream
	 *
	 * @param data data to be appended
	 */
	virtual bool write(char *data);

	/**
	 * Reads data from the stream
	 *
	 * @param data data to be read
	 * @param position position to start reading from
	 * @param duration duration of the data to be read
	 */
	virtual unsigned int read(char *data, unsigned int position, unsigned int duration);

};

#endif /* SEAUDIOSTREAM_H_ */
