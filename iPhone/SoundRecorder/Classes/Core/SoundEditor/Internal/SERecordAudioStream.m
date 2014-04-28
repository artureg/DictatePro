//  SERecordAudioStream.m
//  SoundRecorder//  Created by Igor Danich igor.danich@wise-apps.com on 11.04.14.

#import "SERecordAudioStream.h"

#import "SERecord.h"
#import "SEProject.h"
#import "SEProject+Internal.h"

@interface SERecordAudioStream()
@property(nonatomic,weak) SERecord* pv_record;
@end

@implementation SERecordAudioStream

- (instancetype)initWithRecord:(SERecord*)record {
    NSString* path = [[record.soundURL resourceSpecifier] stringByReplacingOccurrencesOfString:@"%20" withString:@" "];
    if (![[NSFileManager defaultManager] fileExistsAtPath:path]) {
        path = [NSString stringWithFormat:@"%@/%@%@.wav", record.project.projectSoundsPath, @((NSInteger)time(NULL)), @(arc4random()%999999)];
        record.soundURL = [NSURL fileURLWithPath:path];
        [record.project saveProjectInfo];
    }
    if (self = [super initWithContentsOfFile:path]) {
        self.pv_record = record;
    }
    return self;
}

- (SERecord*)record {
    return self.pv_record;
}

- (NSUInteger)durationInMilliSeconds {
    if (self.mode == kSEAudioStreamModeWrite) {
        return [super durationInMilliSeconds];
    } else {
        return self.pv_record.soundRange.duration;
    }
}

- (BOOL)readData:(NSMutableData*)data position:(NSUInteger)position duration:(NSUInteger)duration {
    NSUInteger pos = position + self.pv_record.soundRange.start;
    if (pos > self.record.soundRange.start + self.record.soundRange.duration) {
        return NO;
    }
    NSUInteger lDur = self.record.soundRange.duration - position;
    NSUInteger dur = (lDur < duration)?lDur:duration;
    return [super readData:data position:pos duration:dur];
}

- (BOOL)close {
    if (self.mode == kSEAudioStreamModeWrite) {
        SERecordSoundRange range;
        range.start = 0;
        range.duration = self.duration*1000;
        self.record.soundRange = range;
        [self.pv_record.project saveProjectInfo];
    }
    return [super close];
}

@end
