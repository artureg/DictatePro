//
//  SpeexACMConvert.h
//
//  Created on: 3/20/14.
//  Author: Igor Danich <igor.danich@wise-apps.com>
//

#ifndef __SPEEXConverter__SpeexACMConvert__
#define __SPEEXConverter__SpeexACMConvert__

#include <iostream>

bool encodeWavToSpeexACM(const char* wavFilePath, const char* compressedFilePath);
bool decodeSpeexACMToWav(const char* compressedFilePath, const char* wavFilePath);

#endif /* defined(__SPEEXConverter__SpeexACMConvert__) */
