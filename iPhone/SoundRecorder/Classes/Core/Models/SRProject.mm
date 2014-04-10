//
//  SRProject.m
//  SoundRecorder
//
//  Created by Igor on 4/1/14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SRProject.h"

#include "SpeexACMConvert.h"

@interface SRProject()
@property(nonatomic,strong) NSMutableArray* pv_records;
@property(nonatomic,strong) NSMutableArray* pv_sounds;
@property(nonatomic,strong) SRSound*        pv_sound;
@property(nonatomic,assign) BOOL            pv_isChanged;
- (void)pm_loadProject;
- (void)pm_saveProject;
@end

@implementation SRProject

- (id)init {
    if (self = [super init]) {
        [self pm_loadProject];
    }
    return self;
}

#pragma mark - Load/Save

- (NSString*)projectPath {
    return NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES)[0];
}

- (NSString*)projectFilePath {
    return [self.projectPath stringByAppendingPathComponent:@"Project.plist"];
}

- (NSString*)projectSoundsPath {
    NSString* filePath = [self.projectPath stringByAppendingPathComponent:@"Sounds"];
    if (![[NSFileManager defaultManager] fileExistsAtPath:filePath]) {
        [[NSFileManager defaultManager] createDirectoryAtPath:filePath withIntermediateDirectories:YES attributes:nil error:nil];
    }
    return filePath;
}

- (NSString*)projectSoundPath {
    return [self.projectPath stringByAppendingPathComponent:@"Project.wav"];
}

- (NSString*)projectSoundSpeexPath {
    return [self.projectPath stringByAppendingPathComponent:@"Project_spx.wav"];
}

- (void)updateToDictionary:(NSDictionary*)dictionary {
    self.name = dictionary[@"name"];
    self.pv_records = [NSMutableArray array];
    self.pv_isChanged = [dictionary[@"isChanged"] boolValue];
    for (NSDictionary* recordDict in dictionary[@"records"]) {
        SRRecord* record = [[SRRecord alloc] initWithDictionary:recordDict];
        record.project = self;
        [self.pv_records addObject:record];
    }
}

- (NSMutableDictionary*)dictionaryRepresentation {
    NSMutableDictionary* dict = [NSMutableDictionary dictionary];
    NSMutableArray* records = [NSMutableArray array];
    for (SRRecord* record in self.pv_records) {
        [records addObject:[record dictionaryRepresentation]];
    }
    dict[@"isChanged"] = @(self.isChanged);
    dict[@"records"] = records;
    return dict;
}

- (NSArray*)records {
    return self.pv_records;
}

- (NSArray*)sounds {
    return self.pv_sounds;
}

- (void)pm_loadProject {
    [self updateToDictionary:[NSDictionary dictionaryWithContentsOfFile:self.projectFilePath]];
    self.pv_sounds = [NSMutableArray array];
    NSArray* sounds = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:self.projectSoundsPath error:nil];
    for (NSString* soundName in sounds) {
        [self.pv_sounds addObject:[[SRSound alloc] initWithFilePath:[self.projectSoundsPath stringByAppendingPathComponent:soundName]]];
    }
    if (![[NSFileManager defaultManager] fileExistsAtPath:self.projectSoundPath]) {
        self.pv_isChanged = YES;
    }
}

- (void)pm_saveProject {
    [[self dictionaryRepresentation] writeToFile:self.projectFilePath atomically:YES];
}

#pragma mark - Recording

- (void)startRecording {
}

- (void)stopRecording {
}

- (SRSound*)soundWithPath:(NSString*)path {
    for (SRSound* sound in self.pv_sounds) {
        if ([sound.filePath isEqualToString:path]) {
            return sound;
        }
    }
    SRSound* sound = [[SRSound alloc] initWithFilePath:path];
    if (!sound) {
        return nil;
    }
    if (!self.pv_sounds) {
        self.pv_sounds = [NSMutableArray array];
    }
    [self.pv_sounds addObject:sound];
    return sound;
}

#pragma mark - Edit Records

- (void)splitRecord:(SRRecord*)record inPosition:(NSTimeInterval)position {
    SRRecord* aRecord = [SRRecord new];
    aRecord.soundPath = record.soundPath;
    SRSoundRange range;
    range.start = record.timeRange.start + position;
    range.duration = record.timeRange.duration - position;
    aRecord.timeRange = range;
    aRecord.project = self;
    [self.pv_records insertObject:aRecord atIndex:[self.pv_records indexOfObject:record] + 1];
    range.start = record.timeRange.start;
    range.duration = position;
    record.timeRange = range;
    self.pv_isChanged = YES;
    [self pm_saveProject];
}

- (void)addRecord:(SRRecord*)record {
    if (!self.pv_records) {
        self.pv_records = [NSMutableArray array];
    }
    record.project = self;
    [self.pv_records addObject:record];
    [self addSound:record.sound];
    self.pv_isChanged = YES;
    [self pm_saveProject];
}

- (void)deleteRecord:(SRRecord*)record {
    [self.pv_records removeObject:record];
    self.pv_isChanged = YES;
    [self pm_saveProject];
}

- (void)moveRecord:(SRRecord*)record toIndex:(NSInteger)index {
    [self.pv_records exchangeObjectAtIndex:[self.pv_records indexOfObject:record] withObjectAtIndex:index];
    self.pv_isChanged = YES;
    [self pm_saveProject];
}

#pragma mark - Edit Sounds

- (void)addSound:(SRSound*)sound {
    if (!self.pv_sounds) {
        self.pv_sounds = [NSMutableArray array];
    }
    BOOL exists = NO;
    for (SRSound* aSound in self.pv_sounds) {
        if ([aSound.filePath isEqualToString:sound.filePath]) {
            exists = YES;
            break;
        }
    }
    if (!exists) {
        if (!self.pv_sounds) {
            self.pv_sounds = [NSMutableArray array];
        }
        [self.pv_sounds addObject:sound];
    }
}

- (void)deleteSound:(SRSound*)sound {
    if ([sound.filePath rangeOfString:self.projectPath].location == NSNotFound) {
        return;
    }
    NSMutableArray* remove = [NSMutableArray array];
    for (SRRecord* record in self.pv_records) {
        if ([record.soundPath isEqualToString:sound.filePath]) {
            [remove addObject:record];
        }
    }
    [self.pv_records removeObjectsInArray:remove];
    [sound deleteSound];
    [self.pv_sounds removeObject:sound];
    self.pv_isChanged = YES;
    [self pm_saveProject];
}

#pragma mark - Clear

- (void)clearAll {
    self.pv_records = nil;
    [self.pv_sounds makeObjectsPerformSelector:@selector(setDelegate:) withObject:nil];
    self.pv_sounds = nil;
    [[NSFileManager defaultManager] removeItemAtPath:self.projectSoundsPath error:nil];
    self.pv_isChanged = YES;
    [self pm_saveProject];
}

- (void)clearSound {
    self.pv_isChanged = YES;
    self.pv_sound = nil;
    [[NSFileManager defaultManager] removeItemAtPath:self.projectSoundPath error:nil];
    [[NSFileManager defaultManager] removeItemAtPath:self.projectSoundSpeexPath error:nil];
}

#pragma mark - Building

- (SRSound*)projectSound {
    if (!self.pv_sound) {
        if ([[NSFileManager defaultManager] fileExistsAtPath:self.projectSoundPath]) {
            self.pv_sound = [[SRSound alloc] initWithFilePath:self.projectSoundPath];
        }
    }
    return self.pv_sound;
}

- (BOOL)isChanged {
    return self.pv_isChanged;
}

- (NSTimeInterval)duration {
    NSTimeInterval duration = 0;
    for (SRRecord* record in self.pv_records) {
        duration += record.timeRange.duration;
    }
    return duration;
}

- (void)buildProjectWithCompletion:(void(^)(NSError* error))completion {
//    [self clearSound];
    if (![[NSFileManager defaultManager] fileExistsAtPath:self.projectFilePath]) {
        self.pv_isChanged = YES;
    }
    if ((self.duration == 0)||(!self.isChanged)) {
        completion(nil);
        return;
    }
    [self clearSound];
    AVMutableComposition* mixComposition = [AVMutableComposition composition];
    AVMutableCompositionTrack* compositionTrack = [mixComposition addMutableTrackWithMediaType:AVMediaTypeAudio preferredTrackID:kCMPersistentTrackID_Invalid];
    NSError* error = nil;
    NSMutableArray* timeRanges = [NSMutableArray array];
    NSMutableArray* tracks = [NSMutableArray array];
    for (NSInteger i = 0; i < [self.pv_records count]; i++) {
        SRRecord* record = self.pv_records[i];
        AVAsset* assetClip = [AVAsset assetWithURL:[NSURL fileURLWithPath:record.soundPath]];
        AVAssetTrack* audioTrack = [assetClip tracksWithMediaType:AVMediaTypeAudio][0];
        [timeRanges addObject:[NSValue valueWithCMTimeRange:record.assetTimeRange]];
        [tracks addObject:audioTrack];
    }
    [compositionTrack insertTimeRanges:timeRanges ofTracks:tracks atTime:kCMTimeZero error:&error];
    if (error) {
        if (completion) {
            completion(error);
        }
        return;
    }
    
    AVAssetReader* assetReader = [AVAssetReader assetReaderWithAsset:mixComposition error:&error];
    if (error) {
        if (completion) {
            completion(error);
        }
        return;
    }
    AVAssetTrack* audioTrack = mixComposition.tracks[0];
    
    AVAssetReaderOutput* assetReaderOutput = [AVAssetReaderTrackOutput assetReaderTrackOutputWithTrack:audioTrack outputSettings:nil];
    if(![assetReader canAddOutput:assetReaderOutput]) {
        if (completion) {
            completion([NSError errorWithDomain:@"com.soundeditor" code:1 userInfo:@{NSLocalizedDescriptionKey: @"Can't add output"}]);
        }
        return;
    }
    [assetReader addOutput:assetReaderOutput];
    
    AVAssetWriter* assetWriter = [AVAssetWriter assetWriterWithURL:[NSURL fileURLWithPath:self.projectSoundPath] fileType:AVFileTypeWAVE error:&error];
    if (error) {
        if (completion) {
            completion(error);
        }
        return;
    }
    AudioChannelLayout channelLayout;
    memset(&channelLayout, 0, sizeof(AudioChannelLayout));
    channelLayout.mChannelLayoutTag = kAudioChannelLayoutTag_Mono;
    NSDictionary* settings = @{
        AVFormatIDKey               : @(kAudioFormatLinearPCM),
        AVSampleRateKey             : @(16000.0),
        AVNumberOfChannelsKey       : @(1),
        AVLinearPCMBitDepthKey      : @(16),
        AVLinearPCMIsFloatKey       : @(NO),
        AVLinearPCMIsBigEndianKey   : @(NO),
        AVLinearPCMIsNonInterleaved : @(NO)
    };
    AVAssetWriterInput *assetWriterInput = [AVAssetWriterInput assetWriterInputWithMediaType:AVMediaTypeAudio outputSettings:settings];
    if([assetWriter canAddInput:assetWriterInput]) {
        [assetWriter addInput:assetWriterInput];
    } else {
        if (completion) {
            completion([NSError errorWithDomain:@"com.soundeditor" code:1 userInfo:@{NSLocalizedDescriptionKey: @"Can't add asset writer input"}]);
        }
        return;
    }
    assetWriterInput.expectsMediaDataInRealTime = NO;
    [assetWriter startWriting];
    [assetReader startReading];
    CMTime startTime = CMTimeMake(0, audioTrack.naturalTimeScale);
    [assetWriter startSessionAtSourceTime:startTime];
    
    dispatch_queue_t mediaInputQueue = dispatch_queue_create("mediaInputQueue",NULL);
    [assetWriterInput requestMediaDataWhenReadyOnQueue:mediaInputQueue usingBlock:^ {
        while(assetWriterInput.readyForMoreMediaData) {
            CMSampleBufferRef nextBuffer = [assetReaderOutput copyNextSampleBuffer];
            if(nextBuffer) {
                [assetWriterInput appendSampleBuffer:nextBuffer];
            } else {
                [assetWriterInput markAsFinished];
                [assetWriter finishWritingWithCompletionHandler:^{
                    [[NSOperationQueue mainQueue] addOperationWithBlock:^{
                        if (assetWriter.error) {
                            [self clearSound];
                            if (completion) {
                                completion([NSError errorWithDomain:@"com.soundeditor" code:1 userInfo:@{NSLocalizedDescriptionKey: @"Can't Play File"}]);
                            }
                        } else {
                            self.pv_sound = [[SRSound alloc] initWithFilePath:self.projectSoundPath];
                            if (self.pv_sound.canPlay) {
                                self.pv_isChanged = NO;
                                [self pm_saveProject];
                                if (completion) {
                                    completion(nil);
                                }
                            } else {
                                [self clearSound];
                                if (completion) {
                                    completion([NSError errorWithDomain:@"com.soundeditor" code:1 userInfo:@{NSLocalizedDescriptionKey: @"Can't Play File"}]);
                                }
                            }
                        }
                    }];
                }];
                [assetReader cancelReading];
                break;
            }
        }
    }];
}

- (void)encodeToSpeexACMWithCompletion:(void(^)(NSError* error))completion {
    if (![[NSFileManager defaultManager] fileExistsAtPath:self.projectSoundPath]) {
        if (completion) {
            completion([NSError errorWithDomain:nil code:-1 userInfo:@{NSLocalizedDescriptionKey: @"No Input File"}]);
        }
        return;
    }
    if ([[NSFileManager defaultManager] fileExistsAtPath:self.projectSoundSpeexPath]) {
        [[NSFileManager defaultManager] removeItemAtPath:self.projectSoundSpeexPath error:nil];
    }
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
        bool ready = encodeWavToSpeexACM([self.projectSoundPath cStringUsingEncoding:NSASCIIStringEncoding], [self.projectSoundSpeexPath cStringUsingEncoding:NSASCIIStringEncoding]);
        [[NSOperationQueue mainQueue] addOperationWithBlock:^{
            if (completion) {
                if ((!ready)||(![[NSFileManager defaultManager] fileExistsAtPath:self.projectSoundSpeexPath])) {
                    completion([NSError errorWithDomain:@"com.soundeditor" code:1 userInfo:@{NSLocalizedDescriptionKey: @"Error while encoding"}]);
                } else {
                    completion(nil);
                }
            }
        }];
    });
}

@end
