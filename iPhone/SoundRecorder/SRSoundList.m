//
//  SRSoundList.m
//  SoundRecorder
//
//  Created by Igor on 12/13/13.
//  Copyright (c) 2013 Igor. All rights reserved.
//

#import "SRSoundList.h"
#import <AVFoundation/AVFoundation.h>

@interface SRSoundList()<AVAudioPlayerDelegate>
@property(nonatomic,strong) NSMutableArray* pv_sounds;
@property(nonatomic,assign) BOOL            pv_isPlaying;
@property(nonatomic,strong) NSTimer*        pv_timer;
@property(nonatomic,strong) AVAudioPlayer*  pv_player;
- (NSString*)pm_soundPath;
- (void)pm_preparePlayer;
- (void)pm_onTimer;
- (void)pm_splitSound:(SRSound*)sound startTime:(NSTimeInterval)timeStart endTime:(NSTimeInterval)timeEnd completion:(void(^)(SRSound* newSound))completion;
@end

@implementation SRSoundList

- (id)init {
    if (self = [super init]) {
        NSString* soundsPath = [documentsFolderPath() stringByAppendingPathComponent:@"Sounds"];
        if (![[NSFileManager defaultManager] fileExistsAtPath:soundsPath]) {
            [[NSFileManager defaultManager]
                createDirectoryAtPath:soundsPath
                withIntermediateDirectories:YES
                attributes:nil
                error:nil];
        }
        NSArray* array = [NSArray arrayWithContentsOfFile:[documentsFolderPath() stringByAppendingPathComponent:@"Info.plist"]];
        for (NSString* file in array) {
            SRSound* sound = [[SRSound alloc] initWithFileName:file];
            [sound prepareToPlay];
            if (sound.canPlay) {
                [self addSound:sound];
            }
        }
        [self pm_preparePlayer];
    }
    return self;
}

- (NSString*)pm_soundPath {
    return [documentsFolderPath() stringByAppendingPathComponent:@"Sound.caf"];
}

- (BOOL)isPlaying {
    return self.pv_isPlaying;
}

- (NSTimeInterval)duration {
    if (self.pv_player) {
        return self.pv_player.duration;
    } else {
        NSTimeInterval duration = 0;
        for (SRSound* sound in self.pv_sounds) {
            duration += sound.duration;
        }
        return duration;
    }
}

- (NSArray*)sounds {
    return self.pv_sounds;
}

- (void)clearAll {
    NSArray* files = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:documentsFolderPath() error:nil];
    for (NSString* file in files) {
        [[NSFileManager defaultManager] removeItemAtPath:[documentsFolderPath() stringByAppendingPathComponent:file] error:nil];
    }
    [[NSFileManager defaultManager]
        createDirectoryAtPath:[documentsFolderPath() stringByAppendingPathComponent:@"Sounds"]
        withIntermediateDirectories:YES
        attributes:nil
        error:nil];
    self.pv_sounds = nil;
    [self save];
}

- (void)addSound:(SRSound*)sound {
    if (!self.pv_sounds) {
        self.pv_sounds = [NSMutableArray array];
    }
    [self.pv_sounds addObject:sound];
    [self save];
}

- (void)removeSound:(SRSound*)sound {
    [self.pv_sounds removeObject:sound];
    [self save];
}

- (void)moveSound:(SRSound*)sound toIndex:(NSInteger)index {
    [self.pv_sounds exchangeObjectAtIndex:[self.pv_sounds indexOfObject:sound] withObjectAtIndex:index];
    [self save];
}

- (void)splitSound:(SRSound*)sound time:(NSTimeInterval)time {
    [self pm_splitSound:sound startTime:0 endTime:time completion:^(SRSound *newSound1) {
        SRSound* sound1 = newSound1;
        [sound1 prepareToPlay];
        [self pm_splitSound:sound startTime:time endTime:sound.duration completion:^(SRSound *newSound2) {
            SRSound* sound2 = newSound2;
            [sound2 prepareToPlay];
            NSInteger index = [self.pv_sounds indexOfObject:sound];
            [sound deleteSound];
            [self.pv_sounds removeObject:sound];
            if (sound1.duration > 0) {
                [self.pv_sounds insertObject:sound1 atIndex:index];
                if (sound2.duration > 0) {
                    [self.pv_sounds insertObject:sound2 atIndex:index + 1];
                }
            } else if (sound2.duration > 0) {
                [self.pv_sounds insertObject:sound2 atIndex:index];
            }
            [self save];
            [self.delegate soundListDidEndSplit:self sound1:sound1 sound2:sound2];
        }];
    }];
}

- (void)pm_splitSound:(SRSound*)sound startTime:(NSTimeInterval)timeStart endTime:(NSTimeInterval)timeEnd completion:(void(^)(SRSound* newSound))completion {
    NSString* fileName = [NSString stringWithFormat:@"%@%@.caf", @(time(NULL)), @(arc4random()%9999999999)];
    NSString* path = [[documentsFolderPath() stringByAppendingPathComponent:@"Sounds"] stringByAppendingPathComponent:fileName];
    CMTime trimStart = CMTimeMakeWithSeconds(timeStart, 1000);
    CMTime trimEnd = CMTimeMakeWithSeconds(timeEnd, 1000);
    
    NSString* soundPath = [[documentsFolderPath() stringByAppendingPathComponent:@"Sounds"] stringByAppendingPathComponent:sound.fileName];
    AVAsset* songAsset = [AVAsset assetWithURL:[NSURL fileURLWithPath:soundPath]];
    
    AVAssetExportSession *exportSession = [AVAssetExportSession
        exportSessionWithAsset:songAsset
        presetName:AVAssetExportPresetAppleM4A];
    
    exportSession.outputURL = [NSURL fileURLWithPath:path];
    exportSession.outputFileType = AVFileTypeAppleM4A;
    exportSession.timeRange = CMTimeRangeFromTimeToTime(trimStart, trimEnd);
    
    [exportSession exportAsynchronouslyWithCompletionHandler:^(void) {
        [[NSOperationQueue mainQueue] addOperationWithBlock:^{
            SRSound* newSound = [[SRSound alloc] initWithFileName:fileName];
            if (completion) {
                completion(newSound);
            }
        }];
    }];
}

- (void)save {
    NSMutableArray* strings = [NSMutableArray array];
    [self.pv_sounds enumerateObjectsUsingBlock:^(SRSound* obj, NSUInteger idx, BOOL *stop) {
        [strings addObject:obj.fileName];
    }];
    [strings writeToFile:[documentsFolderPath() stringByAppendingPathComponent:@"Info.plist"] atomically:YES];
}

- (void)clearSound {
    self.pv_player = nil;
    if ([[NSFileManager defaultManager] fileExistsAtPath:[self pm_soundPath]]) {
        [[NSFileManager defaultManager] removeItemAtPath:[self pm_soundPath] error:nil];
    }
}

- (void)play {
    [self.pv_timer invalidate];
    self.pv_timer = nil;
    self.pv_isPlaying = YES;
    if (self.pv_player) {
        self.pv_timer = [NSTimer scheduledTimerWithTimeInterval:0.1f target:self selector:@selector(pm_onTimer) userInfo:nil repeats:YES];
        [self.delegate soundListDidStartPlaying:self];
        [self.pv_player play];
    } else {
        [self clearSound];
        [self.delegate soundListDidStartPreparing:self];
        AVMutableComposition* mixComposition = [AVMutableComposition composition];
        AVMutableCompositionTrack* compositionTrack = [mixComposition addMutableTrackWithMediaType:AVMediaTypeAudio preferredTrackID:kCMPersistentTrackID_Invalid];
        NSError* error = nil;
        NSMutableArray* timeRanges = [NSMutableArray array];
        NSMutableArray* tracks = [NSMutableArray array];
        
        for (NSInteger i = 0; i < [self.pv_sounds count]; i++) {
            SRSound* sound = self.pv_sounds[i];
            NSString* filePath = [[documentsFolderPath() stringByAppendingPathComponent:@"Sounds"] stringByAppendingPathComponent:sound.fileName];
            AVAsset* assetClip = [AVAsset assetWithURL:[NSURL fileURLWithPath:filePath]];
            AVAssetTrack* audioTrack = [assetClip tracksWithMediaType:AVMediaTypeAudio][0];
            [timeRanges addObject:[NSValue valueWithCMTimeRange:CMTimeRangeMake(kCMTimeZero, assetClip.duration)]];
            [tracks addObject:audioTrack];
        }
        [compositionTrack insertTimeRanges:timeRanges ofTracks:tracks atTime:kCMTimeZero error:&error];
        AVAssetExportSession* exportSession = [AVAssetExportSession
            exportSessionWithAsset:mixComposition
            presetName:AVAssetExportPresetAppleM4A];
        exportSession.outputURL = [NSURL fileURLWithPath:[self pm_soundPath]];
        exportSession.outputFileType = AVFileTypeAppleM4A;
        [exportSession exportAsynchronouslyWithCompletionHandler:^(void) {
            [[NSOperationQueue mainQueue] addOperationWithBlock:^{
                [self pm_preparePlayer];
                if (self.pv_player) {
                    [self play];
                } else {
                    self.pv_isPlaying = NO;
                    [self clearSound];
                    [self.delegate soundListDidEndPlaying:self];
                }
            }];
        }];
    }
}

- (void)stop {
    self.pv_isPlaying = NO;
    [self.pv_timer invalidate];
    self.pv_timer = nil;
    [self.pv_player stop];
    self.pv_player.currentTime = 0;
    [self.delegate soundListDidEndPlaying:self];
}

- (void)pm_preparePlayer {
    NSError* error = nil;
    self.pv_player = [[AVAudioPlayer alloc] initWithContentsOfURL:[NSURL fileURLWithPath:[self pm_soundPath]] error:&error];
    if (error) {
        self.pv_player = nil;
        return;
    }
    if (self.pv_player.duration == 0) {
        self.pv_player = nil;
        return;
    }
    self.pv_player.delegate = self;
    [self.pv_player prepareToPlay];
}

- (void)pm_onTimer {
    [self.delegate soundList:self playingTimer:self.pv_player.currentTime duration:self.pv_player.duration];
}

#pragma mark - AVAudioPlayerDelegate

- (void)audioPlayerDidFinishPlaying:(AVAudioPlayer*)player successfully:(BOOL)flag {
    [self.pv_timer invalidate];
    self.pv_timer = nil;
    self.pv_isPlaying = NO;
    [self.delegate soundListDidEndPlaying:self];
}

@end
