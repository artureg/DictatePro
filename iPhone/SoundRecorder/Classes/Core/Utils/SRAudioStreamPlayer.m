//
//  SRAudioStream.m
//  SoundRecorder
//
//  Created by Igor on 4/7/14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SRAudioStreamPlayer.h"

typedef enum {
    kSRAudioStreamPlayerErrorCodeSuccess,
    kSRAudioStreamPlayerErrorCodeAudioOpenStream,
    kSRAudioStreamPlayerErrorCodeAudioEnqueueBuffer,
    kSRAudioStreamPlayerErrorCodeAudioAllocateBuffer,
    kSRAudioStreamPlayerErrorCodeAudioStart
}TSRAudioStreamPlayerErrorCode;

@interface SRAudioStreamPlayer()
@property(nonatomic,assign) AudioStreamBasicDescription pv_audioInfo;
@property(nonatomic,assign) AudioQueueRef               pv_audioQueue;
@property(nonatomic,assign) NSTimeInterval              pv_position;

- (void)pm_processOutputBuffer:(AudioQueueBufferRef)buffer queue:(AudioQueueRef)queue;
- (void)pm_performErrorWithCode:(TSRAudioStreamPlayerErrorCode)errorCode;
- (void)pm_readDataWithBuffer:(AudioQueueBufferRef)buffer;

@end

void SRAudioStreamPlayerOutputBufferCallback(void* inUserData, AudioQueueRef inAQ, AudioQueueBufferRef inBuffer) {
	[(__bridge SRAudioStreamPlayer*)inUserData pm_processOutputBuffer:inBuffer queue:inAQ];
}

@implementation SRAudioStreamPlayer

- (void)dealloc {
    [self stop];
}

- (id)initWithSampleRate:(NSInteger)sampleRate bitsPerSample:(NSInteger)bitsPerSample numberOfChannels:(NSInteger)numberOfChannels {
    if (self = [super init]) {
        NSInteger bytesPerSample = bitsPerSample/8;
        AudioStreamBasicDescription streamFormat;
        streamFormat.mSampleRate = sampleRate;
        streamFormat.mFormatID = kAudioFormatLinearPCM;
        streamFormat.mFormatFlags = kLinearPCMFormatFlagIsSignedInteger;
        streamFormat.mBitsPerChannel = bitsPerSample;
        streamFormat.mChannelsPerFrame = numberOfChannels;
        streamFormat.mFramesPerPacket = 1;
        streamFormat.mBytesPerFrame = bytesPerSample*streamFormat.mChannelsPerFrame;
        streamFormat.mBytesPerPacket = streamFormat.mBytesPerFrame*streamFormat.mFramesPerPacket;
        streamFormat.mReserved = 0;
        self.pv_audioInfo = streamFormat;
    }
    return self;
}

- (void)start {
    if (self.isPaused) {
        AudioQueueStart(_pv_audioQueue, NULL);
    } else {
        self.pv_position = 0;
        OSStatus err;
        
        // New output queue ---- PLAYBACK ----
        err = AudioQueueNewOutput(&_pv_audioInfo, SRAudioStreamPlayerOutputBufferCallback, (__bridge void *)(self), nil, nil, 0, &_pv_audioQueue);
        if (err != noErr) {
            [self stop];
            [self pm_performErrorWithCode:kSRAudioStreamPlayerErrorCodeAudioOpenStream];
            return;
        }
        
        AudioQueueBufferRef buffer = NULL;
        [self pm_readDataWithBuffer:buffer];
        
        // Start queue
        err = AudioQueueStart(_pv_audioQueue, nil);
        if (err != noErr) {
            [self stop];
            [self pm_performErrorWithCode:kSRAudioStreamPlayerErrorCodeAudioStart];
            return;
        }
    }
    _isPlaying = YES;
    _isPaused = NO;
}

- (void)pause {
    if (!self.isPlaying) {
        return;
    }
    _isPaused = YES;
    AudioQueuePause(_pv_audioQueue);
}

- (void)stop {
    _isPaused = NO;
    _isPlaying = YES;
    AudioQueueDispose(_pv_audioQueue, YES);
}

- (void)pm_performErrorWithCode:(TSRAudioStreamPlayerErrorCode)errorCode {
    NSString* text = nil;
    switch (errorCode) {
        case kSRAudioStreamPlayerErrorCodeAudioOpenStream:
            text = @"Can't open stream";
            break;
        case kSRAudioStreamPlayerErrorCodeAudioEnqueueBuffer:
            text = @"Can't enqueue buffer";
            break;
        case kSRAudioStreamPlayerErrorCodeAudioAllocateBuffer:
            text = @"Can't allocate buffer";
            break;
        case kSRAudioStreamPlayerErrorCodeAudioStart:
            text = @"Can't start playing";
            break;
        default:
            text = @"";
            break;
    }
    NSError* error = [NSError errorWithDomain:@"com.audio-stream-player" code:errorCode userInfo:@{NSLocalizedDescriptionKey : text}];
    [self.delegate audioStreamPlayerDecodeErrorDidOccur:self error:error];
}

- (void)pm_readDataWithBuffer:(AudioQueueBufferRef)buffer {
    NSTimeInterval duration = 3;
    NSData* data = [self.delegate audioStreamPlayerProcessData:self position:self.pv_position duration:duration];
    if ([data length] == 0) {
        [self stop];
        return;
    }
    self.pv_position += duration;
    OSStatus err;
    err = AudioQueueAllocateBuffer(_pv_audioQueue, [data length], &buffer);
    if (err == noErr) {
        memcpy(buffer->mAudioData, [data bytes], [data length]);
        buffer->mAudioDataByteSize = [data length];
        buffer->mPacketDescriptionCount = [data length]/self.pv_audioInfo.mBytesPerPacket;
        err = AudioQueueEnqueueBuffer(_pv_audioQueue, buffer, 0, nil);
        if (err != noErr) {
            [self stop];
            [self pm_performErrorWithCode:kSRAudioStreamPlayerErrorCodeAudioEnqueueBuffer];
        }
    } else {
        [self stop];
        [self pm_performErrorWithCode:kSRAudioStreamPlayerErrorCodeAudioAllocateBuffer];
        return;
    }
}

#pragma mark - Audio Callbacks

- (void)pm_processOutputBuffer:(AudioQueueBufferRef)buffer queue:(AudioQueueRef)queue {
    [self pm_readDataWithBuffer:buffer];
}

@end
