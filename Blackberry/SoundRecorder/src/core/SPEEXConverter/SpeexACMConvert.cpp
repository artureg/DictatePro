//
//  SpeexACMConvert.cpp
//  SPEEXConverter
//
//  Created by Igor on 3/20/14.
//  Copyright (c) 2014 Igor Danich. All rights reserved.
//

#include "SpeexACMConvert.h"

#include "WaveSpeexFile.h"

#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>
#include <strings.h>
#include <stdio.h>
#include <string.h>
//#include <src/speex.h>

bool encodeWavToSpeexACM(const char* wavFilePath, const char* compressedFilePath) {
    WaveSpeexFile* file = new WaveSpeexFile();


    printf("RRRRRRRR\n");


    if (!file->openWrite(compressedFilePath)) {
    	printf(" SPEEX ERROR  %s\n", file->getError());

        file->close();
        return false;
    }
    if (!file->encodeWavFile(wavFilePath)) {
    	printf(" SPEEX ERROR  %s\n", file->getError());
        file->close();
        return false;
    }

    file->close();

    return true;
}

bool decodeSpeexACMToWav(const char* compressedFilePath, const char* wavFilePath) {
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
    return file;
}
