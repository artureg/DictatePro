//
//  SEProjectStream.m
//  SoundRecorder
//
//  Created by Igor on 11.04.14.

//

#import "SEProjectAudioStream.h"
#import "SERecord.h"
#import "SERecordAudioStream.h"
#import "SEProject.h"
#import "SEProject+Internal.h"

@interface SEProjectAudioStream()
@property(nonatomic,weak) SEProject* pv_project;
@end

@implementation SEProjectAudioStream

- (instancetype)initWithProject:(SEProject*)project {
    if (self = [super init]) {
        self.pv_project = project;
        AudioStreamBasicDescription aInfo;
        aInfo.mSampleRate = 16000;
        aInfo.mChannelsPerFrame = 1;
        aInfo.mFramesPerPacket = 1;
        aInfo.mBitsPerChannel = 16;
        aInfo.mBytesPerFrame = 2;
        aInfo.mBytesPerPacket =  2;
        aInfo.mFormatID = kAudioFormatLinearPCM;
        aInfo.mFormatFlags = kLinearPCMFormatFlagIsSignedInteger|kLinearPCMFormatFlagIsPacked;
        [self adjustToAudioDescription:aInfo];
    }
    return self;
}

- (SEProject*)project {
    return self.pv_project;
}

- (BOOL)openWithMode:(TSEAudioStreamMode)mode {
    if (mode != kSEAudioStreamModeRead) {
        return NO;
    }
    return YES;
}

- (BOOL)readData:(NSMutableData*)data position:(NSUInteger)position duration:(NSUInteger)duration {
    NSUInteger pos = 0;
    NSUInteger eDuration = duration;
    for (SERecord* record in self.pv_project.records) {
        if (record.audioStream.mode != kSEAudioStreamModeRead) {
            [record.audioStream close];
            [record.audioStream openWithMode:kSEAudioStreamModeRead];
        }
        if (eDuration <= 0) {
            break;
        }
        NSUInteger recordDuration = record.audioStream.duration*1000;
        if (duration + recordDuration > position) {
            NSUInteger lPos = position - pos;
            NSUInteger lDuration = (eDuration > recordDuration)?recordDuration:eDuration;
            [record.audioStream readData:data position:lPos duration:lDuration];
            eDuration -= lDuration;
        }
        pos += record.audioStream.duration*1000;
    }
    if ([data length] == 0) {
        NSLog(@"");
    }
    return YES;
}

@end
