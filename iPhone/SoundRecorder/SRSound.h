//
//  SRSound.h
//  SoundRecorder
//
//  Created by Igor on 12/13/13.
//  Copyright (c) 2013 Igor. All rights reserved.
//

#import <AVFoundation/AVFoundation.h>

@protocol SRSoundDelegate;

@interface SRSound : NSObject

@property(nonatomic,weak) id<SRSoundDelegate>   delegate;
@property(nonatomic,readonly) NSString*         fileName;
@property(nonatomic,readonly) BOOL              isRecording;
@property(nonatomic,readonly) BOOL              isPlaying;
@property(nonatomic,readonly) BOOL              canPlay;
@property(nonatomic,readonly) NSTimeInterval    duration;
@property(nonatomic,readonly) NSTimeInterval    currentTime;

- (id)initWithFileName:(NSString*)fileName;

- (void)record;
- (void)stop;
- (void)play;
- (void)pause;
- (void)prepareToPlay;
- (void)deleteSound;

@end

@protocol SRSoundDelegate <NSObject>
- (void)soundDidStartRecording:(SRSound*)sound;
- (void)sound:(SRSound*)sound recordingPosition:(NSTimeInterval)time duration:(NSTimeInterval)duration;
- (void)soundDidEndRecording:(SRSound*)sound;
- (void)soundDidStartPlaying:(SRSound*)sound;
- (void)soundDidPause:(SRSound*)sound;
- (void)soundDidContinue:(SRSound*)sound;
- (void)sound:(SRSound*)sound playPosition:(NSTimeInterval)time duration:(NSTimeInterval)duration;
- (void)soundDidEndPlaying:(SRSound*)sound;
@end
