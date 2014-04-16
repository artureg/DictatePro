//  SEAudioStreamPlayer.h
//  SoundRecorder
//  Created by Igor Danich igor.danich@wise-apps.com on 11.04.14.


#import <AudioToolbox/AudioToolbox.h>

@protocol SEAudioStreamEngineDelegate;
@class SEAudioStream;

typedef enum {
    kSEAudioStreamEngineStateNotReady,
    kSEAudioStreamEngineStateReady,
    kSEAudioStreamEngineStatePlaying,
    kSEAudioStreamEngineStatePaused,
    kSEAudioStreamEngineStateRecording,
}TSEAudioStreamEngineState;

@interface SEAudioStreamEngine : NSObject

/** SEAudioStreamPlayerDelegate */
@property(nonatomic,weak) id<SEAudioStreamEngineDelegate> delegate;

/** Pointer to stream */
@property(nonatomic,readonly) SEAudioStream* audioStream;

/** State of engine */
@property(nonatomic,readonly) TSEAudioStreamEngineState state;

/** Current Time of audio track */
@property(nonatomic,assign) NSTimeInterval currentTime;

/** Volume 0-1 */
@property(nonatomic,assign) CGFloat volume;

/** Track duration */
@property(nonatomic,readonly) NSTimeInterval duration;

/** Last error */
@property(nonatomic,readonly) NSError* error;

/** Load audio stream to player */
- (instancetype)initWithStream:(SEAudioStream*)stream;

/** Start playing */
- (void)startPlaying;

/** Pause on current position*/
- (void)pausePlaying;

/** Stop playing and seek to start */
- (void)stopPlaying;

/** Start recording to stream */
- (void)startRecording;

/** Stop recording to stream */
- (void)stopRecording;

@end

@protocol SEAudioStreamEngineDelegate<NSObject>

@optional

/** Notification for begin playing */
- (void)audioStreamEngineDidStartPlaying:(SEAudioStreamEngine*)engine;

/** Notification for pause playing */
- (void)audioStreamEngineDidPause:(SEAudioStreamEngine*)engine;

/** Notification for continue playing after pause */
- (void)audioStreamEngineDidContinue:(SEAudioStreamEngine*)engine;

/** Notification for updating info about play state */
- (void)audioStreamEnginePlaying:(SEAudioStreamEngine*)engine didUpdateWithCurrentTime:(NSTimeInterval)time;

/** Notification for end playing */
- (void)audioStreamEngineDidFinishPlaying:(SEAudioStreamEngine*)engine stopped:(BOOL)stopped;

/** Notification for start recording */
- (void)audioStreamEngineDidStartRecording:(SEAudioStreamEngine*)engine;

/** Notification for updating info about play state */
- (void)audioStreamEngineRecording:(SEAudioStreamEngine*)engine didUpdateWithCurrentTime:(NSTimeInterval)time;

/** Notification for end recording */
- (void)audioStreamEngineDidStopRecording:(SEAudioStreamEngine*)engine;

/** Notification for error */
- (void)audioStreamEngine:(SEAudioStreamEngine*)engine didOccurError:(NSError*)error;

@end
