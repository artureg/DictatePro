//
//  SRSound.h
//  SoundRecorder
//
//  Created by Igor Danich
//  Property of UAB "Mobilios Aplikacijos"
//

#import <AVFoundation/AVFoundation.h>

@protocol SRSoundDelegate;

typedef struct {
    NSTimeInterval start;
    NSTimeInterval duration;
}SRSoundRange;

SRSoundRange SRSoundRangeMake(NSTimeInterval start, NSTimeInterval duration);

@interface SRSound : NSObject

@property(nonatomic,weak) id<SRSoundDelegate>   delegate;
@property(nonatomic,readonly) NSString*         filePath;
@property(nonatomic,readonly) BOOL              isRecording;
@property(nonatomic,readonly) BOOL              isPlaying;
@property(nonatomic,readonly) BOOL              canPlay;
@property(nonatomic,readonly) NSTimeInterval    duration;
@property(nonatomic,assign) NSTimeInterval      currentTime;
@property(nonatomic,assign) SRSoundRange        timeRange;

- (id)initWithFilePath:(NSString*)filePath;

- (void)record;
- (void)stop;
- (void)play;
- (void)pause;
- (void)prepareToPlay;
- (void)deleteSound;
- (void)clearTimeRange;

@end

@protocol SRSoundDelegate <NSObject>
@optional
- (void)soundDidStartRecording:(SRSound*)sound;
- (void)sound:(SRSound*)sound recordingPosition:(NSTimeInterval)time duration:(NSTimeInterval)duration;
- (void)soundDidEndRecording:(SRSound*)sound;
- (void)soundDidStartPlaying:(SRSound*)sound;
- (void)soundDidPause:(SRSound*)sound;
- (void)soundDidContinue:(SRSound*)sound;
- (void)sound:(SRSound*)sound playPosition:(NSTimeInterval)time duration:(NSTimeInterval)duration;
- (void)soundDidEndPlaying:(SRSound*)sound;
@end
