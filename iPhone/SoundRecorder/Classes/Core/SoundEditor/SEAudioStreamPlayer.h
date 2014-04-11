//
//  SEAudioStreamPlayer.h
//  SoundRecorder
//
//  Created by Igor on 11.04.14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import <AudioToolbox/AudioToolbox.h>

@protocol SEAudioStreamPlayerDelegate;
@class SEAudioStream;

@interface SEAudioStreamPlayer : NSObject

@property(nonatomic,weak) id<SEAudioStreamPlayerDelegate> delegate;

/* Check if stream is playing */
@property(nonatomic,readonly) BOOL isPlaying;

/* Check if stream is paused */
@property(nonatomic,readonly) BOOL isPaused;

/* Current Time of audio track */
@property(nonatomic,assign) NSTimeInterval currentTime;

/* Load audio stream to player */
- (id)initWithStream:(SEAudioStream*)stream;

/* Start playing */
- (void)start;

/* Pause on current position*/
- (void)pause;

/* Stop playing and seek to start */
- (void)stop;

@end

@protocol SEAudioStreamPlayerDelegate<NSObject>

@end
