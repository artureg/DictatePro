//
//  SEProject.h
//  SoundRecorder
//
//  Created by Igor on 4/1/14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SRRecord.h"
#import "SRSound.h"

@interface SRProject : SEModel

/* Project Info */
@property(nonatomic,strong) NSString*   name;
@property(nonatomic,readonly) NSString* projectPath;
@property(nonatomic,readonly) NSString* projectFilePath;
@property(nonatomic,readonly) NSString* projectSoundsPath;
@property(nonatomic,readonly) NSString* projectSoundPath;
@property(nonatomic,readonly) NSString* projectSoundSpeexPath;
@property(nonatomic,readonly) NSArray*  records;
@property(nonatomic,readonly) NSArray*  sounds;

/* Project Sound */
@property(nonatomic,readonly) SRSound*          projectSound;
@property(nonatomic,readonly) NSTimeInterval    duration;
@property(nonatomic,readonly) BOOL              isChanged;

/** Record new sound */
- (void)startRecording;

/* Stop recording new sound */
- (void)stopRecording;

/* Get sound with path */
- (SRSound*)soundWithPath:(NSString*)path;

/* Split record in time position */
- (void)splitRecord:(SRRecord*)record inPosition:(NSTimeInterval)position;

/* Add record to project */
- (void)addRecord:(SRRecord*)record;

/* Delete record from project */
- (void)deleteRecord:(SRRecord*)record;

/* Change records order */
- (void)moveRecord:(SRRecord*)record toIndex:(NSInteger)index;

/* Add sound to project */
- (void)addSound:(SRSound*)sound;

/* Delete sound from project */
- (void)deleteSound:(SRSound*)sound;

/* Delete all sounds and records */
- (void)clearAll;

/* Remove Sound */
- (void)clearSound;

/* Build Project for Playing */
- (void)buildProjectWithCompletion:(void(^)(NSError* error))completion;

/* Encode to Speex ACM */
- (void)encodeToSpeexACMWithCompletion:(void(^)(NSError* error))completion;

@end
