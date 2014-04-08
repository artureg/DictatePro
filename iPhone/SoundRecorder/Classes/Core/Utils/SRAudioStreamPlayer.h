//
//  SRAudioStream.h
//  SoundRecorder
//
//  Created by Igor on 4/7/14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import <AudioToolbox/AudioToolbox.h>

@protocol SRAudioStreamDelegate;

@interface SRAudioStreamPlayer : NSObject

@property(nonatomic,weak) id<SRAudioStreamDelegate> delegate;

@property(nonatomic,readonly) BOOL isPlaying;
@property(nonatomic,readonly) BOOL isPaused;

- (id)initWithSampleRate:(NSInteger)sampleRate bitsPerSample:(NSInteger)bitsPerSample numberOfChannels:(NSInteger)numberOfChannels;
- (void)start;
- (void)pause;
- (void)stop;

@end

@protocol SRAudioStreamDelegate<NSObject>
- (NSData*)audioStreamPlayerProcessData:(SRAudioStreamPlayer*)aStream position:(NSTimeInterval)position duration:(NSTimeInterval)duration;
- (void)audioStreamPlayerDecodeErrorDidOccur:(SRAudioStreamPlayer*)asPlayer error:(NSError*)error;
@end
