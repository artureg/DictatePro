//
//  SRPlayerViewController.m
//  SoundRecorder
//
//  Created by Igor on 4/7/14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SRPlayerViewController.h"

#import "SRAudioStreamPlayer.h"

#import <AVFoundation/AVFoundation.h>

#include "SpeexACMConvert.h"

@interface SRPlayerViewController()<SRAudioStreamDelegate>
@property(nonatomic,weak) IBOutlet UIButton*        pv_playBtn;
@property(nonatomic,weak) IBOutlet UIButton*        pv_pauseBtn;
@property(nonatomic,weak) IBOutlet UISlider*        pv_progressSlider;
@property(nonatomic,weak) IBOutlet UILabel*         pv_timeLbl;
@property(nonatomic,strong) SRAudioStreamPlayer*    pv_player;
@property(nonatomic,assign) NSTimeInterval          pv_duration;
@property(nonatomic,assign) NSTimeInterval          pv_currentPos;
@property(nonatomic,strong) NSTimer*                pv_updateTimer;
- (IBAction)pm_onPlay:(id)sender;
- (IBAction)pm_onPause:(id)sender;
- (IBAction)pm_onSlider:(id)sender;
- (void)pm_onUpdate;
@end

@implementation SRPlayerViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.pv_duration = getACMSpeexFileDuration([self.filePath cStringUsingEncoding:NSASCIIStringEncoding]);
    int sampleRate = getACMSpeexFileSampleRate([self.filePath cStringUsingEncoding:NSASCIIStringEncoding]);
    self.pv_player = [[SRAudioStreamPlayer alloc] initWithSampleRate:sampleRate bitsPerSample:16 numberOfChannels:1];
    self.pv_player.delegate = self;
    self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", self.pv_currentPos, self.pv_duration];
    self.pv_progressSlider.value = 0;
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategorySoloAmbient error:nil];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.navigationController.navigationBarHidden = NO;
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.pv_player stop];
    self.navigationController.navigationBarHidden = YES;
}

#pragma mark - SRAudioStreamDelegate

- (NSData*)audioStreamPlayerProcessData:(SRAudioStreamPlayer*)aStream position:(NSTimeInterval)position duration:(NSTimeInterval)duration {
    int length;
    const int size = (duration + 1)*self.pv_player.audioInfo.mSampleRate*4;
    self.pv_currentPos = position;
    char data[size];
    if (decodeSpeexACMStream([self.filePath cStringUsingEncoding:NSASCIIStringEncoding], position*1000, duration*1000, data, &length)) {
        return [NSData dataWithBytes:data length:length];
    } else {
        self.pv_playBtn.hidden = NO;
        self.pv_pauseBtn.hidden = YES;
        [self.pv_updateTimer invalidate];
        self.pv_updateTimer = nil;
        [self.pv_player stop];
        self.pv_progressSlider.value = 0;
        self.pv_currentPos = 0;
        self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", self.pv_currentPos, self.pv_duration];
        return nil;
    }
}

- (void)audioStreamPlayerDecodeErrorDidOccur:(SRAudioStreamPlayer*)asPlayer error:(NSError*)error {
    NSLog(@"%@", error);
}

- (IBAction)pm_onPlay:(id)sender {
    self.pv_playBtn.hidden = YES;
    self.pv_pauseBtn.hidden = NO;
    self.pv_updateTimer = [NSTimer scheduledTimerWithTimeInterval:0.1f target:self selector:@selector(pm_onUpdate) userInfo:nil repeats:YES];
    [self.pv_player start];
}

- (IBAction)pm_onPause:(id)sender {
    [self.pv_updateTimer invalidate];
    self.pv_updateTimer = nil;
    self.pv_playBtn.hidden = NO;
    self.pv_pauseBtn.hidden = YES;
    [self.pv_player pause];
}

- (IBAction)pm_onSlider:(id)sender {
}

- (void)pm_onUpdate {
    self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", self.pv_currentPos, self.pv_duration];
    self.pv_progressSlider.value = self.pv_currentPos/self.pv_duration;
}

@end
