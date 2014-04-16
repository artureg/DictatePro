//
//  SEProject.m
//  SoundRecorder
//
//  Created by Igor on 11.04.14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SEProject.h"
#import "SERecord.h"
#import "SEProject+Internal.h"

@interface SEProject()
@property(nonatomic,strong) NSString*               pv_projectPath;
@property(nonatomic,strong) SEProjectAudioStream*   pv_stream;
@property(nonatomic,strong) NSMutableArray*         pv_records;
@property(nonatomic,assign) BOOL                    pv_isChanged;
- (void)pm_loadProject;
- (void)pm_saveProject;
@end

@implementation SEProject

- (void)dealloc {
    [self.pv_stream close];
}

- (instancetype)initWithFolder:(NSString*)folder {
    if (self = [super init]) {
        self.pv_projectPath = folder;
        [self pm_loadProject];
//        [self clearProject];
//        if ([self.pv_records count] == 0) {
//            SERecord* record = [SERecord new];
//            record.soundURL = [NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:@"1" ofType:@"wav"]];
//            [self addRecord:record];
//        }
    }
    return self;
}

#pragma mark - Load/Save

- (NSString*)projectPath {
    return self.pv_projectPath;
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
        SERecord* record = [[SERecord alloc] initWithDictionary:recordDict];
        record.project = self;
        [self.pv_records addObject:record];
    }
}

- (NSMutableDictionary*)dictionaryRepresentation {
    NSMutableDictionary* dict = [NSMutableDictionary dictionary];
    NSMutableArray* records = [NSMutableArray array];
    for (SERecord* record in self.pv_records) {
        [records addObject:[record dictionaryRepresentation]];
    }
    dict[@"isChanged"] = @(self.isChanged);
    dict[@"records"] = records;
    return dict;
}

- (NSArray*)records {
    return self.pv_records;
}

- (void)pm_loadProject {
    [self updateToDictionary:[NSDictionary dictionaryWithContentsOfFile:self.projectFilePath]];
    if (![[NSFileManager defaultManager] fileExistsAtPath:self.projectSoundPath]) {
        self.pv_isChanged = YES;
    }
}

- (void)pm_saveProject {
    [[self dictionaryRepresentation] writeToFile:self.projectFilePath atomically:YES];
}

#pragma mark - Public

- (void)clearProject {
    [[NSFileManager defaultManager] removeItemAtPath:self.projectSoundsPath error:nil];
    [self.pv_records removeAllObjects];
    [self pm_saveProject];
}

- (void)saveProject {
}

- (void)saveProjectAsynchronouslyWithCompletion:(void(^)(NSError* error))completion {
}

#pragma mark - Internal

- (SEProjectAudioStream*)audioStream {
    if (!self.pv_stream) {
        self.pv_stream = [[SEProjectAudioStream alloc] initWithProject:self];
    }
    return self.pv_stream;
}

- (SERecord*)splitRecordInPosition:(NSTimeInterval)position {
    if (([self.records count] == 0)||(position = self.audioStream.duration)) {
        SERecord* record = [SERecord new];
        [self addRecord:record];
        return record;
    }
    NSTimeInterval rTime = 0;
    NSTimeInterval lTime = 0;
    SERecord* cRecord = nil;
    BOOL split = YES;
    for (SERecord* record in self.records) {
        cRecord = record;
        if (rTime + record.audioStream.duration < position) {
        } else if (rTime == position) {
            split = NO;
            break;
        } else {
            split = YES;
            lTime = rTime + record.audioStream.duration - position;
            break;
        }
    }
    SERecord* record = [SERecord new];
    NSInteger index = [self.records indexOfObject:cRecord];
    if (split) {
        [self.pv_records removeObjectAtIndex:index];
        SERecord* pRec = [SERecord new];
        SERecordSoundRange range;
        range.start = cRecord.soundRange.start;
        range.duration = lTime;
        pRec.soundRange = range;
        pRec.soundURL = cRecord.soundURL;
        [self insertRecord:pRec toIndex:index];
        index++;
        [self insertRecord:record toIndex:index];
        index++;
        SERecord* nRec = [SERecord new];
        range.start = cRecord.soundRange.start + lTime;
        range.duration = cRecord.soundRange.duration - lTime;
        nRec.soundRange = range;
        nRec.soundURL = cRecord.soundURL;
        [self insertRecord:nRec toIndex:index];
    } else {
        [self insertRecord:record toIndex:index];
    }
    return record;
}

- (void)addRecord:(SERecord*)record {
    [self insertRecord:record toIndex:[self.pv_records count]];
}

- (void)deleteRecord:(SERecord*)record {
    [self.pv_records removeObject:record];
    [self pm_saveProject];
}

- (void)insertRecord:(SERecord*)record toIndex:(NSInteger)index {
    if (!self.pv_records) {
        self.pv_records = [NSMutableArray array];
    }
    record.project = self;
    if (index < [self.pv_records count]) {
        [self.pv_records insertObject:record atIndex:index];
    } else {
        [self.pv_records addObject:record];
    }
    [self pm_saveProject];
}

@end
