/*
 * InputStream.cpp
 *
 *  Created on: 21 apr. 2014 г.
 *      Author: Timofey Kovalenko <timothy.kovalenko@wise-apps.com>
 */

#include "NativeInputStream.h"

#include <android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG  , "-JNI-",__VA_ARGS__)

#include "SPEEXConverter/WaveSpeexFile.h"

NativeInputStream::NativeInputStream() : curPositionInBytes(0), curMilliSecondPosition(0) {
}

NativeInputStream::~NativeInputStream() {
}

int NativeInputStream::open(const char *file_path, int _format) {

	format = _format;

	if (format == 0) {
		wavFile = new WaveFile();

		if (!wavFile->openRead(file_path) || wavFile->getFMTInfo().audioFormat != 1) {
			wavFile->close();
			//LOGD("!!!!!!! JJJJJJJJ  !!!!!!!!!!!");
			return -1;
		}
		//LOGD("RTRTRT wav = %d", wavFile->getFMTInfo().audioFormat);

	} else if (format == 1 ) {

		speexFile = new WaveSpeexFile();

		if (!speexFile->openRead(file_path) || speexFile->getFMTInfo().audioFormat != 41225) {
			speexFile->close();
			return -1;
		}
	//	LOGD("RTRTRT SPEEX = %d", speexFile->getFMTInfo().audioFormat);

	} else {
		return -1;
	}

	return 0;

}

int NativeInputStream::getSampleRate() {

	if (format == 0) {
		return wavFile->getFMTInfo().sampleRate;

	} else if(format == 1) {
		return speexFile->getFMTInfo().sampleRate;
	}

	return -1;
}

int NativeInputStream::skip(int bytes) {

	//LOGD("ERERER = %d", bytes);

	curPositionInBytes = bytes;

	//LOGD("ERERE PRO = %d", bytes);

	if (format == 1) {
		if(bytes != 0) {
			curMilliSecondPosition = ((double)bytes / (speexFile->getFMTInfo().sampleRate * 2))*1000.0f;
		} else {
			curMilliSecondPosition = 0;
		}
	}

	return 0;
}

int NativeInputStream::close() {

	if(format == 0) {

		wavFile->close();
		free(wavFile);

	} else if(format == 1) {

		//LOGD("NativeInputStream::close() !!!!!!!!!!!");
		speexFile->close();
		free(speexFile);
	} else {
		return -1;
	}

	return 0;

}

int NativeInputStream::read(char *data, int length) {

	//LOGD("NativeInputStream.cpp read(buffer)");

	if (format == 0) {

		if(curPositionInBytes >= wavFile->getDataSize()) {
			return 0;
		}

//		LOGD("- wav read() num of samples = %d", wavFile->getNumberOfSamples());
//		LOGD("- wav read() data size = %d", wavFile->getDataSize());
//		LOGD("- wav read() error = %s", wavFile->getError());
//		LOGD("- wav read() wav duration = %f", wavFile->getDuration());
//		LOGD("- wav read() wav length = %d", length);
//		LOGD("- wav read() wav curPositionInBytes = %d", curPositionInBytes);

		FILE* rFile = wavFile->getFile();
		if(curPositionInBytes != 0) {
//			rewind(rFile);
			fseek(rFile, curPositionInBytes, SEEK_SET); //SEEK_CUR
		}

		size_t size = fread(data, sizeof(wavFile->getFMTInfo().bytesPerSample),
				length, rFile);

//		LOGD("- wav read() wav read size = %d", size);

		curPositionInBytes += size;

//		LOGD("- wav read wav curPositionInBytes = %d",curPositionInBytes);

		return size;

	} else if (format == 1) {

//		LOGD("- speex read speex duration = %d", speexFile->getDuration());
//		LOGD("- speex read sample rate = %d", speexFile->getFMTInfo().sampleRate);
//		LOGD("- speex read bytesPerSecond = %d", speexFile->getFMTInfo().bytesPerSecond);
//		LOGD("- speex read bitsPerSample = %d", speexFile->getFMTInfo().bitsPerSample);
//
//		short samples[48000];
////
//		int size = 0;
//		double offsetSecond = curPositionInBytes; // / speexFile->getFMTInfo().bytesPerSecond;
//		double durationSecond = length / speexFile->getFMTInfo().bytesPerSecond;
////
//		bool result = speexFile->decodeToData(offsetSecond, durationSecond, samples, &size);
////
//		data = (char*) samples;
////
//		return size;

//		LOGD("- speex read speex curPositionInBytes = %d", curPositionInBytes);

//		LOGD("- speex read speex duration = %f", speexFile->getDuration());
//		LOGD("- speex read sample rate = %d", speexFile->getFMTInfo().sampleRate);
//		LOGD("- speex read bytesPerSecond = %d", speexFile->getFMTInfo().bytesPerSecond);
//		LOGD("- speex read bitsPerSample = %d", speexFile->getFMTInfo().bitsPerSample);
//		LOGD("- speex read speex expectedPacketSize = %d", speexFile->expectedPacketSize());

		short samples[48000];

		//long k =  speexFile->getFMTInfo().bytesPerSecond;

		//LOGD("- speex read length in byte = %d", length);

		int size = 0;

		int offsetMilliSecond = 0;
		if(curPositionInBytes != 0) {
			offsetMilliSecond = ((double)curPositionInBytes / (speexFile->getFMTInfo().sampleRate * 2))*1000.0f;
		}
		int durationMilliSecond = ((double)length / (speexFile->getFMTInfo().sampleRate * 2))*1000.0f;

//		if(durationMilliSecond > speexFile->getDuration()) {
//			return 0;
//		}
//		durationMilliSecond = 200;
//		LOGD("- speex read offsetMilliSecond = %d", offsetMilliSecond);
//		LOGD("- speex read durationMilliSecond = %d", durationMilliSecond);

		bool result = speexFile->decodeToData(offsetMilliSecond, durationMilliSecond, samples, &size);

		if(!result) {
//			LOGD("- speex read durationSecond CAN NOT DECODE");
			return -1;
		}

//		LOGD("- speex read read !!! size= %d", size);

		char* t_data = (char*) samples;

		for(int i=0; i< size; i++) {
			data[i] = t_data[i];
		}

		if(size > length) {
			curPositionInBytes = curPositionInBytes + length;
			return length;
		} else {
			curPositionInBytes = curPositionInBytes + size;
		}

		return size;

	}

	return 0;

}

int NativeInputStream::read(char *data, long offset, long duration) {

	if (format == 0) {


		if(curPositionInBytes >= wavFile->getDataSize()) {
			return 0;
		}

//		LOGD("- wav read num of samples = %d", wavFile->getNumberOfSamples());
//		LOGD("- wav read data size = %d", wavFile->getDataSize());
//		LOGD("- wav read error = %s", wavFile->getError());
//		LOGD("- wav read wav duration = %f", wavFile->getDuration());

//		LOGD("- wav read offset = %d", offset);
//		LOGD("- wav read duration = %d", duration);
		//int off = offset - curPositionInBytes;
//		short* samples = (short*) data;

		if(offset == 0 && curPositionInBytes != 0) {
			offset = curPositionInBytes;
		}

		FILE* rFile = wavFile->getFile();
		if(curPositionInBytes != 0) {
			fseek(rFile, offset, SEEK_SET); //SEEK_CUR
		}
		size_t size = fread(data, sizeof(wavFile->getFMTInfo().bytesPerSample),
				duration, rFile);

		curPositionInBytes = offset + duration;

//		LOGD("- wav read wav curPositionInBytes = %d",curPositionInBytes);

		return size;

	} else if (format == 1) {

//		LOGD("- speex read speex curPositionInBytes = %d", curPositionInBytes);

//		if(offset == 0 && curPositionInBytes != 0) {
//			offset = curPositionInBytes;
//		}


//		LOGD("- speex read speex duration = %f", speexFile->getDuration());
//		LOGD("- speex read sample rate = %d", speexFile->getFMTInfo().sampleRate);
//		LOGD("- speex read bytesPerSecond = %d", speexFile->getFMTInfo().bytesPerSecond);
//		LOGD("- speex read bitsPerSample = %d", speexFile->getFMTInfo().bitsPerSample);
//
//		LOGD("- speex read speex expectedPacketSize = %d", speexFile->expectedPacketSize());


//		if(speexFile->getDuration() == 0) {
//			return -1;
//		}

		short samples[48000];

		//long k =  speexFile->getFMTInfo().bytesPerSecond;

//		LOGD("- speex read offset in byte = %d", offset);
//		LOGD("- speex read duration in byte = %d", duration);

		int size = 0;

		int offsetMilliSecond = 0;
		if(offset != 0) {
			offsetMilliSecond = ((double)offset / (speexFile->getFMTInfo().sampleRate * 2))*1000.0f;
		}
//		int durationMilliSecond = ((double)duration / (speexFile->getFMTInfo().sampleRate * 2))*1000.0f;

		if(offset == 0 && curMilliSecondPosition != 0) {
			offsetMilliSecond = curMilliSecondPosition;
		}

		int durationMilliSecond = 200;


//		if(durationMilliSecond > speexFile->getDuration()) {
//			return 0;
//		}
//		durationMilliSecond = 200;
//		LOGD("- speex read offsetMilliSecond = %d", offsetMilliSecond);
//		LOGD("- speex read durationMilliSecond = %d", durationMilliSecond);

		bool result = speexFile->decodeToData(offsetMilliSecond, durationMilliSecond, samples, &size);

		if(!result) {
//			LOGD("- speex read durationSecond CAN NOT DECODE");
			return -1;
		}

//		LOGD("- speex read read !!! size= %d", size);

		char* t_data = (char*) samples;

		for(int i=0; i< size; i++) {
			data[i] = t_data[i];
		}

//		if(size > duration) {
//			curPositionInBytes = offset + duration;
//			return duration;
//		} else {
			curPositionInBytes = offset + size;
//		}
		curMilliSecondPosition += durationMilliSecond;
		return size;

	}

	return 0;

}
