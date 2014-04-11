//
//  SEProject.h
//  SoundRecorder
//
//  Created by Igor on 11.04.14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SEModel.h"
#import "SEAudioStreamPlayer.h"
#import "SERecord.h"

@interface SEProject : SEModel

/** Project name */
@property(nonatomic,strong) NSString* name;

/** Project file path */
@property(nonatomic,strong) NSString* projectFilePath;

/** Project sounds path */
@property(nonatomic,readonly) NSString* projectSoundsPath;

/** List of records related to this project */
@property(nonatomic,readonly) NSArray* records;

/** Project audio preview stream */
@property(nonatomic,readonly) SEAudioStreamPlayer* audioStream;

/** Check project if it is change (add or remove record affects that) */
@property(nonatomic,readonly) BOOL isChanged;

/* Split record in time position */
- (SERecord*)splitRecordInPosition:(NSTimeInterval)position;

/** Add record to project */
- (void)addRecord:(SERecord*)record;

/** Delete record from project */
- (void)deleteRecord:(SERecord*)record;

/** Change records order */
- (void)moveRecord:(SERecord*)record toIndex:(NSInteger)index;

/** Remove all records from project including all sound that are saved to the project sound folder */
- (void)removeAllRecords;

/** Save project */
- (void)saveProject;

/** Save project in asynchronously (in another thread) */
- (void)saveProjectAsynchronouslyWithCompletion:(void(^)(NSError* error))completion;

@end
