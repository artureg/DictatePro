//
//  SEProjectStream.h
//  SoundRecorder
//
//  Created by Igor on 11.04.14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SEAudioStream.h"

@class SEProject;

@interface SEProjectAudioStream : SEAudioStream

/** Pointer to project instance */
@property(nonatomic,readonly) SEProject* project;

/** Initialize stream with project */
- (id)initWithProject:(SEProject*)project;

@end
