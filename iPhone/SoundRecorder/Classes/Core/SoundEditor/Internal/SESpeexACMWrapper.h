//
//  SESpeexACMWrapper.h
//  SoundRecorder
//
//  Created by Igor on 4/18/14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

@interface SESpeexACMWrapper : NSObject

@property(nonatomic,readonly) NSTimeInterval duration;

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

- (void)readData:(NSMutableData*)data position:(NSTimeInterval)position duration:(NSUInteger)duration;
- (NSTimeInterval)durationForBufferWithSize:(NSUInteger)size;

@end
