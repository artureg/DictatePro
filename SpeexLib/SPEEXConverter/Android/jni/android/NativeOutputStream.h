/*
 * OutputStream.h
 *
 *  Created on: 21 apr. 2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef NATIVEOUTPUTSTREAM_H_
#define NATIVEOUTPUTSTREAM_H_

class WaveFile;
class WaveSpeexFile;

class NativeOutputStream {

private:
//	const char file_path;
	int format;
	int sample_rate;
	int bits_per_sample;
	int channel;
	WaveFile* wavFile;
	WaveSpeexFile* speexFile;

	char *bufferStorage;
	int bufferStorageLength;

public:
	NativeOutputStream();
	virtual ~NativeOutputStream();

	int open(const char *file_path, int format, int sample_rate, int bits_per_sample, int channel);
	int write(char *data, int length);
	int close();
};

#endif /* OUTPUTSTREAM_H_ */
