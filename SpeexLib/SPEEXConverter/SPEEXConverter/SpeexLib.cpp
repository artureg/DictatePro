//
//  SpeexLib.cpp
//  SPEEXConverter
//
//  Created by Igor on 17.04.14.
//  Copyright (c) 2014 Igor. All rights reserved.
//

#include "SpeexLib.h"

#include "WaveFile.h"
#include <android/log.h>
#include <jni.h>

int readPCMFile(const char* filePath, double offset, double duration, void* data, int* length) {

    WaveFile* wavFile = new WaveFile();
    if (!wavFile->openRead(filePath)) {
        wavFile->close();
        return 1;
    }
    __android_log_print(ANDROID_LOG_ERROR, " - C NAative", " duration %d", duration);

    int bOffset = wavFile->getFMTInfo().bytesPerSample*wavFile->getFMTInfo().sampleRate*offset;
    int bDuration = wavFile->getFMTInfo().bytesPerSample*wavFile->getFMTInfo().sampleRate*duration;
    FILE* rFile = wavFile->getFile();
    fseek(rFile, bOffset, SEEK_CUR);
    size_t size = fread(data, sizeof(wavFile->getFMTInfo().bytesPerSample), bDuration, rFile);
    *length = size;
    wavFile->close();
    return 0;
}

int writePCMFile(const char* filePath, void* data, int length) {
    WaveFile* wavFile = new WaveFile();

    if (!wavFile->openWrite(filePath)) {
        wavFile->close();
        return 1;
    }

    __android_log_print(ANDROID_LOG_ERROR, " - C NAative", " writePCMFile length %d", length);
//    __android_log_print(ANDROID_LOG_ERROR, " - C NAative", " writePCMFile data size 1 %d", wavFile->getDataSize());

    wavFile->setupInfo(8000, 16, 1);
    wavFile->seekToEnd();
    short* samples = (short*)data;

    for (int i = 0; i < length/2; i++) {
        short sample = samples[i];
        wavFile->writeSample(sample);
    }

    wavFile->close();
    return 0;
}
