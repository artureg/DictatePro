//
//  WaveFile.h
//  SPEEXConverter
//
//  Created by Igor on 3/19/14.
//  Copyright (c) 2014 Igor. All rights reserved.
//

#ifndef __SPEEXConverter__WaveFile__
#define __SPEEXConverter__WaveFile__

#include <iostream>
#include "RiffFile.h"

#endif /* defined(__SPEEXConverter__WaveFile__) */

typedef struct {
    unsigned short  audioFormat;
    unsigned short  numberOfChannels;
    unsigned long   sampleRate;
    unsigned long   bytesPerSecond;
    unsigned short  bytesPerSample;
    unsigned short  bitsPerSample;
}WaveFMTInfo;

class WaveFile {
public:
    WaveFile();
    ~WaveFile();
    
    // Get Last Error
    const char* getError() {return p_error;}
    
    // Open For Reading
    virtual bool openRead(const char* filePath);
    
    // Open For Writing
    virtual bool openWrite(const char* filePath);
    
    // Close File
    virtual void close();
    
    // Info
    virtual WaveFMTInfo& getFMTInfo();
    unsigned long getNumberOfSamples();
    unsigned long getNumberOfFrames();
    unsigned long getDuration();
    unsigned long getDataSize();
    void showInfo();
    void setupInfo(int sampleRate, short bitsPerSample, short channels);
    
    FILE* getFile();
    
    // Read
    bool readSample(unsigned char& sample);
    bool readSample(short& sample);
    bool readSample(long& sample);
    bool readRaw(char* buffer, size_t numBytes = 1);
    
    //Write
    bool writeSample(unsigned char& sample);
    bool writeSample(short& sample);
    bool writeSample(long& sample);
    bool writeRaw(char* buffer, size_t numBytes = 1);

protected:
    RiffFile*           p_riffFile;
    FILE*               p_writeFile;
    const char*         p_filePath;
    const char*         p_error;
    WaveFMTInfo         p_fmtInfo;
    unsigned long       p_dataSize;
    
    void showFMTInfo();
    virtual bool writeHeader();
};
