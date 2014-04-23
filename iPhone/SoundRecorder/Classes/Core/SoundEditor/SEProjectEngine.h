//  SEProjectAudioPlayer.h
//  SoundRecorder
//  Created by Igor Danich igor.danich@wise-apps.com on 4/14/14.


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
