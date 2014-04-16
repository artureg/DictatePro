//
//  SpeexACMConvert.h
//  SPEEXConverter
//
//  Created by Igor on 3/20/14.
//  Copyright (c) 2014 Igor Danich. All rights reserved.
//

#ifndef __SPEEXConverter__SpeexACMConvert__
#define __SPEEXConverter__SpeexACMConvert__

#include <iostream>

bool encodeWavToSpeexACM(const char* wavFilePath, const char* compressedFilePath);
bool decodeSpeexACMToWav(const char* compressedFilePath, const char* wavFilePath);

#endif /* defined(__SPEEXConverter__SpeexACMConvert__) */
