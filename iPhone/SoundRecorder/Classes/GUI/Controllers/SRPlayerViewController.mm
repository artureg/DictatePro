//
//  SRPlayerViewController.m
//  SoundRecorder
//
//  Created by Igor on 4/7/14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SRPlayerViewController.h"

#import "SRAudioStreamPlayer.h"

#include "SpeexACMConvert.h"

@interface SRPlayerViewController()<SRAudioStreamDelegate>
@property(nonatomic,strong) SRAudioStreamPlayer*    pv_player;
@property(nonatomic,strong) NSString*               pv_filePath;
@end

@implementation SRPlayerViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.pv_filePath = [[NSBundle mainBundle] pathForResource:@"rec1.wav" ofType:nil];
    self.pv_player = [[SRAudioStreamPlayer alloc] initWithSampleRate:8000 bitsPerSample:16 numberOfChannels:1];
    self.pv_player.delegate = self;
    [self.pv_player start];
}

#pragma mark - SRAudioStreamDelegate

- (NSData*)audioStreamPlayerProcessData:(SRAudioStreamPlayer*)aStream position:(NSTimeInterval)position duration:(NSTimeInterval)duration {
    int length;
    const int size = duration*10000;
    char data[size];
    decodeSpeexACMStream([self.pv_filePath cStringUsingEncoding:NSASCIIStringEncoding], position*1000, duration*1000, data, &length);
    return [NSData dataWithBytes:data length:length];
}

- (void)audioStreamPlayerDecodeErrorDidOccur:(SRAudioStreamPlayer*)asPlayer error:(NSError*)error {
    NSLog(@"%@", error);
}

@end
