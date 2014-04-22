//  SEProject.m
//  SoundRecorder
//  Created by Igor Danich igor.danich@wise-apps.com on 11.04.14.


#import "SEProject.h"
#import "SERecord.h"
#import "SEProject+Internal.h"

@interface SEProject()
@property(nonatomic,strong) NSString*               pv_projectPath;
@property(nonatomic,strong) SEProjectAudioStream*   pv_stream;
@property(nonatomic,strong) NSMutableArray*         pv_records;
@property(nonatomic,assign) BOOL                    pv_isChanged;
- (void)pm_loadProject;
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
//            SERecordSoundRange range;
//            range.start = 18000;
//            range.duration = 10000;
//            record.soundRange = range;
//            [self addRecord:record];
//            
//            record = [self splitRecordInPosition:3.0f];
//            record.soundURL = [NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:@"2" ofType:@"wav"]];
//            range.start = 1500;
//            range.duration = 5000;
//            record.soundRange = range;
//            [self saveProjectInfo];
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

- (void)saveProjectInfo {
    [[self dictionaryRepresentation] writeToFile:self.projectFilePath atomically:YES];
}

#pragma mark - Public

- (void)clearProject {
    [[NSFileManager defaultManager] removeItemAtPath:self.projectSoundsPath error:nil];
    [self.pv_records removeAllObjects];
    [self saveProjectInfo];
}

- (NSError*)saveProject {
    NSUInteger duration = self.audioStream.duration*1000.0f;
    if (duration == 0) {
        return [NSError errorWithDomain:@"com.seproject" code:-1 userInfo:@{NSLocalizedDescriptionKey : @"Project is empty"}];
    }
    NSString* fileName = [NSString stringWithFormat:@"%d%d.wav", (int)time(NULL), arc4random()%99999];
    NSString* file = [self.projectSoundsPath stringByAppendingPathComponent:fileName];
    SEAudioStream* stream = [[SEAudioStream alloc] initWithAudioStream:self.audioStream];
    NSError* error = [stream exportToFile:file];
    error = [stream exportToFile:file];
    if (error) {
        [stream close];
        return error;
    }
    [stream close];
    for (SERecord* record in self.pv_records) {
        [record.audioStream close];
    }
    self.pv_records = nil;
    NSArray* files = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:self.projectSoundsPath error:nil];
    for (NSString* childFile in files) {
        if (![childFile isEqualToString:fileName]) {
            [[NSFileManager defaultManager] removeItemAtPath:[self.projectSoundsPath stringByAppendingPathComponent:childFile] error:nil];
        }
    }
    self.pv_records = nil;
    [self saveProjectInfo];
    SERecord* record = [SERecord new];
    record.soundURL = [NSURL fileURLWithPath:file];
    SERecordSoundRange range = record.soundRange;
    range.start = 0;
    range.duration = duration;
    record.soundRange = range;
    [record.project saveProjectInfo];
    [self addRecord:record];
    return nil;
}

- (void)saveProjectAsynchronouslyWithCompletion:(void(^)(NSError* error))completion {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
        NSError* error = [self saveProject];
        if (completion) {
            [[NSOperationQueue mainQueue] addOperationWithBlock:^{
                completion(error);
            }];
        }
    });
}

#pragma mark - Internal

- (SEProjectAudioStream*)audioStream {
    if (!self.pv_stream) {
        self.pv_stream = [[SEProjectAudioStream alloc] initWithProject:self];
    }
    return self.pv_stream;
}

- (SERecord*)splitRecordInPosition:(NSTimeInterval)position {
    if (([self.records count] == 0)||(position == self.audioStream.duration)) {
        SERecord* record = [SERecord new];
        [self addRecord:record];
        return record;
    }
    NSUInteger pos = 0;
    NSUInteger splitPos = position*1000;
    NSUInteger sTime = 0;
    NSInteger index = 0;
    SERecord* cRecord = nil;
    BOOL split = YES;
    for (SERecord* record in self.records) {
        cRecord = record;
        if (pos + record.soundRange.duration == splitPos) {
            split = NO;
            index = [self.pv_records indexOfObject:record] + 1;
            break;
        } else if (pos + record.soundRange.duration > splitPos) {
            index = [self.pv_records indexOfObject:record];
            split = YES;
            sTime = splitPos - pos;
            break;
        }
        pos += record.soundRange.duration;
    }
    SERecord* record = [SERecord new];
    if (split) {
        [self.pv_records removeObjectAtIndex:index];
        SERecord* pRec = [SERecord new];
        SERecordSoundRange range;
        range.start = cRecord.soundRange.start;
        range.duration = sTime;
        pRec.soundRange = range;
        pRec.soundURL = cRecord.soundURL;
        [self insertRecord:pRec toIndex:index];
        index++;
        [self insertRecord:record toIndex:index];
        index++;
        SERecord* nRec = [SERecord new];
        range.start = cRecord.soundRange.start + sTime;
        range.duration = cRecord.soundRange.duration - sTime;
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
    [self saveProjectInfo];
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
    [self saveProjectInfo];
}

@end
