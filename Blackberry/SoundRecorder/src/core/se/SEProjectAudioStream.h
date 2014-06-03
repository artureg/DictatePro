/*
 * SEProjectAudioStream.h
 *
 *  Created on: 20.05.2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef SEPROJECTAUDIOSTREAM_H_
#define SEPROJECTAUDIOSTREAM_H_

#include "SEAudioStream.h"
#include "SEProject.h"

class SEProjectAudioStream: public SEAudioStream {

private:
	SEProject *project;

public:

	SEProjectAudioStream();
	virtual ~SEProjectAudioStream();

	void initWithProject(SEProject &_project);

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
	bool write(char *data);

	/**
	 * Reads data from the stream
	 *
	 * @param data data to be read
	 * @param position position to start reading from
	 * @param duration duration of the data to be read
	 */
	unsigned int read(char *data, unsigned int position, unsigned int duration);

};

#endif /* SEPROJECTAUDIOSTREAM_H_ */
