//
//  SESpeexACMWrapper.h
//  SoundRecorder
//
//  Created by Igor on 4/18/14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SESpeexACMWrapper : NSObject

@property(nonatomic,readonly) NSTimeInterval    duration;
@property(nonatomic,readonly) NSUInteger        sampleRate;

/** Open for reading */
- (BOOL)openRead:(NSString*)filePath;

/** Open for writing */
- (BOOL)openWrite:(NSString*)filePath;

/** Close file */
- (void)close;

/** 
 Setting up file
 Sample rate - samples per second
 quality - qulity if speex acm file
 */
- (void)adjustToSampleRate:(NSUInteger)sampleRate bytesPerSample:(NSUInteger)bytesPerSample quality:(NSUInteger)quality;

/** Size of 1 packet for writing */
- (NSUInteger)expectedPacketSize;

/** Write packets to file */
- (BOOL)writeData:(NSData*)data;

/** Write packets from file */
- (void)readData:(NSMutableData*)data position:(NSUInteger)position duration:(NSUInteger)duration;
- (NSTimeInterval)durationForBufferWithSize:(NSUInteger)size;

@end
