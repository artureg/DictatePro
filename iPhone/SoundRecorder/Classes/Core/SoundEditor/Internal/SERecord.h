//
//  SERecord.h
//  SoundRecorder
//
//  Created by Igor on 11.04.14.

//

#import "SEModel.h"
#import "SERecordAudioStream.h"

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

/** Record audio stream */
@property(nonatomic,readonly) SERecordAudioStream* audioStream;

@end
