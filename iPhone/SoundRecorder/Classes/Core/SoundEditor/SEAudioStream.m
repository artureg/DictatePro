//
//  SEAudioStream.m
//  SoundRecorder
//
//  Created by Igor on 11.04.14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SEAudioStream.h"

@implementation SEAudioStream

- (id)init {
    if (self = [super init]) {
    }
    return self;
}

- (id)initWithURL:(NSString*)url {
    if (self = [super init]) {
    }
    return self;
}

- (id)initWithContentsOfFile:(NSString*)file {
    if (self = [super init]) {
    }
    return self;
}

- (void)open {
}

- (void)close {
}

- (void)clear {
}

- (void)writeSamples:(NSData*)samples {
}

- (void)writeSamples:(void*)data count:(NSInteger)count {
}

- (NSData*)readSamplesWithCount:(NSInteger)count {
    return nil;
}

- (NSData*)readSamplesFromChannel:(NSInteger)channels count:(NSInteger)count {
    return nil;
}

- (void)readSamples:(void*)samples count:(NSInteger)count {
}

- (void)seekToSamplePosition:(NSInteger)position {
}

- (void)seekToSecond:(NSTimeInterval)second {
}

@end
