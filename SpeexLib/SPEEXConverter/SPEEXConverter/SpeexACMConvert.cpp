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

int getFormat(char* path) {
    WaveSpeexFile* file = new WaveSpeexFile();
    if (!file->openRead(path)) {
        file->close();
        return 0;
    }
    int sampleRate = file->getFMTInfo().sampleRate;
    file->close();
    return sampleRate;
}

bool encodeWavToSpeexACM(const char* wavFilePath, const char* compressedFilePath) {
    WaveSpeexFile* file = new WaveSpeexFile();
    if (!file->openWrite(compressedFilePath)) {
        file->close();
        return false;
    }
//    if (!file->encodeWavFile(wavFilePath)) {
//        file->close();
//        return false;
//    }
    file->close();
    return true;
}

bool decodeSpeexToWav(const char* compressedFilePath, const char* wavFilePath) {
    WaveSpeexFile* file = new WaveSpeexFile();
    if (!file->openRead(compressedFilePath)) {
        file->close();
        return false;
    }
//    if (!file->decodeToWavFile(wavFilePath)) {
//        file->close();
//        return false;
//    }
    file->close();
    return true;
}

bool decodeSpeexACMStream(const char* compressedFilePath, double positionInMilliseconds, double durationInMilliseconds, void* data, int* length) {
    WaveFile* wavFile = new WaveFile();
    if (!wavFile->openRead(compressedFilePath)) {
        return false;
    }
    if (wavFile->getFMTInfo().audioFormat == 41225) {
        wavFile->close();
        WaveSpeexFile* file = new WaveSpeexFile();
        if (!file->openRead(compressedFilePath)) {
            file->close();
            return false;
        }
        if (!file->decodeToData(positionInMilliseconds/1000.0f, durationInMilliseconds/1000.0f, (short*)data, length)) {
            file->close();
            return false;
        }
        file->close();
    } else {
        int offset = wavFile->getFMTInfo().bytesPerSample*wavFile->getFMTInfo().sampleRate*positionInMilliseconds/1000.0f;
        int duration = 2*wavFile->getFMTInfo().bytesPerSample*wavFile->getFMTInfo().sampleRate*durationInMilliseconds/1000.0f;
        FILE* rFile = wavFile->getFile();
        fseek(rFile, offset, SEEK_CUR);
        switch (wavFile->getFMTInfo().bytesPerSample) {
            case 1: {
//                char samples[duration];
                char* samples = (char*)data;
                int size = 0;
                for (int i = 0; i < duration; i++) {
                    size = i;
                    if (feof(rFile)) {
                        break;
                    }
                    unsigned char sample;
                    wavFile->readSample(sample);
                    samples[i] = sample;
                }
                *length = size;
            }break;
            case 2: {
//                short samples[duration];
                int size = 0;
                short* samples = (short*)data;
                for (int i = 0; i < duration/2; i++) {
                    size = i*2;
                    if (feof(rFile)) {
                        break;
                    }
                    short sample;
                    wavFile->readSample(sample);
                    samples[i] = sample;
                }
                *length = size;
            }break;
            default:
                break;
        }
    }
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
    double duration = file->getDuration();
    file->close();
    return duration;
}
