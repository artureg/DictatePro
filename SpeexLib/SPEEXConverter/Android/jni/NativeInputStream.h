/*
 * InputStream.h
 *
 *  Created on: 21 apr. 2014 г.
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#ifndef NATIVEINPUTSTREAM_H_
#define NATIVEINPUTSTREAM_H_

class WaveFile;
class WaveSpeexFile;

class NativeInputStream {
private:
	int format;
	unsigned int curPositionInBytes;
	unsigned int curMilliSecondPosition; // it is used for speex
	WaveFile* wavFile;
	WaveSpeexFile* speexFile;
public:
	NativeInputStream();
	virtual ~NativeInputStream();

	int open(const char *file_path, int _format);
	int getSampleRate();
	int read(char *data, int length);
	int read(char *buffer, long offset, long duration);
	int skip(int bytes);
	int close();
};

#endif /* INPUTSTREAM_H_ */
