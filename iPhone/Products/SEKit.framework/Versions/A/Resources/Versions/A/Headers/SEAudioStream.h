//  SEAudioStream.h
//  SoundRecorder
//  Created by Igor Danich igor.danich@wise-apps.com on 11.04.14.

typedef enum {
    kSEAudioStreamModeUnknown   = 0,
    kSEAudioStreamModeRead      = 1,
    kSEAudioStreamModeWrite     = 2
}TSEAudioStreamMode;

@interface SEAudioStream : NSObject

/**  Audio File Description */
@property(nonatomic,readonly) AudioStreamBasicDescription audioDescription;

/** Audio Duration* */
@property(nonatomic,readonly) NSTimeInterval duration;

/** Source URL */
@property(nonatomic,readonly) NSURL* URL;

/** Last Error */
@property(nonatomic,readonly) NSError* error;

/** Stream Length in bytes */
@property(nonatomic,readonly) NSUInteger length;

/** Other stream */
@property(nonatomic,readonly) SEAudioStream* otherStream;

/* Stream open mode */
@property(nonatomic,readonly) TSEAudioStreamMode mode;

/** Create stream in memory */
- (instancetype)init;

/** Init with another audio stream */
- (instancetype)initWithAudioStream:(SEAudioStream*)stream;

/** Load from server */
- (instancetype)initWithURL:(NSURL*)url;

/** Load from Storage */
- (instancetype)initWithContentsOfFile:(NSString*)file;

/** Open Stream */
- (BOOL)openWithMode:(TSEAudioStreamMode)mode;

/** Close Stream */
- (BOOL)close;

/** Delete all information in stream */
- (BOOL)clear;

/** Export all data to file*/
- (NSError*)exportToFile:(NSString*)filePath;

/** Export all data to file asynchronously*/
- (void)exportAsynchronouslyToFile:(NSString*)filePath completion:(void(^)(NSError* error))completion;

@end

@interface SEAudioStream(Write)

/** Set audio info for writing */
- (void)adjustToAudioDescription:(AudioStreamBasicDescription)aInfo;

/** Write to the end of the stream using NSData */
- (BOOL)writeData:(NSData*)samples;

@end

@interface SEAudioStream(Read)

/** 
 Read samples
 data - mutable data to append with stream samples
 position - position of sound file in milliseconds
 duration - duration of reading data in milliseconds
 */
- (BOOL)readData:(NSMutableData*)data position:(NSUInteger)position duration:(NSUInteger)duration;

- (NSTimeInterval)durationForBufferWithSize:(NSUInteger)size;

@end
