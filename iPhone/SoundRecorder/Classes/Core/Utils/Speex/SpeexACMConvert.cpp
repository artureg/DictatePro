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
