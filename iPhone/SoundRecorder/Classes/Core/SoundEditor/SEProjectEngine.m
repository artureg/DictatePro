//  SEProjectAudioPlayer.m
//  SoundRecorder
//  Created by Igor Danich igor.danich@wise-apps.com on 4/14/14.


#import "SEProjectEngine.h"

#import "SEProject.h"
#import "SEProject+Internal.h"
#import "SEAudioStreamEngine+Internal.h"

@interface SEProjectEngine()<SEAudioStreamEngineDelegate>
@property(nonatomic,strong) SEProject*              pv_project;
@property(nonatomic,strong) SEAudioStreamEngine*    pv_recorder;
@property(nonatomic,assign) NSTimeInterval          pv_tmpTime;
@end

@implementation SEProjectEngine

- (instancetype)initWithProject:(SEProject*)project {
    if (self = [super initWithStream:project.audioStream]) {
        self.pv_project = project;
    }
    return self;
}

- (instancetype)initWithStream:(SEAudioStream*)stream {
    return nil;
}

- (NSTimeInterval)recordingDuration {
    return self.pv_recorder.duration;
}

- (SEProject*)project {
    return self.pv_project;
}

- (void)startRecording {
    if (self.state == kSEAudioStreamEngineStatePaused) {
        [self stopPlayingWithoutNotification];
    }
    if (self.state != kSEAudioStreamEngineStateReady) {
        return;
    }
    [self setState:kSEAudioStreamEngineStateRecording];
    SERecord* record = [self.pv_project splitRecordInPosition:self.currentTime];
    self.pv_recorder = [[SEAudioStreamEngine alloc] initWithStream:record.audioStream];
    self.pv_recorder.delegate = self;
    [self.pv_recorder startRecording];
}

- (void)stopRecording {
    if (!self.pv_recorder) {
        return;
    }
    [self.pv_recorder stopRecording];
    self.currentTime += self.pv_recorder.duration;
    self.pv_recorder = nil;
    [self setState:kSEAudioStreamEngineStateReady];
}

#pragma mark - SEAudioStreamEngineDelegate

- (void)audioStreamEngineDidStartRecording:(SEAudioStreamEngine*)engine {
    if ([self.delegate respondsToSelector:@selector(audioStreamEngineDidStartRecording:)]) {
        [self.delegate audioStreamEngineDidStartRecording:self];
    }
}

- (void)audioStreamEngineRecording:(SEAudioStreamEngine*)engine didUpdateWithCurrentTime:(NSTimeInterval)time {
    if ([self.delegate respondsToSelector:@selector(audioStreamEngineRecording:didUpdateWithCurrentTime:)]) {
        [self.delegate audioStreamEngineRecording:self didUpdateWithCurrentTime:self.currentTime];
    }
}

- (void)audioStreamEngineDidStopRecording:(SEAudioStreamEngine*)engine {
    if ([self.delegate respondsToSelector:@selector(audioStreamEngineDidStopRecording:)]) {
        [self.delegate audioStreamEngineDidStopRecording:self];
    }
}

- (void)audioStreamEngine:(SEAudioStreamEngine*)engine didOccurError:(NSError*)error {
    self.pv_recorder = nil;
    [self setState:kSEAudioStreamEngineStateReady];
    if ([self.delegate respondsToSelector:@selector(audioStreamEngine:didOccurError:)]) {
        [self.delegate audioStreamEngine:self didOccurError:error];
    }
}

@end
