//
//  SRAddSoundViewController.m
//  SoundRecorder
//
//  Created by Igor on 12/13/13.
//  Copyright (c) 2013 Igor. All rights reserved.
//

#import "SRAddSoundViewController.h"

#import "SRSoundList.h"

@interface SRAddSoundViewController()<SRSoundDelegate>
@property(nonatomic,weak) IBOutlet UIButton*        pv_stopBtn;
@property(nonatomic,weak) IBOutlet UIButton*        pv_recordBtn;
@property(nonatomic,weak) IBOutlet UIButton*        pv_playBtn;
@property(nonatomic,weak) IBOutlet UIButton*        pv_pauseBtn;
@property(nonatomic,weak) IBOutlet UILabel*         pv_progressLbl;
@property(nonatomic,weak) IBOutlet UIProgressView*  pv_progressView;
@property(nonatomic,assign) BOOL                    pv_isSplitting;
- (IBAction)pm_onRecord:(id)sender;
- (IBAction)pm_onStop:(id)sender;
- (IBAction)pm_onPlay:(id)sender;
- (IBAction)pm_onPause:(id)sender;
- (void)pm_onSplit;
@end

@implementation SRAddSoundViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc]
        initWithTitle:@"Split"
        style:UIBarButtonItemStyleBordered
        target:self
        action:@selector(pm_onSplit)];
    self.navigationItem.rightBarButtonItem.enabled = NO;
    self.pv_recordBtn.hidden = NO;
    self.pv_stopBtn.hidden = YES;
    self.pv_progressView.progress = 0;
    self.pv_pauseBtn.hidden = YES;
    if (self.sound) {
        self.pv_playBtn.enabled = YES;
        self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", 0.0f, self.sound.duration];
    } else {
        self.sound = [SRSound new];
        self.pv_playBtn.enabled = NO;
        self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", 0.0f, 0.0f];
    }
    self.sound.delegate = self;
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    if ((self.sound.isPlaying)||(self.sound.isRecording)) {
        [self.sound stop];
    }
    self.sound.delegate = nil;
    if (self.pv_isSplitting) {
        return;
    }
    if (!self.sound.canPlay) {
        [self.soundList removeSound:self.sound];
        return;
    }
    if (![self.soundList.sounds containsObject:self.sound]) {
        [self.soundList addSound:self.sound];
    }
}

- (void)pm_onSplit {
    self.pv_isSplitting = YES;
    if (![self.soundList.sounds containsObject:self.sound]) {
        [self.soundList addSound:self.sound];
    }
    [self.soundList splitSound:self.sound time:self.sound.currentTime];
    [self.navigationController popViewControllerAnimated:YES];
}

- (IBAction)pm_onRecord:(id)sender {
    [self.sound record];
}

- (IBAction)pm_onStop:(id)sender {
    [self.sound stop];
}

- (IBAction)pm_onPlay:(id)sender {
    [self.sound play];
}

- (IBAction)pm_onPause:(id)sender {
    [self.sound pause];
}

#pragma mark - SRSoundDelegate

- (void)soundDidStartRecording:(SRSound*)sound {
    self.pv_recordBtn.hidden = YES;
    self.pv_stopBtn.hidden = NO;
    self.pv_playBtn.enabled = NO;
}

- (void)sound:(SRSound*)sound recordingPosition:(NSTimeInterval)time duration:(NSTimeInterval)duration {
    self.pv_progressView.progress = time/duration;
    self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", time, duration];
}

- (void)soundDidEndRecording:(SRSound*)sound {
    if (self.sound.canPlay) {
        self.pv_recordBtn.hidden = YES;
        self.pv_stopBtn.hidden = NO;
        self.pv_stopBtn.enabled = NO;
        self.pv_playBtn.enabled = YES;
        self.pv_progressView.progress = 0;
        self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", 0.0f, self.sound.duration];
    } else {
        self.pv_recordBtn.hidden = NO;
        self.pv_stopBtn.hidden = YES;
        self.pv_playBtn.enabled = NO;
        self.pv_progressView.progress = 0;
        self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", 0.0f, 0.0f];
    }
}

- (void)soundDidStartPlaying:(SRSound*)sound {
    self.pv_playBtn.hidden = YES;
    self.pv_pauseBtn.enabled = YES;
    self.pv_pauseBtn.hidden = NO;
    self.pv_recordBtn.hidden = YES;
    self.pv_stopBtn.hidden = NO;
    self.pv_stopBtn.enabled = YES;
}

- (void)soundDidPause:(SRSound*)sound {
    self.pv_pauseBtn.hidden = YES;
    self.pv_playBtn.enabled = YES;
    self.pv_playBtn.hidden = NO;
    self.navigationItem.rightBarButtonItem.enabled = YES;
}

- (void)soundDidContinue:(SRSound*)sound {
    self.pv_playBtn.hidden = YES;
    self.pv_pauseBtn.hidden = NO;
    self.pv_pauseBtn.enabled = YES;
    self.navigationItem.rightBarButtonItem.enabled = NO;
}

- (void)sound:(SRSound*)sound playPosition:(NSTimeInterval)time duration:(NSTimeInterval)duration {
    self.pv_progressView.progress = time/duration;
    self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", time, duration];
}

- (void)soundDidEndPlaying:(SRSound*)sound {
    self.navigationItem.rightBarButtonItem.enabled = NO;
    self.pv_recordBtn.hidden = NO;
    self.pv_stopBtn.enabled = YES;
    self.pv_stopBtn.hidden = YES;
    self.pv_playBtn.enabled = YES;
    self.pv_playBtn.hidden = NO;
    self.pv_pauseBtn.hidden = YES;
    self.pv_progressView.progress = 0;
    self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", 0.0f, self.sound.duration];
}

@end
