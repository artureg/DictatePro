//
//  SpeexACMConvert.h
//  SPEEXConverter
//
//  Created by Igor on 3/20/14.
//  Copyright (c) 2014 Igor. All rights reserved.
//

#ifndef __SPEEXConverter__SpeexACMConvert__
#define __SPEEXConverter__SpeexACMConvert__

int getFormat(char* path);
bool encodeWavToSpeexACM(const char* wavFilePath, const char* compressedFilePath);
bool decodeSpeexToWav(const char* compressedFilePath, const char* wavFilePath);
bool decodeSpeexACMStream(const char* compressedFilePath, double positionInMilliseconds, double durationInMilliseconds, void* data, int* length);
int getACMSpeexFileSampleRate(const char* compressedFilePath);
double getACMSpeexFileDuration(const char* compressedFilePath);

#endif /* defined(__SPEEXConverter__SpeexACMConvert__) */
