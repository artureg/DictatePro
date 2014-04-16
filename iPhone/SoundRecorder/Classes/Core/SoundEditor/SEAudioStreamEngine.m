//
//  SEAudioStreamPlayer.m
//  SoundRecorder
//
//  Created by Igor on 11.04.14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SEAudioStreamEngine.h"
#import "SEAudioStream.h"
#import <AVFoundation/AVFoundation.h>

static const NSUInteger kSEAudioStreamEngineSeekDuration        = 500;
static const NSUInteger kSEAudioStreamEngineSeekBuferDuration   = 1000;

typedef enum {
    kSEAudioStreamEngineErrorNone,
    kSEAudioStreamEngineErrorCantLoadStream,
    kSEAudioStreamEngineErrorCantPlay,
    kSEAudioStreamEngineErrorReadBuffer,
    kSEAudioStreamEngineErrorCantPause,
    kSEAudioStreamEngineErrorCantRecord,
    kSEAudioStreamEngineErrorWrongRecordInfo
}TSEAudioStreamEngineError;

#define SEThrowIfError(error, operation)    \
do {                                        \
    if (error) {                                \
        @throw @(operation);                         \
    }                                           \
} while (0)

@interface SEAudioStreamEngine()

@property(nonatomic,strong) SEAudioStream*              pv_stream;
@property(nonatomic,assign) AudioQueueRef               pv_audioOutputQueue;
@property(nonatomic,assign) AudioQueueRef               pv_audioInputQueue;
@property(nonatomic,assign) NSUInteger                  pv_position;
@property(nonatomic,strong) NSOperationQueue*           pv_queue;
@property(nonatomic,assign) TSEAudioStreamEngineState   pv_state;
@property(nonatomic,strong) NSError*                    pv_error;
@property(nonatomic,assign) NSUInteger                  pv_bufferStartPosition;
@property(nonatomic,assign) NSUInteger                  pv_bufferPosition;
@property(nonatomic,strong) NSMutableData*              pv_bufferData;
@property(nonatomic,assign) BOOL                        pv_isBuffering;
@property(nonatomic,assign) AudioQueueBufferRef         pv_audioBuffer;
@property(nonatomic,assign) NSUInteger                  pv_bufferPacketSize;

- (void)pm_performError:(TSEAudioStreamEngineError)errorType;

- (void)pm_processOutputBuffer:(AudioQueueBufferRef)buffer queue:(AudioQueueRef)queue;
- (void)pm_readBufferDataWithDuration:(NSUInteger)duration;

- (int)pm_computeBufferSizeWithFormat:(const AudioStreamBasicDescription*)format seconds:(float)seconds;

- (void)pm_processInputBuffer:(AudioQueueBufferRef)buffer
    queue:(AudioQueueRef)queue
    startTime:(const AudioTimeStamp*)startTime
    numberPacketDescriptions:(UInt32)inNumberPacketDescriptions
    packetDescs:(const AudioStreamPacketDescription*)inPacketDescs;

@end

void SEAudioStreamEngineOutputBufferCallback(void* inUserData, AudioQueueRef inAQ, AudioQueueBufferRef inBuffer) {
	[(__bridge SEAudioStreamEngine*)inUserData pm_processOutputBuffer:inBuffer queue:inAQ];
}

void SEAudioStreamEngineInputBufferCallBack(
    void* inUserData,
    AudioQueueRef inAQ,
    AudioQueueBufferRef inBuffer,
    const AudioTimeStamp* inStartTime,
    UInt32 inNumberPacketDescriptions,
    const AudioStreamPacketDescription *inPacketDescs) {
    [(__bridge SEAudioStreamEngine*)inUserData
        pm_processInputBuffer:inBuffer
        queue:inAQ
        startTime:inStartTime
        numberPacketDescriptions:inNumberPacketDescriptions
        packetDescs:inPacketDescs];
}

@implementation SEAudioStreamEngine {
    AudioQueueBufferRef pv_buffers[3];
}

- (void)dealloc {
    [self.pv_stream close];
    self.pv_stream = nil;
    [self.pv_queue cancelAllOperations];
    self.pv_queue = nil;
    [self stopPlaying];
    [self stopRecording];
}

- (instancetype)initWithStream:(SEAudioStream*)stream {
    if (self = [super init]) {
        self.volume = 1;
        self.pv_stream = stream;
        self.pv_audioOutputQueue = NULL;
        self.pv_audioInputQueue = NULL;
        if (self.pv_stream) {
            self.pv_queue = [NSOperationQueue new];
            [self.pv_queue setMaxConcurrentOperationCount:1];
            self.pv_state = kSEAudioStreamEngineStateReady;
        } else {
            [self pm_performError:kSEAudioStreamEngineErrorCantLoadStream];
        }
    }
    return self;
}

#pragma mark - Info

- (void)setState:(TSEAudioStreamEngineState)state {
    self.pv_state = state;
}

- (void)setCurrentTime:(NSTimeInterval)currentTime {
    if (currentTime < 0) {
        currentTime = 0;
    }
    if (currentTime > self.duration) {
        currentTime = self.duration;
    }
    self.pv_position = currentTime*1000;
    self.pv_bufferStartPosition = self.pv_position;
    self.pv_bufferPosition = self.pv_position;
    if (self.state == kSEAudioStreamEngineStatePlaying) {
        self.pv_bufferData = nil;
        [self pm_readBufferDataWithDuration:kSEAudioStreamEngineSeekBuferDuration];
    }
}

- (NSTimeInterval)currentTime {
    return (NSTimeInterval)self.pv_position/1000;
}

- (SEAudioStream*)audioStream {
    return self.pv_stream;
}

- (void)setVolume:(CGFloat)volume {
    _volume = volume;
    if (self.pv_audioOutputQueue) {
        AudioQueueParameterValue value = volume;
        AudioQueueSetParameter(self.pv_audioOutputQueue, kAudioQueueParam_Volume, value);
    }
}

- (NSTimeInterval)duration {
    return self.pv_stream.duration;
}

- (TSEAudioStreamEngineState)state {
    return self.pv_state;
}

- (NSError*)error {
    return self.pv_error;
}

#pragma mark - Playing

- (void)startPlaying {
    if ((self.pv_state != kSEAudioStreamEngineStateReady)&&(self.pv_state != kSEAudioStreamEngineStatePaused)) {
        return;
    }
    if (self.pv_state == kSEAudioStreamEngineStatePaused) {
        AudioQueueStart(_pv_audioOutputQueue, NULL);
        [[NSOperationQueue mainQueue] addOperationWithBlock:^{
            if ([self.delegate respondsToSelector:@selector(audioStreamEngineDidContinue:)]) {
                [self.delegate audioStreamEngineDidContinue:self];
            }
        }];
    } else {
        if (self.pv_stream.mode != kSEAudioStreamModeUnknown) {
            [self.pv_stream close];
        }
        if (![self.pv_stream openWithMode:kSEAudioStreamModeRead]) {
            [self pm_performError:kSEAudioStreamEngineErrorCantPlay];
            return;
        }
        self.pv_position = 0;
        AudioStreamBasicDescription info = self.pv_stream.audioDescription;
        OSStatus err = AudioQueueNewOutput(
            &info,
            SEAudioStreamEngineOutputBufferCallback,
            (__bridge void *)(self),
            nil,
            nil,
            0,
            &_pv_audioOutputQueue
        );
        if (err != noErr) {
            [self stopPlaying];
            [self pm_performError:kSEAudioStreamEngineErrorCantPlay];
            return;
        }
        AudioQueueParameterValue volume = self.volume;
        AudioQueueSetParameter(self.pv_audioOutputQueue, kAudioQueueParam_Volume, volume);
        self.pv_bufferStartPosition = self.pv_position;
        self.pv_bufferPosition = self.pv_position;
        self.pv_audioBuffer = NULL;
        self.pv_bufferData = nil;
        self.pv_isBuffering = YES;
        [self pm_readBufferDataWithDuration:kSEAudioStreamEngineSeekDuration];
        self.pv_position += kSEAudioStreamEngineSeekDuration;
        self.pv_bufferPacketSize = [self.pv_bufferData length];
        err = AudioQueueAllocateBuffer(self.pv_audioOutputQueue, [self.pv_bufferData length], &_pv_audioBuffer);
        if (err) {
            self.pv_audioBuffer = NULL;
            [self stopPlaying];
            [self pm_performError:kSEAudioStreamEngineErrorCantPlay];
            return;
        }
        memcpy(self.pv_audioBuffer->mAudioData, [self.pv_bufferData bytes], self.pv_bufferPacketSize);
        self.pv_audioBuffer->mAudioDataByteSize = self.pv_bufferPacketSize;
        self.pv_audioBuffer->mPacketDescriptionCount = self.pv_bufferPacketSize/self.pv_stream.audioDescription.mBytesPerPacket;
        err = AudioQueueEnqueueBuffer(self.pv_audioOutputQueue, self.pv_audioBuffer, 0, nil);
        if (err) {
            [self stopPlaying];
            [self pm_performError:kSEAudioStreamEngineErrorReadBuffer];
            return;
        }
        [self pm_readBufferDataWithDuration:kSEAudioStreamEngineSeekBuferDuration];
        [self.pv_queue addOperationWithBlock:^{
            [self pm_readBufferDataWithDuration:kSEAudioStreamEngineSeekBuferDuration];
        }];
        err = AudioQueueStart(self.pv_audioOutputQueue, nil);
        if (err) {
            [self stopPlaying];
            [self pm_performError:kSEAudioStreamEngineErrorCantPlay];
            return;
        }
    }
    self.pv_state = kSEAudioStreamEngineStatePlaying;
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        if ([self.delegate respondsToSelector:@selector(audioStreamEngineDidStartPlaying:)]) {
            [self.delegate audioStreamEngineDidStartPlaying:self];
        }
    }];
}

- (void)pausePlaying {
    if (self.pv_state != kSEAudioStreamEngineStatePlaying) {
        return;
    }
    AudioQueuePause(self.pv_audioOutputQueue);
    self.pv_state = kSEAudioStreamEngineStatePaused;
    if ([self.delegate respondsToSelector:@selector(audioStreamEngineDidPause:)]) {
        [self.delegate audioStreamEngineDidPause:self];
    }
}

- (void)stopPlaying {
    if ((self.pv_state != kSEAudioStreamEngineStateReady)
        &&(self.pv_state != kSEAudioStreamEngineStatePlaying)
        &&(self.pv_state != kSEAudioStreamEngineStatePaused)) {
        return;
    }
    if (!self.pv_audioOutputQueue) {
        return;
    }
    AudioQueueDispose(self.pv_audioOutputQueue, true);
    self.pv_audioOutputQueue = NULL;
    [self.pv_stream close];
    self.pv_state = kSEAudioStreamEngineStateReady;
    
    self.pv_bufferData = nil;
    if (self.pv_audioBuffer) {
        AudioQueueFreeBuffer(self.pv_audioOutputQueue, self.pv_audioBuffer);
        self.pv_audioBuffer = NULL;
    }
    if ([self.delegate respondsToSelector:@selector(audioStreamEngineDidFinishPlaying:stopped:)]) {
        [self.delegate audioStreamEngineDidFinishPlaying:self stopped:YES];
    }
}

- (void)pm_readBufferDataWithDuration:(NSUInteger)duration {
    if (!self.pv_isBuffering) {
        return;
    }
    if (!self.pv_bufferData) {
        self.pv_bufferData = [NSMutableData data];
    }
    NSMutableData* data = [NSMutableData data];
    if ([self.pv_stream readData:data position:self.pv_bufferPosition duration:duration]) {
        self.pv_bufferPosition += [data length];
        [self.pv_bufferData appendData:data];
    } else {
        self.pv_isBuffering = NO;
    }
}

#pragma mark - Play Audio Callbacks

- (void)pm_processOutputBuffer:(AudioQueueBufferRef)buffer queue:(AudioQueueRef)queue {
    if (self.pv_state != kSEAudioStreamEngineStatePlaying) {
        return;
    }
    if ([self.pv_bufferData length] == 0) {
        [self stopPlaying];
        return;
    }
    NSUInteger offset = (self.pv_position - self.pv_bufferStartPosition)*self.pv_bufferPacketSize/kSEAudioStreamEngineSeekDuration;
    UInt32 bufferSize = [self.pv_bufferData length] - self.pv_bufferPacketSize;
    if (bufferSize > self.pv_bufferPacketSize) {
        bufferSize = self.pv_bufferPacketSize;
    }
    NSData* data = [self.pv_bufferData subdataWithRange:NSMakeRange(offset, bufferSize)];
    memcpy(buffer->mAudioData, [data bytes], [data length]);
    buffer->mAudioDataByteSize = [data length];
    buffer->mPacketDescriptionCount = [data length]/self.pv_stream.audioDescription.mBytesPerPacket;
    OSStatus err = AudioQueueEnqueueBuffer(self.pv_audioOutputQueue, buffer, 0, nil);
    if (err) {
        [self stopPlaying];
        [self pm_performError:kSEAudioStreamEngineErrorReadBuffer];
        return;
    }
    self.pv_position += kSEAudioStreamEngineSeekDuration;
    [self.pv_queue addOperationWithBlock:^{
        [self pm_readBufferDataWithDuration:kSEAudioStreamEngineSeekBuferDuration];
    }];
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        if ([self.delegate respondsToSelector:@selector(audioStreamEnginePlaying:didUpdateWithCurrentTime:)]) {
            [self.delegate audioStreamEnginePlaying:self didUpdateWithCurrentTime:self.currentTime];
        }
    }];
}

#pragma mark - Recording

- (void)startRecording {
    if (self.pv_state != kSEAudioStreamEngineStateReady) {
        [self pm_performError:kSEAudioStreamEngineErrorCantRecord];
        return;
    }
    AVAudioSession* audioSession = [AVAudioSession sharedInstance];
    NSError* error = nil;
    [audioSession setCategory:AVAudioSessionCategoryPlayAndRecord error:&error];
    if(error){
        NSLog(@"audioSession: %@ %d %@", [error domain], [error code], [error localizedDescription]);
        return;
    }
    [audioSession setActive:YES error:&error];
    error = nil;
    if(error){
        NSLog(@"audioSession: %@ %d %@", [error domain], [error code], [error localizedDescription]);
        return;
    }
    if (!self.pv_stream.mode != kSEAudioStreamModeWrite) {
        [self.pv_stream close];
    }
    if (![self.pv_stream openWithMode:kSEAudioStreamModeWrite]) {
        [self pm_performError:kSEAudioStreamEngineErrorWrongRecordInfo];
        return;
    }
    [self.pv_queue addOperationWithBlock:^{
        @try {
            NSUInteger bufferByteSize = 0;
            AudioStreamBasicDescription aInfo;
            if (self.pv_stream.audioDescription.mSampleRate == 0) {
                aInfo.mSampleRate = 16000;
                aInfo.mChannelsPerFrame = 1;
                aInfo.mFramesPerPacket = 1;
                aInfo.mBitsPerChannel = 16;
                aInfo.mBytesPerFrame = 2;
                aInfo.mBytesPerPacket =  2;
                aInfo.mFormatID = kAudioFormatLinearPCM;
                aInfo.mFormatFlags = kLinearPCMFormatFlagIsSignedInteger|kLinearPCMFormatFlagIsPacked;
                [self.pv_stream adjustToAudioDescription:aInfo];
            } else {
                aInfo = self.pv_stream.audioDescription;
            }
            SEThrowIfError(
                AudioQueueNewInput(&aInfo, SEAudioStreamEngineInputBufferCallBack, (__bridge void*)(self), nil, nil, 0, &_pv_audioInputQueue),
                kSEAudioStreamEngineErrorWrongRecordInfo
            );
            bufferByteSize = [self pm_computeBufferSizeWithFormat:&aInfo seconds:0.5f];
            for (NSInteger i = 0; i < 3; i++) {
                AudioQueueAllocateBuffer(self.pv_audioInputQueue, bufferByteSize, &pv_buffers[i]);
                AudioQueueEnqueueBuffer(self.pv_audioInputQueue, pv_buffers[i], 0, NULL);
            }
            SEThrowIfError(AudioQueueStart(self.pv_audioInputQueue, 0), kSEAudioStreamEngineErrorWrongRecordInfo);
            self.pv_state = kSEAudioStreamEngineStateRecording;
            [[NSOperationQueue mainQueue] addOperationWithBlock:^{
                if ([self.delegate respondsToSelector:@selector(audioStreamEngineDidStartRecording:)]) {
                    [self.delegate audioStreamEngineDidStartRecording:self];
                }
            }];
        }
        @catch (NSNumber* error) {
            [self.pv_stream close];
            [self pm_performError:[error integerValue]];
            self.pv_audioInputQueue = NULL;
            self.pv_state = kSEAudioStreamEngineStateReady;
        }
    }];
}

- (void)stopRecording {
    if (self.pv_state != kSEAudioStreamEngineStateRecording) {
        return;
    }
    self.pv_state = kSEAudioStreamEngineStateReady;
    [self.pv_queue addOperationWithBlock:^{
        [self.pv_stream close];
        if (self.pv_audioInputQueue) {
            SEThrowIfError(AudioQueueStop(self.pv_audioInputQueue, true), kSEAudioStreamEngineErrorWrongRecordInfo);
            SEThrowIfError(AudioQueueDispose(self.pv_audioInputQueue, true), kSEAudioStreamEngineErrorWrongRecordInfo);
            self.pv_audioInputQueue = NULL;
        }
    }];
    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
        if ([self.delegate respondsToSelector:@selector(audioStreamEngineDidStopRecording:)]) {
            [self.delegate audioStreamEngineDidStopRecording:self];
        }
    }];
}

- (int)pm_computeBufferSizeWithFormat:(const AudioStreamBasicDescription*)format seconds:(float)seconds {
    int packets, frames, bytes = 0;
	@try {
		frames = (int)ceil(seconds * format->mSampleRate);
		
		if (format->mBytesPerFrame > 0)
			bytes = frames * format->mBytesPerFrame;
		else {
			UInt32 maxPacketSize;
			if (format->mBytesPerPacket > 0)
				maxPacketSize = format->mBytesPerPacket;
			else {
				UInt32 propertySize = sizeof(maxPacketSize);
				AudioQueueGetProperty(
                    self.pv_audioInputQueue,
                    kAudioQueueProperty_MaximumOutputPacketSize,
                    &maxPacketSize,
                    &propertySize
                );
			}
			if (format->mFramesPerPacket > 0) {
				packets = frames / format->mFramesPerPacket;
			} else {
				packets = frames;
            }
			if (packets == 0) {
				packets = 1;
            }
			bytes = packets * maxPacketSize;
		}
	} @catch (NSException* e) {
		return 0;
	}	
	return bytes;
}

#pragma mark - Record Callbacks

- (void)pm_processInputBuffer:(AudioQueueBufferRef)buffer
    queue:(AudioQueueRef)queue
    startTime:(const AudioTimeStamp*)startTime
    numberPacketDescriptions:(UInt32)inNumberPacketDescriptions
    packetDescs:(const AudioStreamPacketDescription*)inPacketDescs {
    if (self.pv_state != kSEAudioStreamEngineStateRecording) {
        return;
    }
    @try {
        if (self.pv_stream.mode != kSEAudioStreamModeWrite) {
            @throw @(kSEAudioStreamEngineErrorWrongRecordInfo);
        }
        [self.pv_stream writeData:[NSData dataWithBytes:buffer->mAudioData length:buffer->mAudioDataByteSize]];
        SEThrowIfError(AudioQueueEnqueueBuffer(queue, buffer, 0, NULL), kSEAudioStreamEngineErrorWrongRecordInfo);
        if ([self.delegate respondsToSelector:@selector(audioStreamEngineRecording:didUpdateWithCurrentTime:)]) {
            [self.delegate audioStreamEngineRecording:self didUpdateWithCurrentTime:self.duration];
        }
    }
    @catch (NSNumber* exception) {
        [self stopRecording];
    }
}

#pragma mark - Error

- (void)pm_performError:(TSEAudioStreamEngineError)errorType {
    NSString* errorString = nil;
    switch (errorType) {
        case kSEAudioStreamEngineErrorCantLoadStream:
            errorString = @"Can't load stream";
            break;
        case kSEAudioStreamEngineErrorCantPause:
            errorString = @"Can't pause";
            break;
        case kSEAudioStreamEngineErrorCantPlay:
            errorString = @"Can't play";
            break;
        case kSEAudioStreamEngineErrorCantRecord:
            errorString = @"Can't record";
            break;
        case kSEAudioStreamEngineErrorReadBuffer:
            errorString = @"Can't read buffer";
            break;
        case kSEAudioStreamEngineErrorWrongRecordInfo:
            errorString = @"Wrong record info";
            break;
        default:
            break;
    }
    if (errorString) {
        self.pv_error = [NSError errorWithDomain:@"com.seaudiostreamengine" code:errorType userInfo:@{NSLocalizedDescriptionKey : errorString}];
        if ([self.delegate respondsToSelector:@selector(audioStreamEngine:didOccurError:)]) {
            [self.delegate audioStreamEngine:self didOccurError:self.pv_error];
        }
    } else {
        self.pv_error = nil;
    }
}

@end
