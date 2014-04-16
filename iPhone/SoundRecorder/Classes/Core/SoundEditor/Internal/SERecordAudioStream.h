//
//  SERecordAudioStream.h
//  SoundRecorder
//
//  Created by Igor on 11.04.14.

//

#import "SEAudioStream.h"

@class SERecord;
@protocol SERecordAudioStreamDelegate;

@interface SERecordAudioStream : SEAudioStream

@property(nonatomic,weak) id<SERecordAudioStreamDelegate> delegate;

/** Pointer to record */
@property(nonatomic,readonly) SERecord* record;

/** Initialize stream with record */
- (instancetype)initWithRecord:(SERecord*)record;

@end
