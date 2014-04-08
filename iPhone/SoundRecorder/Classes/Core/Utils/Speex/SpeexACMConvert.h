//
//  SpeexACMConvert.h
//  SPEEXConverter
//
//  Created by Igor on 3/20/14.
//  Copyright (c) 2014 Igor. All rights reserved.
//

#ifndef __SPEEXConverter__SpeexACMConvert__
#define __SPEEXConverter__SpeexACMConvert__

bool encodeWavToSpeexACM(const char* wavFilePath, const char* compressedFilePath);
bool decodeSpeexACMStream(const char* compressedFilePath, double positionInMilliseconds, double durationInMilliseconds, void* data, int* length);

#endif /* defined(__SPEEXConverter__SpeexACMConvert__) */
