//
//  SERecordAudioStream.m
//  SoundRecorder
//
//  Created by Igor on 11.04.14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SERecordAudioStream.h"

#import "SERecord.h"
#import "SEProject.h"
#import "SEProject+Internal.h"

@implementation SERecordAudioStream

- (instancetype)initWithRecord:(SERecord*)record {
    NSString* path = [[record.soundURL resourceSpecifier] stringByReplacingOccurrencesOfString:@"%20" withString:@" "];
    if (![[NSFileManager defaultManager] fileExistsAtPath:path]) {
        path = [NSString stringWithFormat:@"%@/%@%@.wav", record.project.projectSoundsPath, @((NSInteger)time(NULL)), @(arc4random()%999999)];
        record.soundURL = [NSURL fileURLWithPath:path];
    }
    if (self = [super initWithContentsOfFile:path]) {
    }
    return self;
}

@end