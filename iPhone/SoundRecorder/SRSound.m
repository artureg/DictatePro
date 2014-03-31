//
//  SRSound.m
//  SoundRecorder
//
//  Created by Igor on 12/13/13.
//  Copyright (c) 2013 Igor. All rights reserved.
//

#import "SRSound.h"

static const NSTimeInterval kSRSoundRecordDuration = 3*60.0f;

@interface SRSound()<AVAudioRecorderDelegate,AVAudioPlayerDelegate>
@property(nonatomic,strong) AVAudioPlayer*      pv_player;
@property(nonatomic,strong) AVAudioRecorder*    pv_recorder;
@property(nonatomic,strong) NSString*           pv_soundName;
@property(nonatomic,assign) NSTimeInterval      pv_time;
@property(nonatomic,strong) NSTimer*            pv_timer;
- (NSString*)pm_soundPath;
- (void)pm_onRecordUpdate;
- (void)pm_onPlayUpdate;
@end

@implementation SRSound

- (void)dealloc {
    [self.pv_timer invalidate];
    self.pv_timer = nil;
}

- (id)initWithFileName:(NSString*)fileName {
    if (self = [super init]) {
        self.pv_soundName = fileName;
        [self prepareToPlay];
    }
    return self;
}

- (BOOL)canPlay {
    return (self.pv_player != nil);
}

- (NSTimeInterval)currentTime {
    return self.pv_player.currentTime;
}

- (BOOL)isPlaying {
    return self.pv_player.isPlaying;
}

- (BOOL)isRecording {
    return self.pv_recorder.isRecording;
}

- (NSString*)fileName {
    return self.pv_soundName;
}

- (NSTimeInterval)duration {
    if ([self canPlay]) {
        return self.pv_player.duration;
    } else {
        return 0;
    }
}

- (NSString*)pm_soundPath {
    if (!self.pv_soundName) {
        self.pv_soundName = [NSString stringWithFormat:@"%@.caf", @(time(NULL))];
    }
    return [[documentsFolderPath() stringByAppendingPathComponent:@"Sounds"] stringByAppendingPathComponent:self.pv_soundName];
}

- (void)record {
    NSLog(@"record");
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
        AVSampleRateKey : @(44000.0),
        AVNumberOfChannelsKey : @(2),
        AVLinearPCMBitDepthKey : @(16),
        AVEncoderAudioQualityKey : @(AVAudioQualityMedium)
    };
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:[self pm_soundPath]]) {
        NSError* error = nil;
        [[NSFileManager defaultManager] removeItemAtPath:[self pm_soundPath] error:&error];
        if (error) {
            NSLog(@"%@", [error localizedDescription]);
        }
    }
    
    NSURL* url = [NSURL fileURLWithPath:[self pm_soundPath]];
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
    [self.delegate soundDidStartRecording:self];
}

- (void)stop {
    if (self.pv_recorder) {
        [self.pv_timer invalidate];
        self.pv_timer = nil;
        if (!self.pv_recorder.isRecording) {
            return;
        }
        [self.pv_recorder stop];
        NSURL* url = [NSURL fileURLWithPath:[self pm_soundPath]];
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
        [audioData writeToFile:[self pm_soundPath] atomically:YES];
        self.pv_recorder = nil;
    } else {
        [self.pv_player stop];
        self.pv_player.currentTime = 0;
        [self.pv_timer invalidate];
        self.pv_timer = nil;
        [self.delegate soundDidEndPlaying:self];
    }
}

- (void)prepareToPlay {
    NSError* error = nil;
    self.pv_player = [[AVAudioPlayer alloc] initWithContentsOfURL:[NSURL fileURLWithPath:[self pm_soundPath]] error:&error];
    if (error) {
        self.pv_player = nil;
        [self deleteSound];
        return;
    }
    if (self.pv_player.duration == 0) {
        self.pv_player = nil;
        [self deleteSound];
        return;
    }
    self.pv_player.delegate = self;
    [self.pv_player prepareToPlay];
}

- (void)deleteSound {
    self.pv_player = nil;
    if ([[NSFileManager defaultManager] fileExistsAtPath:[self pm_soundPath]]) {
        [[NSFileManager defaultManager] removeItemAtPath:[self pm_soundPath] error:nil];
    }
    self.pv_soundName = nil;
}

- (void)play {
    if (!self.pv_player) {
        return;
    }
    if (self.pv_player.currentTime == 0) {
        [self.delegate soundDidStartPlaying:self];
    } else {
        [self.delegate soundDidContinue:self];
    }
    self.pv_timer = [NSTimer scheduledTimerWithTimeInterval:0.1f target:self selector:@selector(pm_onPlayUpdate) userInfo:nil repeats:YES];
    [self.pv_player play];
}

- (void)pause {
    [self.pv_timer invalidate];
    self.pv_timer = nil;
    [self.pv_player pause];
    [self.delegate soundDidPause:self];
}

- (void)pm_onRecordUpdate {
    self.pv_time += 0.1f;
    [self.delegate sound:self recordingPosition:self.pv_time duration:kSRSoundRecordDuration];
}

- (void)pm_onPlayUpdate {
    [self.delegate sound:self playPosition:self.pv_player.currentTime duration:self.pv_player.duration];
}

#pragma mark - AVAudioRecorderDelegate

- (void)audioRecorderDidFinishRecording:(AVAudioRecorder*)recorder successfully:(BOOL)flag {
    [self.pv_timer invalidate];
    self.pv_timer = nil;
    self.pv_recorder = nil;
    [self prepareToPlay];
    [self.delegate soundDidEndRecording:self];
}

#pragma mark - AVAudioPlayerDelegate

- (void)audioPlayerDidFinishPlaying:(AVAudioPlayer*)player successfully:(BOOL)flag {
    [self.pv_timer invalidate];
    self.pv_timer = nil;
    [self.delegate soundDidEndPlaying:self];
}

@end
