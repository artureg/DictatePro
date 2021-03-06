//
//  WaveSpeexFile.h
//  SPEEXConverter
//
//  Created by Igor on 3/20/14.
//  Copyright (c) 2014 Igor. All rights reserved.
//

#ifndef __SPEEXConverter__WaveSpeexFile__
#define __SPEEXConverter__WaveSpeexFile__

#include <iostream>
#include "WaveFile.h"

#endif /* defined(__SPEEXConverter__WaveSpeexFile__) */

// NB=160, WB=320, UWB=640

struct WaveSpeexFileInfo {
	char somedata[2];
	unsigned short acmVersion;             // 1
	char speexName[8];           // "Speex\0\0\0" size=8
	char speexVersion[20];       // "speex-1.0\0\0\0\0\0\0\0\0\0\0\0" size=20
	unsigned long speexVersionID;         // 1
	unsigned long headerSize;             // 80
	unsigned long sampleRate;
	unsigned long bandMode;               // NB=0, WB=1, UWB=2
	unsigned long modeBitstreamVersion;   // 4
	unsigned long numberOfChannels;       // 1(mono) or 2(stereo)
	unsigned long reserved1;              // -1
	unsigned long frameSize;
	unsigned long vbr;
	unsigned long framesPerPacket;
	char reserved2[12];          // size=12
	char homepage[32];           // "\tCodec homepage www.openacm.org\0" size=32
};

struct SpeexCodingInfo {
	int quality;
	int enh;
	int mode;
	int subMode;
	int frameSize;
	int framesPerPacket;
	int vbr;
	int complexity;
	int plc_tunning;
	int highpass;
};
;

class WaveSpeexFile: WaveFile {
public:
	WaveSpeexFile();

	// Get Last Error
	const char* getError() {
		return p_error;
	}

	// Open For Reading
	bool openRead(const char* filePath);

	// Open For Writing
	bool openWrite(const char* filePath);

	// Close File
	void close();

	// Info
	WaveFMTInfo& getFMTInfo();
	double getDuration();
	void showInfo();
	void setupInfo(int sampleRate, int bytesPerSample, int quality);
	double getDurationForBufferSize(unsigned long);

	// Encoding
	unsigned int expectedPacketSize();
	bool encodeWavData(const void* data, int numberOfPackets);

	// Decoding
	bool decodeToData(int offsetMilliSeconds, int durationMilliSeconds,
			void* data, int* size);

private:
	WaveSpeexFileInfo p_spxInfo;
	double p_duration;

	void* p_writeState;
	void* p_readState;
	int p_bytesPerSample;
	SpeexCodingInfo p_coding;
	bool writeHeader();
};
