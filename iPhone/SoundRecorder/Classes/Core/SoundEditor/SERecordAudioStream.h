//
//  SERecordAudioStream.h
//  SoundRecorder
//
//  Created by Igor on 11.04.14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SEAudioStream.h"

@class SRRecord;
@protocol SERecordAudioStreamDelegate;

@interface SERecordAudioStream : SEAudioStream

@property(nonatomic,weak) id<SERecordAudioStreamDelegate> delegate;

/** Pointer to record */
@property(nonatomic,readonly) SRRecord* record;

/** Initialize stream with record */
- (id)initWithRecord:(SRRecord*)record;

/** Start recording sound */
- (void)startRecording;

/** Stop recording sound */
- (void)stopRecording;

@end

@protocol SERecordAudioStreamDelegate<NSObject>

/** Notification for begin recording */
- (void)recordAudioStreamDidStartRecording:(SERecordAudioStream*)stream;

/** Notification for update recording info */
- (void)recordAudioStream:(SERecordAudioStream*)stream didUpdateWithDuration:(NSTimeInterval)duration;

/** Notification for end recording */
- (void)recordAudioStreamDidFinishRecording:(SERecordAudioStream*)stream;

@end