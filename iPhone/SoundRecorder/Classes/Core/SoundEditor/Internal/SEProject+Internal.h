//
//  SEProject+Internal.h
//  SoundRecorder
//
//  Created by Igor on 4/14/14.

//

#import "SEProject.h"
#import "SEProjectAudioStream.h"

@interface SEProject (Internal)

/** List of records related to this project */
@property(nonatomic,readonly) NSArray* records;

/** Project file path */
@property(nonatomic,strong) NSString* projectFilePath;

/** Project sounds path */
@property(nonatomic,readonly) NSString* projectSoundsPath;

/** Project audio preview stream */
@property(nonatomic,readonly) SEProjectAudioStream* audioStream;

/* Split record in time position */
- (SERecord*)splitRecordInPosition:(NSTimeInterval)position;

/** Add record to project */
- (void)addRecord:(SERecord*)record;

/** Delete record from project */
- (void)deleteRecord:(SERecord*)record;

/** Insert record to project */
- (void)insertRecord:(SERecord*)record toIndex:(NSInteger)index;

@end
