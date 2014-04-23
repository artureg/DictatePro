//  SERecord.h
//  SoundRecorder//  Created by Igor Danich igor.danich@wise-apps.com on 11.04.14.

#import "SEModel.h"
#import "SERecordAudioStream.h"

@class SEProject;

typedef struct {
    NSUInteger start;       /** Sound start position in milliseconds */
    NSUInteger duration;    /** Sound duration from start position in milliseconds */
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
