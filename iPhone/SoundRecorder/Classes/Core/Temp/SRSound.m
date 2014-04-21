//
//  SRSound.m
//  SoundRecorder
//
//  Created by Igor Danich igor.danich@wise-apps.com Danich

//

#import "SRSound.h"

SRSoundRange SRSoundRangeMake(NSTimeInterval start, NSTimeInterval duration) {
    SRSoundRange range;
    range.start = start;
    range.duration = duration;
    return range;
}

static const NSTimeInterval kSRSoundRecordDuration = 3*60.0f;

@interface SRSound()<AVAudioRecorderDelegate,AVAudioPlayerDelegate>
@property(nonatomic,strong) AVAudioPlayer*      pv_player;
@property(nonatomic,strong) AVAudioRecorder*    pv_recorder;
@property(nonatomic,strong) NSString*           pv_soundPath;
@property(nonatomic,assign) NSTimeInterval      pv_time;
@property(nonatomic,strong) NSTimer*            pv_timer;
- (void)pm_onRecordUpdate;
- (void)pm_onPlayUpdate;
@end

@implementation SRSound

@synthesize currentTime = _currentTime;

- (void)dealloc {
    [self.pv_timer invalidate];
    self.pv_timer = nil;
    self.pv_player.delegate = nil;
    [self.pv_player stop];
    self.pv_player = nil;
    if (self.pv_recorder) {
        self.pv_recorder.delegate = nil;
        [self.pv_recorder stop];
        self.pv_recorder = nil;
        [[NSFileManager defaultManager] removeItemAtPath:self.filePath error:nil];
    }
}

- (id)initWithFilePath:(NSString*)filePath {
    if (self = [super init]) {
        self.pv_soundPath = filePath;
        [self prepareToPlay];
    }
    return self;
}

- (BOOL)canPlay {
    return (self.pv_player != nil);
}

- (NSTimeInterval)currentTime {
    return _currentTime;
    return self.pv_player.currentTime - self.timeRange.start;
}

- (void)setCurrentTime:(NSTimeInterval)currentTime {
    _currentTime = currentTime;
    if (_currentTime >= self.duration) {
        [self stop];
    } else {
        self.pv_player.currentTime = self.timeRange.start + currentTime;
    }
}

- (void)setTimeRange:(SRSoundRange)timeRange {
    _timeRange = timeRange;
    self.pv_player.currentTime = timeRange.start;
}

- (void)clearTimeRange {
    self.timeRange = SRSoundRangeMake(0, self.pv_player.duration);
}

- (void)setVolume:(CGFloat)volume {
    self.pv_player.volume = volume;
}

- (CGFloat)volume {
    return self.pv_player.volume;
}

- (BOOL)isPlaying {
    return self.pv_player.isPlaying;
}

- (BOOL)isRecording {
    return self.pv_recorder.isRecording;
}

- (NSString*)filePath {
    return self.pv_soundPath;
}

- (NSTimeInterval)duration {
    if ([self canPlay]) {
        return self.timeRange.duration;
    } else {
        return 0;
    }
}

- (void)record {
    [self deleteSound];
    self.pv_player = nil;
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayAndRecord error:nil];
    
    AVAudioSession* audioSession = [AVAudioSession sharedInstance];
    NSError* error = nil;
    [audioSession setCategory:AVAudioSessionCategoryPlayAndRecord error:&error];
    if(error){
        NSLog(@"audioSession: %@ %d %@", [error domain], [error code], [error localizedDescription]);
        return;
    }
    [audioSession setActive:YES error:&error];
    error = nil;
    if(error){
        NSLog(@"audioSession: %@ %d %@", [error domain], [error code], [error localizedDescription]);
        return;
    }
    
    NSDictionary* recordSetting = @{
        AVFormatIDKey : @(kAudioFormatLinearPCM),
        AVSampleRateKey : @(16000.0),
        AVNumberOfChannelsKey : @(1),
        AVLinearPCMBitDepthKey : @(16),
        AVEncoderAudioQualityKey : @(AVAudioQualityMedium)
    };
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:self.pv_soundPath]) {
        NSError* error = nil;
        [[NSFileManager defaultManager] removeItemAtPath:self.pv_soundPath error:&error];
        if (error) {
            NSLog(@"%@", [error localizedDescription]);
        }
    }
    
    NSURL* url = [NSURL fileURLWithPath:self.pv_soundPath];
    error = nil;
    self.pv_recorder = [[AVAudioRecorder alloc] initWithURL:url settings:recordSetting error:&error];
    self.pv_recorder.meteringEnabled = YES;
    if(!self.pv_recorder){
        NSLog(@"recorder: %@ %d %@", [error domain], [error code], [error localizedDescription]);
        UIAlertView *alert =
        [[UIAlertView alloc] initWithTitle:@"Warning"
            message:[error localizedDescription]
            delegate:nil
            cancelButtonTitle:@"OK"
            otherButtonTitles:nil];
        [alert show];
        return;
    }
    
    self.pv_recorder.delegate = self;
    [self.pv_recorder prepareToRecord];
    
    [self.pv_recorder recordForDuration:kSRSoundRecordDuration];
    self.pv_time = 0;
    self.pv_timer = [NSTimer scheduledTimerWithTimeInterval:0.1f target:self selector:@selector(pm_onRecordUpdate) userInfo:nil repeats:YES];
    if ([self.delegate respondsToSelector:@selector(soundDidStartRecording:)]) {
        [self.delegate soundDidStartRecording:self];
    }
}

- (void)stop {
    if (self.pv_recorder) {
        [self.pv_timer invalidate];
        self.pv_timer = nil;
        if (!self.pv_recorder.isRecording) {
            return;
        }
        [self.pv_recorder stop];
        NSURL* url = [NSURL fileURLWithPath:self.pv_soundPath];
        NSError* error = nil;
        NSData* audioData = [NSData dataWithContentsOfFile:[url path] options: 0 error:&error];
        if(!audioData) {
            NSLog(@"audio data: %@ %d %@", [error domain], [error code], [[error userInfo] description]);
        }
        NSFileManager *fm = [NSFileManager defaultManager];
        error = nil;
        [fm removeItemAtPath:[url path] error:&error];
        if(error) {
            NSLog(@"File Manager: %@ %d %@", [error domain], [error code], [[error userInfo] description]);
        }
        [audioData writeToFile:self.pv_soundPath atomically:YES];
        self.pv_recorder = nil;
    } else {
        [self.pv_player stop];
        self.pv_player.currentTime = self.timeRange.start;
        _currentTime = self.pv_player.currentTime - self.timeRange.start;
        [self.pv_timer invalidate];
        self.pv_timer = nil;
        if ([self.delegate respondsToSelector:@selector(soundDidEndPlaying:)]) {
            [self.delegate soundDidEndPlaying:self];
        }
    }
}

- (void)prepareToPlay {
    NSError* error = nil;
    self.pv_player = [[AVAudioPlayer alloc] initWithContentsOfURL:[NSURL fileURLWithPath:self.pv_soundPath] error:&error];
    if (error) {
        self.pv_player = nil;
        return;
    }
    _currentTime = 0;
    if (self.pv_player.duration == 0) {
        self.pv_player = nil;
        return;
    }
    if (self.timeRange.duration == 0) {
        self.timeRange = SRSoundRangeMake(0, self.pv_player.duration);
    }
    self.pv_player.currentTime = self.timeRange.start;
    self.pv_player.delegate = self;
    [self.pv_player prepareToPlay];
}

- (void)deleteSound {
    self.timeRange = SRSoundRangeMake(0, 0);
    self.pv_player = nil;
    if ([[NSFileManager defaultManager] fileExistsAtPath:self.pv_soundPath]) {
        [[NSFileManager defaultManager] removeItemAtPath:self.pv_soundPath error:nil];
    }
}

- (void)play {
    if (!self.pv_player) {
        return;
    }
    if (self.currentTime == self.duration) {
        self.currentTime = 0;
        [self stop];
    }
    if (self.currentTime == 0) {
        if ([self.delegate respondsToSelector:@selector(soundDidStartPlaying:)]) {
            [self.delegate soundDidStartPlaying:self];
        }
    } else {
        if ([self.delegate respondsToSelector:@selector(soundDidContinue:)]) {
            [self.delegate soundDidContinue:self];
        }
    }
    self.pv_timer = [NSTimer scheduledTimerWithTimeInterval:0.1f target:self selector:@selector(pm_onPlayUpdate) userInfo:nil repeats:YES];
    [self.pv_player play];
}

- (void)pause {
    _currentTime = self.pv_player.currentTime - self.timeRange.start;
    [self.pv_timer invalidate];
    self.pv_timer = nil;
    [self.pv_player pause];
    if ([self.delegate respondsToSelector:@selector(soundDidPause:)]) {
        [self.delegate soundDidPause:self];
    }
}

- (void)pm_onRecordUpdate {
    self.pv_time += 0.1f;
    if ([self.delegate respondsToSelector:@selector(sound:recordingPosition:duration:)]) {
        [self.delegate sound:self recordingPosition:self.pv_time duration:kSRSoundRecordDuration];
    }
}

- (void)pm_onPlayUpdate {
    if ([self.delegate respondsToSelector:@selector(sound:playPosition:duration:)]) {
        [self.delegate sound:self playPosition:self.currentTime duration:self.duration];
    }
    _currentTime = self.pv_player.currentTime - self.timeRange.start;
    if (self.currentTime >= self.duration) {
        [self stop];
    }
}

#pragma mark - AVAudioRecorderDelegate

- (void)audioRecorderDidFinishRecording:(AVAudioRecorder*)recorder successfully:(BOOL)flag {
    [self.pv_timer invalidate];
    self.pv_timer = nil;
    self.pv_recorder = nil;
    [self prepareToPlay];
    if ([self.delegate respondsToSelector:@selector(soundDidEndRecording:)]) {
        [self.delegate soundDidEndRecording:self];
    }
}

#pragma mark - AVAudioPlayerDelegate

- (void)audioPlayerDidFinishPlaying:(AVAudioPlayer*)player successfully:(BOOL)flag {
    [self.pv_timer invalidate];
    self.pv_timer = nil;
    [self pm_onPlayUpdate];
    self.currentTime = 0;
    if ([self.delegate respondsToSelector:@selector(soundDidEndPlaying:)]) {
        [self.delegate soundDidEndPlaying:self];
    }
}

@end
