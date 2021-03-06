//  SEProjectStream.h
//  SoundRecorder//  Created by Igor Danich igor.danich@wise-apps.com on 11.04.14.

#import "SEAudioStream.h"

@class SEProject;

@interface SEProjectAudioStream : SEAudioStream

/** Pointer to project instance */
@property(nonatomic,readonly) SEProject* project;

/** Initialize stream with project */
- (instancetype)initWithProject:(SEProject*)project;

@end
