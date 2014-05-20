/*
 * OutputStream.cpp
 *
 *  Created on: 21 apr. 2014
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#include "NativeOutputStream.h"

#include <android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "-JNI-",__VA_ARGS__)

#include "SPEEXConverter/WaveSpeexFile.h"

NativeOutputStream::NativeOutputStream():bufferStorageLength(0) {

//	file_path = _filePath;
//	format = _format;
//	sample_rate = _sampleRate;
//	bits_per_sample = _bitsPerSample;
//	channel = _channel;

}

NativeOutputStream::~NativeOutputStream() {
	close();
}

int NativeOutputStream::open(const char *file_path, int _format, int _sample_rate, int _bits_per_sample, int _channel) {

	format = _format;
	sample_rate = _sample_rate;
	bits_per_sample = _bits_per_sample;
	channel = _channel;

	if(format == 0) {
		wavFile = new WaveFile();
		//wavFile = (WaveFile*)malloc(sizeof(wavFile));

		if (!wavFile->openWrite(file_path)) {
			wavFile->close();
			return -1;
		}
		wavFile->setupInfo(sample_rate, bits_per_sample, channel);

	} else if (format == 1) {

//		LOGD("-write speex OPEN SPEEX");

		speexFile = new WaveSpeexFile();

		if (!speexFile->openWrite(file_path)) {
			speexFile->close();
			return -1;
		}

//		LOGD("-write speex GGGGGGG sample_rate = %d", sample_rate);
//		LOGD("-write speex GGGGGGG bits_per_sample = %d", bits_per_sample);

		//speexFile->setupInfo(sample_rate, bits_per_sample/2, 8);
		speexFile->setupInfo(sample_rate, 2, 10);

		//bufferStorage = new char[48000];

	} else {
		return -1;
	}

	return 0;
}

int NativeOutputStream::close() {

	if (format == 0) {
		wavFile->close();
		free(wavFile);
	} else if(format == 1) {

	//	LOGD("NativeOutputStream::close() !!!!!!!!!!!!!");
		speexFile->close();
		free(speexFile);
	} else {
		return -1;
	}

	return 0;
}

int NativeOutputStream::write(char *data, int length) {

	if (format == 0) {

//		LOGD("OutputStream write() format = %d", format);

		short* samples = (short*) data;

		try {
			for (int i = 0; i < length / 2; i++) {
				short sample = samples[i];
				wavFile->writeSample(sample);
			}
		} catch (const std::exception& e) {
			return -1;
		}

		return length;

	} else if (format == 1) {

		unsigned int expectedPkgSize = speexFile->expectedPacketSize();
//		LOGD("-write speex expectedPkgSize = %d", expectedPkgSize);
//		LOGD("-write speex bufferStorageLength = %d", bufferStorageLength);
//		LOGD("-write speex length = %d", length);

		char* bufferB = new char[20000];
		int c = 0;
		for (int i = 0; i < bufferStorageLength; i++) {
			char byte = bufferStorage[i];
			bufferB[c] = byte;
			c++;
		}
		for (int i = 0; i < length; i++) {
			char byte = data[i];
			bufferB[c] = byte;
			c++;
		}
		bufferStorage = bufferB;
		bufferStorageLength += length;

		if(bufferStorageLength < expectedPkgSize) {
//			LOGD("-write speex NEW speexLeftBytesLength = %d", bufferStorageLength);
			return 0;
		}

		int coundPkg = bufferStorageLength / expectedPkgSize;
//		LOGD("-write speex coundPkg = %d", coundPkg);

		int lengthForWrite = expectedPkgSize * coundPkg;
//		LOGD("-write speex lengthForWrite = %d", lengthForWrite);

		char bytesForWrite[lengthForWrite];
		for (int i = 0; i < lengthForWrite; i++) {
			char byte = bufferStorage[i];
			bytesForWrite[i] = byte;
		}

		speexFile->encodeWavData(bytesForWrite, coundPkg);

//		LOGD("-write speex pkg writed");

		char newLeftByte[bufferStorageLength - lengthForWrite];
		for (int i = lengthForWrite; i < bufferStorageLength; i++) {
			char byte = bufferStorage[i];
			newLeftByte[i] = byte;
		}

		bufferStorageLength = bufferStorageLength - lengthForWrite;

		bufferStorage = newLeftByte;

//		LOGD("-write speex pkg writed speexLeftBytesLength =  %d", bufferStorageLength);


		return lengthForWrite;
	}

	return 0;

}


