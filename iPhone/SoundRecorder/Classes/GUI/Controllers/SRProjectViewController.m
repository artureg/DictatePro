//
//  SRProjectViewController.m
//  SoundRecorder
//
//  Created by Igor on 4/8/14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SRProjectViewController.h"
#import <AVFoundation/AVFoundation.h>
#import "SRProject.h"
#import "SRPlayerViewController.h"

@interface SRProjectViewController()<SRSoundDelegate>
@property(nonatomic,weak) IBOutlet UISlider*                pv_trackSlider;
@property(nonatomic,weak) IBOutlet UISlider*                pv_volumeSlider;
@property(nonatomic,weak) IBOutlet UIButton*                pv_scrollLeft;
@property(nonatomic,weak) IBOutlet UIButton*                pv_scrollRight;
@property(nonatomic,weak) IBOutlet UIButton*                pv_scrollToStart;
@property(nonatomic,weak) IBOutlet UIButton*                pv_scrollToEnd;
@property(nonatomic,weak) IBOutlet UIButton*                pv_playBtn;
@property(nonatomic,weak) IBOutlet UIButton*                pv_pauseBtn;
@property(nonatomic,weak) IBOutlet UIButton*                pv_recordBtn;
@property(nonatomic,weak) IBOutlet UIButton*                pv_stopRecordBtn;
@property(nonatomic,weak) IBOutlet UIButton*                pv_exportBtn;
@property(nonatomic,weak) IBOutlet UIButton*                pv_saveBtn;
@property(nonatomic,weak) IBOutlet UILabel*                 pv_statusLbl;
@property(nonatomic,weak) IBOutlet UILabel*                 pv_timeLbl;
@property(nonatomic,weak) IBOutlet UIActivityIndicatorView* pv_indicator;
@property(nonatomic,strong) SRProject*                      pv_project;
@property(nonatomic,strong) SRSound*                        pv_tmpSound;
@property(nonatomic,strong) SRSound*                        pv_sound;
@property(nonatomic,assign) CGFloat                         pv_volume;
- (IBAction)pm_onTrackSlider:(id)sender;
- (IBAction)pm_onVolumeSlider:(id)sender;
- (IBAction)pm_onScrollLeft:(id)sender;
- (IBAction)pm_onScrollRight:(id)sender;
- (IBAction)pm_onScrollToStart:(id)sender;
- (IBAction)pm_onScrollToEnd:(id)sender;
- (IBAction)pm_onPlay:(id)sender;
- (IBAction)pm_onPause:(id)sender;
- (IBAction)pm_onRecord:(id)sender;
- (IBAction)pm_onStopRecord:(id)sender;
- (IBAction)pm_onExport:(id)sender;
- (IBAction)pm_onSave:(id)sender;
- (void)pm_reloadSound;
@end

@implementation SRProjectViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.pv_project = [SRProject new];
    
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayAndRecord error:nil];
    [self.pv_trackSlider setThumbImage:[UIImage imageNamed:@"play_pin-cursor.png"] forState:UIControlStateNormal];
    
//    [self.pv_volumeSlider setMinimumTrackImage:[UIImage imageNamed:@"volume_selected.png"] forState:UIControlStateNormal];
    [self.pv_volumeSlider setThumbImage:[UIImage imageNamed:@"volume-cursor.png"] forState:UIControlStateNormal];
    
    self.pv_trackSlider.value = 0;
    self.pv_statusLbl.text = nil;
    self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", self.pv_trackSlider.value*self.pv_project.duration, self.pv_project.duration];
    if (![[NSFileManager defaultManager] fileExistsAtPath:self.pv_project.projectSoundSpeexPath]) {
        self.pv_saveBtn.enabled = NO;
    }
    self.pv_volume = 1;
    self.pv_volumeSlider.value = 1;
    [self pm_reloadSound];
}

- (IBAction)pm_onTrackSlider:(id)sender {
    self.pv_sound.currentTime = self.pv_trackSlider.value*self.pv_sound.duration;
    self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", self.pv_sound.currentTime, self.pv_sound.duration];
}

- (IBAction)pm_onVolumeSlider:(id)sender {
    self.pv_volume = self.pv_volumeSlider.value;
    self.pv_sound.volume = self.pv_volume;
}

- (IBAction)pm_onScrollLeft:(id)sender {
    if (!self.pv_sound.canPlay) {
        return;
    }
    if (self.pv_sound.currentTime > 1) {
        self.pv_sound.currentTime--;
    } else {
        self.pv_sound.currentTime = 0;
    }
    self.pv_trackSlider.value = self.pv_sound.currentTime/self.pv_sound.duration;
    self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", self.pv_sound.currentTime, self.pv_sound.duration];
}

- (IBAction)pm_onScrollRight:(id)sender {
    if (!self.pv_sound.canPlay) {
        return;
    }
    if (self.pv_sound.currentTime + 1 < self.pv_sound.duration) {
        self.pv_sound.currentTime++;
    } else {
        self.pv_sound.currentTime = self.pv_sound.duration;
    }
    self.pv_trackSlider.value = self.pv_sound.currentTime/self.pv_sound.duration;
    self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", self.pv_sound.currentTime, self.pv_sound.duration];
}

- (IBAction)pm_onScrollToStart:(id)sender {
    if (!self.pv_sound.canPlay) {
        return;
    }
    self.pv_sound.currentTime = 0;
    self.pv_trackSlider.value = self.pv_sound.currentTime/self.pv_sound.duration;
    self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", self.pv_sound.currentTime, self.pv_sound.duration];
}

- (IBAction)pm_onScrollToEnd:(id)sender {
    if (!self.pv_sound.canPlay) {
        return;
    }
    self.pv_sound.currentTime = self.pv_sound.duration;
    self.pv_trackSlider.value = self.pv_sound.currentTime/self.pv_sound.duration;
    self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", self.pv_sound.currentTime, self.pv_sound.duration];
}

- (IBAction)pm_onPlay:(id)sender {
    [self.pv_sound play];
}

- (IBAction)pm_onPause:(id)sender {
    [self.pv_sound pause];
}

- (IBAction)pm_onRecord:(id)sender {
    self.pv_tmpSound = [[SRSound alloc] initWithFilePath:[self.pv_project.projectSoundsPath stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.wav", @(time(NULL))]]];
    self.pv_tmpSound.delegate = self;
    [self.pv_tmpSound record];
}

- (IBAction)pm_onStopRecord:(id)sender {
    [self.pv_tmpSound stop];
}

- (IBAction)pm_onExport:(id)sender {
    self.view.userInteractionEnabled = NO;
    [self.pv_indicator startAnimating];
    self.pv_statusLbl.text = @"Encoding...";
    [self.pv_project encodeToSpeexACMWithCompletion:^(NSError *error) {
        [self.pv_indicator stopAnimating];
        self.pv_statusLbl.text = nil;
        self.view.userInteractionEnabled = YES;
        self.pv_saveBtn.enabled = YES;
    }];
}

- (void)pm_reloadSound {
    self.pv_sound = [[SRSound alloc] initWithFilePath:self.pv_project.projectSoundPath];
    self.pv_sound.delegate = self;
    if (self.pv_sound.canPlay) {
        self.pv_sound.volume = self.pv_volume;
        self.pv_playBtn.enabled = YES;
        self.pv_exportBtn.enabled = YES;
    } else {
        self.pv_playBtn.enabled = NO;
        self.pv_exportBtn.enabled = NO;
    }
}

- (IBAction)pm_onSave:(id)sender {
    SRPlayerViewController* ctrl = [SRPlayerViewController new];
    ctrl.filePath = self.pv_project.projectSoundSpeexPath;
    [self.navigationController pushViewController:ctrl animated:YES];
}

#pragma mark - SRSoundDelegate

- (void)pm_enableStopRecord {
    self.pv_stopRecordBtn.enabled = YES;
}

- (void)soundDidStartRecording:(SRSound*)sound {
    self.pv_scrollLeft.enabled = NO;
    self.pv_scrollRight.enabled = NO;
    self.pv_scrollToStart.enabled = NO;
    self.pv_scrollToEnd.enabled = NO;
    self.pv_playBtn.enabled = NO;
    self.pv_exportBtn.enabled = NO;
    self.pv_saveBtn.enabled = NO;
    self.pv_recordBtn.hidden = YES;
    self.pv_stopRecordBtn.hidden = NO;
    self.pv_trackSlider.enabled = NO;
    [self.pv_indicator startAnimating];
    self.pv_statusLbl.text = @"Recording...";
    self.pv_stopRecordBtn.enabled = NO;
    [self performSelector:@selector(pm_enableStopRecord) withObject:nil afterDelay:0.5f];
}

- (void)sound:(SRSound*)sound recordingPosition:(NSTimeInterval)time duration:(NSTimeInterval)duration {
    self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f / %3.1f",
        self.pv_trackSlider.value*self.pv_project.duration,
        time,
        time + self.pv_project.duration];
}

- (void)soundDidEndRecording:(SRSound*)sound {
    self.pv_scrollLeft.enabled = YES;
    self.pv_scrollRight.enabled = YES;
    self.pv_scrollToStart.enabled = YES;
    self.pv_scrollToEnd.enabled = YES;
    self.pv_recordBtn.hidden = NO;
    self.pv_stopRecordBtn.hidden = YES;
    self.pv_statusLbl.text = @"Building...";
    if ([self.pv_project.records count] == 0) {
        SRRecord* record = [SRRecord new];
        record.soundPath = self.pv_tmpSound.filePath;
        record.timeRange = SRSoundRangeMake(0, self.pv_tmpSound.duration);
        [self.pv_project addRecord:record];
    } else {
        SRRecord* record = self.pv_project.records[0];
        [self.pv_project splitRecord:record inPosition:self.pv_trackSlider.value*self.pv_project.duration];
        record = [SRRecord new];
        record.soundPath = self.pv_tmpSound.filePath;
        record.timeRange = SRSoundRangeMake(0, self.pv_tmpSound.duration);
        [self.pv_project addRecord:record];
        [self.pv_project moveRecord:record toIndex:1];
    }
    [self.pv_project buildProjectWithCompletion:^(NSError *error) {
        if ([self.pv_project.records count] > 1) {
            NSTimeInterval duration = self.pv_project.duration;
            while ([self.pv_project.sounds count] > 0) {
                [self.pv_project deleteSound:[self.pv_project.sounds lastObject]];
            }
            NSString* newFile = [self.pv_project.projectSoundsPath stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.wav", @(time(NULL))]];
            [[NSFileManager defaultManager] moveItemAtPath:self.pv_project.projectSoundPath toPath:newFile error:nil];
            SRRecord* record = [SRRecord new];
            record.soundPath = newFile;
            record.timeRange = SRSoundRangeMake(0, duration);
            self.pv_tmpSound = nil;
            [self.pv_project addRecord:record];
            [self.pv_project buildProjectWithCompletion:^(NSError *error) {
                self.pv_trackSlider.enabled = YES;
                self.pv_statusLbl.text = nil;
                [self.pv_indicator stopAnimating];
                [self pm_reloadSound];
            }];
        } else {
            self.pv_tmpSound = nil;
            self.pv_trackSlider.enabled = YES;
            self.pv_statusLbl.text = nil;
            [self.pv_indicator stopAnimating];
            [self pm_reloadSound];
        }
    }];
}

- (void)soundDidStartPlaying:(SRSound*)sound {
    self.pv_recordBtn.enabled = NO;
    self.pv_playBtn.hidden = YES;
    self.pv_pauseBtn.hidden = NO;
    [self.pv_indicator startAnimating];
    self.pv_statusLbl.text = @"Playing...";
}

- (void)soundDidPause:(SRSound*)sound {
    self.pv_playBtn.hidden = NO;
    self.pv_pauseBtn.hidden = YES;
    self.pv_recordBtn.enabled = YES;
    [self.pv_indicator stopAnimating];
    self.pv_statusLbl.text = nil;
}

- (void)soundDidContinue:(SRSound*)sound {
    self.pv_playBtn.hidden = YES;
    self.pv_pauseBtn.hidden = NO;
    self.pv_recordBtn.enabled = NO;
    [self.pv_indicator startAnimating];
    self.pv_statusLbl.text = @"Playing...";
}

- (void)sound:(SRSound*)sound playPosition:(NSTimeInterval)time duration:(NSTimeInterval)duration {
    self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", time, duration];
    self.pv_trackSlider.value = time/duration;
}

- (void)soundDidEndPlaying:(SRSound*)sound {
    self.pv_playBtn.hidden = NO;
    self.pv_pauseBtn.hidden = YES;
    self.pv_recordBtn.enabled = YES;
    [self.pv_indicator stopAnimating];
    self.pv_statusLbl.text = nil;
}

@end
