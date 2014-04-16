//
//  SEProjectAudioPlayer.h
//  SoundRecorder
//
//  Created by Igor on 4/14/14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SEAudioStreamEngine.h"

@class SEProject;

@interface SEProjectEngine : SEAudioStreamEngine

/** Pointer to audio project */
@property(nonatomic,readonly) SEProject* project;

/** Recording duration */
@property(nonatomic,readonly) NSTimeInterval recordingDuration;

/** Initialization with Project */
- (instancetype)initWithProject:(SEProject*)project;

@end
