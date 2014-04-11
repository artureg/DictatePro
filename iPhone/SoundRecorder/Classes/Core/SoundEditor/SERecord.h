//
//  SERecord.h
//  SoundRecorder
//
//  Created by Igor on 11.04.14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SEModel.h"

@class SEProject;

typedef struct {
    NSTimeInterval start;       /** Sound start position */
    NSTimeInterval duration;    /** Sound duration from start position */
}SERecordSoundRange;

@interface SERecord : SEModel

/** Pointer to parent project */
@property(nonatomic,weak) SEProject* project;

/** URL for source sound location */
@property(nonatomic,strong) NSURL* soundURL;

/** Range in sound for current record */
@property(nonatomic,assign) SERecordSoundRange soundRange;

@end
