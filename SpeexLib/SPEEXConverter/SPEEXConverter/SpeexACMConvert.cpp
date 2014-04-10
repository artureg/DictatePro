//
//  SpeexACMConvert.cpp
//  SPEEXConverter
//
//  Created by Igor on 3/20/14.
//  Copyright (c) 2014 Igor. All rights reserved.
//

#include "SpeexACMConvert.h"

#include "WaveSpeexFile.h"

#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>
#include <strings.h>
#include <stdio.h>
#include <string.h>
#include "speex.h"

bool encodeWavToSpeexACM(const char* wavFilePath, const char* compressedFilePath) {
    WaveSpeexFile* file = new WaveSpeexFile();
    if (!file->openWrite(compressedFilePath)) {
        file->close();
        return false;
    }
    if (!file->encodeWavFile(wavFilePath)) {
        file->close();
        return false;
    }
    file->close();
    return true;
}

bool decodeSpeexToWav(const char* compressedFilePath, const char* wavFilePath) {
    WaveSpeexFile* file = new WaveSpeexFile();
    if (!file->openRead(compressedFilePath)) {
        file->close();
        return false;
    }
    if (!file->decodeToWavFile(wavFilePath)) {
        file->close();
        return false;
    }
    file->close();
    return true;
}

bool decodeSpeexACMStream(const char* compressedFilePath, double positionInMilliseconds, double durationInMilliseconds, void* data, int* length) {
    WaveSpeexFile* file = new WaveSpeexFile();
    if (!file->openRead(compressedFilePath)) {
        file->close();
        return false;
    }
    if (!file->decodeToData(positionInMilliseconds/1000, durationInMilliseconds/1000, (short*)data, length)) {
        file->close();
        return false;
    }
    file->close();
    return true;
}

int getACMSpeexFileSampleRate(const char* compressedFilePath) {
    WaveSpeexFile* file = new WaveSpeexFile();
    if (!file->openRead(compressedFilePath)) {
        file->close();
        return 0;
    }
    int sampleRate = file->getFMTInfo().sampleRate;
    file->close();
    return sampleRate;
}

double getACMSpeexFileDuration(const char* compressedFilePath) {
    WaveSpeexFile* file = new WaveSpeexFile();
    if (!file->openRead(compressedFilePath)) {
        file->close();
        return 0;
    }
    int duration = file->getDuration();
    file->close();
    return duration;
}
