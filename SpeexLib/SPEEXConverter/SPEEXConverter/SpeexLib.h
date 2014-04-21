//
//  SpeexLib.h
//  SPEEXConverter
//
//  Created by Igor on 17.04.14.
//  Copyright (c) 2014 Igor. All rights reserved.
//

#ifndef SPEEXConverter_SpeexLib_h
#define SPEEXConverter_SpeexLib_h

int readPCMFile(const char* filePath, double offset, double duration, void* data, int* length);
int writePCMFile(const char* filePath, void* data, int length);


#endif
