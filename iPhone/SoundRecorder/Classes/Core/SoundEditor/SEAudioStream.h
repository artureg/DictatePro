//
//  SEAudioStream.h
//  SoundRecorder
//
//  Created by Igor on 11.04.14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import <AudioToolbox/AudioToolbox.h>

@interface SEAudioStream : NSObject

/** 
 Audio File Description
 */
@property(nonatomic,assign) AudioStreamBasicDescription* audioDescription;

/* Audio Duration* */
@property(nonatomic,readonly) NSTimeInterval duration;

/* File Path */
@property(nonatomic,readonly) NSURL* filePathURL;

/* Last Error */
@property(nonatomic,readonly) NSError* error;

/* Stream Length in bytes */
@property(nonatomic,readonly) NSInteger length;

/* Number of samples including all channels */
@property(nonatomic,readonly) NSInteger numberOfSamples;

/* Number of samples per channel */
@property(nonatomic,readonly) NSInteger numberOfSamplesPerChannel;

/* Create stream in memory */
- (id)init;

/* Load from server */
- (id)initWithURL:(NSString*)url;

/* Load from Storage */
- (id)initWithContentsOfFile:(NSString*)file;

/* Open Stream */
- (void)open;

/* Close Stream */
- (void)close;

/* Delete all information in stream */
- (void)clear;

/* Seek to position in samples include all channels */
- (void)seekToSamplePosition:(NSInteger)position;

/* Seek to second */
- (void)seekToSecond:(NSTimeInterval)second;

@end

@interface SEAudioStream(Write)

- (void)writeSamples:(NSData*)samples;      /* Write Samples* using NSData */

/* Write Samples using data */
- (void)writeSamples:(void*)data count:(NSInteger)count;

@end

@interface SEAudioStream(Read)

/* Read Samples from All Channels */
- (NSData*)readSamplesWithCount:(NSInteger)count;

/* Read Samples from one channel */
- (NSData*)readSamplesFromChannel:(NSInteger)channels count:(NSInteger)count;

/* Read Samples data */
- (void)readSamples:(void*)samples count:(NSInteger)count;

@end
