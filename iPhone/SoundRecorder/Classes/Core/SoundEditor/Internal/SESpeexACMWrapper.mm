//
//  SESpeexACMWrapper.m
//  SoundRecorder
//
//  Created by Igor on 4/18/14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SESpeexACMWrapper.h"

#include <WaveSpeexFile.h>

@implementation SESpeexACMWrapper {
    WaveSpeexFile* pv_speex;
}

- (void)dealloc {
    delete pv_speex;
}

- (id)init {
    if (self = [super init]) {
        pv_speex = new WaveSpeexFile();
    }
    return self;
}

- (NSTimeInterval)duration {
    return pv_speex->getDuration();
}

- (BOOL)openRead:(NSString*)filePath {
    return pv_speex->openRead([filePath cStringUsingEncoding:NSASCIIStringEncoding]);
}

- (BOOL)openWrite:(NSString*)filePath {
    return pv_speex->openWrite([filePath cStringUsingEncoding:NSASCIIStringEncoding]);
}

- (void)close {
    pv_speex->close();
}

- (void)adjustToSampleRate:(NSUInteger)sampleRate bytesPerSample:(NSUInteger)bytesPerSample quality:(NSUInteger)quality {
    pv_speex->setupInfo(sampleRate, bytesPerSample, quality);
}

- (NSUInteger)expectedPacketSize {
    return pv_speex->expectedPacketSize();
}

- (BOOL)writeData:(NSData*)data {
    NSUInteger numberOfPackets = [data length]/[self expectedPacketSize];
    return pv_speex->encodeWavData([data bytes], numberOfPackets - 1);
}

- (void)readData:(NSMutableData*)data position:(NSTimeInterval)position duration:(NSUInteger)duration {
    short cData[128000];
    int size;
    pv_speex->decodeToData(position, duration, cData, &size);
    [data appendBytes:cData length:size];
}

- (NSTimeInterval)durationForBufferWithSize:(NSUInteger)size {
    return pv_speex->getDurationForBufferSize(size);
}

@end
