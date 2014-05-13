//  SRProjectViewController.m
//  SoundRecorder
//  Created by Igor Danich igor.danich@wise-apps.com on 4/8/14.


#import "SRProjectViewController.h"
#import <AVFoundation/AVFoundation.h>
#import "SRPlayerViewController.h"
#import "SEProject.h"
#import "SEProjectEngine.h"

#import "SEAudioStream.h"
#import "SEAudioStreamEngine.h"

#import "SESpeexACMAudioStream.h"

#import "SEProgressIndicator.h"
#import "SEVolumeIndicator.h"

@interface SRProjectViewController()<SEAudioStreamEngineDelegate>
@property(nonatomic,weak) IBOutlet SEProgressIndicator*     pv_trackSlider;
@property(nonatomic,weak) IBOutlet SEVolumeIndicator*       pv_volumeSlider;
@property(nonatomic,weak) IBOutlet UIButton*                pv_scrollLeft;
@property(nonatomic,weak) IBOutlet UIButton*                pv_scrollRight;
@property(nonatomic,weak) IBOutlet UIButton*                pv_scrollToStart;
@property(nonatomic,weak) IBOutlet UIButton*                pv_scrollToEnd;
@property(nonatomic,weak) IBOutlet UIButton*                pv_clearBtn;
@property(nonatomic,weak) IBOutlet UIButton*                pv_playBtn;
@property(nonatomic,weak) IBOutlet UIButton*                pv_pauseBtn;
@property(nonatomic,weak) IBOutlet UIButton*                pv_recordBtn;
@property(nonatomic,weak) IBOutlet UIButton*                pv_stopRecordBtn;
@property(nonatomic,weak) IBOutlet UIButton*                pv_exportBtn;
@property(nonatomic,weak) IBOutlet UIButton*                pv_saveBtn;
@property(nonatomic,weak) IBOutlet UILabel*                 pv_statusLbl;
@property(nonatomic,weak) IBOutlet UILabel*                 pv_timeLbl;
@property(nonatomic,weak) IBOutlet UIActivityIndicatorView* pv_indicator;
@property(nonatomic,strong) SEProjectEngine*                pv_engine;
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
- (IBAction)pm_onClear:(id)sender;
- (void)pm_updateTrackSlider;
@end

@implementation SRProjectViewController

- (void)pm_updateTrackSlider {
    self.pv_clearBtn.enabled = YES;
    switch (self.pv_engine.state) {
        case kSEAudioStreamEngineStatePlaying:
            if (!self.pv_indicator.isAnimating) {
                [self.pv_indicator startAnimating];
            }
            self.pv_statusLbl.text = @"Playing...";
            self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", self.pv_engine.currentTime, self.pv_engine.duration];
            if (!self.pv_trackSlider.isTracking) {
                self.pv_trackSlider.value = self.pv_engine.currentTime;
            }
            self.pv_trackSlider.duration = self.pv_engine.duration;
            self.pv_trackSlider.recordingDuration = 0;
            self.pv_scrollLeft.enabled = NO;
            self.pv_scrollRight.enabled = NO;
            self.pv_scrollToEnd.enabled = NO;
            self.pv_scrollToStart.enabled = NO;
            self.pv_saveBtn.enabled = NO;
            self.pv_clearBtn.enabled = NO;
            self.pv_exportBtn.enabled = NO;
            break;
        case kSEAudioStreamEngineStateRecording:
            if (!self.pv_indicator.isAnimating) {
                [self.pv_indicator startAnimating];
            }
            self.pv_statusLbl.text = @"Recording...";
            self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f / %3.1f",
                self.pv_engine.currentTime,
                self.pv_engine.recordingDuration,
                self.pv_engine.duration];
            self.pv_trackSlider.value = self.pv_engine.currentTime;
            self.pv_trackSlider.duration = self.pv_engine.duration;
            self.pv_trackSlider.recordingDuration = self.pv_engine.recordingDuration;
            self.pv_scrollLeft.enabled = NO;
            self.pv_scrollRight.enabled = NO;
            self.pv_scrollToEnd.enabled = NO;
            self.pv_scrollToStart.enabled = NO;
            self.pv_clearBtn.enabled = NO;
            self.pv_saveBtn.enabled = NO;
            self.pv_exportBtn.enabled = NO;
            break;
        default:
            if (self.pv_indicator.isAnimating) {
                [self.pv_indicator stopAnimating];
            }
            self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", self.pv_engine.currentTime, self.pv_engine.duration];
            self.pv_trackSlider.value = self.pv_engine.currentTime;
            self.pv_trackSlider.duration = self.pv_engine.duration;
            self.pv_trackSlider.recordingDuration = 0;
            self.pv_statusLbl.text = @"Ready";
            self.pv_playBtn.enabled = (self.pv_engine.duration > 0);
            self.pv_scrollLeft.enabled = (self.pv_engine.duration > 0);
            self.pv_scrollRight.enabled = (self.pv_engine.duration > 0);
            self.pv_scrollToEnd.enabled = (self.pv_engine.duration > 0);
            self.pv_scrollToStart.enabled = (self.pv_engine.duration > 0);
            self.pv_saveBtn.enabled = (self.pv_engine.duration > 0);
            self.pv_clearBtn.enabled = YES;
            self.pv_exportBtn.enabled = (self.pv_engine.duration > 0);
            break;
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.pv_engine = [[SEProjectEngine alloc] initWithProject:[[SEProject alloc] initWithFolder:documentsFolderPath()]];
    self.pv_engine.delegate = self;
    
    [self.pv_trackSlider addTarget:self action:@selector(pm_onTrackSlider:) forControlEvents:UIControlEventValueChanged];
    [self.pv_volumeSlider addTarget:self action:@selector(pm_onVolumeSlider:) forControlEvents:UIControlEventValueChanged];
    
    self.pv_trackSlider.value = 0;
    self.pv_statusLbl.text = nil;
    self.pv_volumeSlider.value = 1;
    
    [self pm_updateTrackSlider];
}

- (IBAction)pm_onTrackSlider:(id)sender {
    self.pv_engine.currentTime = self.pv_trackSlider.value;
    [self pm_updateTrackSlider];
}

- (IBAction)pm_onVolumeSlider:(id)sender {
    self.pv_engine.volume = self.pv_volumeSlider.value;
}

- (IBAction)pm_onScrollLeft:(id)sender {
    self.pv_engine.currentTime--;
    [self pm_updateTrackSlider];
}

- (IBAction)pm_onScrollRight:(id)sender {
    self.pv_engine.currentTime++;
    [self pm_updateTrackSlider];
}

- (IBAction)pm_onScrollToStart:(id)sender {
    self.pv_engine.currentTime = 0;
    [self pm_updateTrackSlider];
}

- (IBAction)pm_onScrollToEnd:(id)sender {
    self.pv_engine.currentTime = self.pv_engine.duration;
    [self pm_updateTrackSlider];
}

- (IBAction)pm_onPlay:(id)sender {
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategorySoloAmbient error:nil];
    self.pv_playBtn.enabled = NO;
    [self.pv_engine startPlaying];
    [self pm_updateTrackSlider];
}

- (IBAction)pm_onPause:(id)sender {
    [self.pv_engine pausePlaying];
    [self pm_updateTrackSlider];
}

- (IBAction)pm_onRecord:(id)sender {
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayAndRecord error:nil];
    self.pv_recordBtn.enabled = NO;
    [self.pv_engine startRecording];
}

- (IBAction)pm_onStopRecord:(id)sender {
    [self.pv_engine stopRecording];
}

- (IBAction)pm_onExport:(id)sender {
    self.view.userInteractionEnabled = NO;
    [self.pv_indicator startAnimating];
    self.pv_statusLbl.text = @"Encoding...";
    
    SESpeexACMAudioStream* speex = [[SESpeexACMAudioStream alloc] initWithAudioStream:self.pv_engine.audioStream];
    NSString* file = [documentsFolderPath() stringByAppendingPathComponent:@"spx.wav"];
    [speex exportAsynchronouslyToFile:file completion:^(NSError *error) {
        [self.pv_indicator stopAnimating];
        self.pv_statusLbl.text = nil;
        self.view.userInteractionEnabled = YES;
        self.pv_saveBtn.enabled = YES;
        if (error) {
            
        } else {
            SRPlayerViewController* player = [SRPlayerViewController new];
            player.filePath = file;
            [self.navigationController pushViewController:player animated:YES];
        }
        [speex close];
    }];
}

- (IBAction)pm_onSave:(id)sender {
    [self.pv_indicator startAnimating];
    self.pv_statusLbl.text = @"Saving...";
    self.view.userInteractionEnabled = NO;
    [self.pv_engine.project saveProjectAsynchronouslyWithCompletion:^(NSError *error) {
        if (error) {
            UIAlertView* aView = [[UIAlertView alloc] initWithTitle:nil
                message:[error localizedDescription]
                delegate:nil
                cancelButtonTitle:@"OK"
                otherButtonTitles:nil];
            [aView show];
        }
        self.view.userInteractionEnabled = YES;
        [self.pv_indicator stopAnimating];
        self.pv_statusLbl.text = @"Ready";
    }];
}

- (IBAction)pm_onClear:(id)sender {
    [self.pv_engine.project clearProject];
    self.pv_engine.currentTime = 0;
    [self.pv_trackSlider clear];
    [self pm_updateTrackSlider];
}

#pragma mark - SEAudioStreamEngineDelegate

- (void)audioStreamEngineDidStartPlaying:(SEAudioStreamEngine*)player {
    self.pv_playBtn.hidden = YES;
    self.pv_pauseBtn.hidden = NO;
    self.pv_recordBtn.enabled = NO;
    [self pm_updateTrackSlider];
}

- (void)audioStreamEngineDidPause:(SEAudioStreamEngine*)player {
    self.pv_playBtn.hidden = NO;
    self.pv_playBtn.enabled = YES;
    self.pv_pauseBtn.hidden = YES;
    self.pv_recordBtn.enabled = YES;
    [self pm_updateTrackSlider];
}

- (void)audioStreamEngineDidContinue:(SEAudioStreamEngine*)player {
    self.pv_playBtn.hidden = YES;
    self.pv_pauseBtn.hidden = NO;
    self.pv_recordBtn.enabled = NO;
    [self pm_updateTrackSlider];
}

- (void)audioStreamEnginePlaying:(SEAudioStreamEngine*)player didUpdateWithCurrentTime:(NSTimeInterval)time {
    [self pm_updateTrackSlider];
}

- (void)audioStreamEngineDidFinishPlaying:(SEAudioStreamEngine*)player stopped:(BOOL)stopped {
    self.pv_playBtn.hidden = NO;
    self.pv_playBtn.enabled = YES;
    self.pv_pauseBtn.hidden = YES;
    self.pv_recordBtn.enabled = YES;
    [self pm_updateTrackSlider];
}

- (void)audioStreamEngineDidStartRecording:(SEAudioStreamEngine*)player {
    self.pv_playBtn.enabled = NO;
    self.pv_scrollLeft.enabled = NO;
    self.pv_scrollRight.enabled = NO;
    self.pv_scrollToEnd.enabled = NO;
    self.pv_scrollToStart.enabled = NO;
    self.pv_recordBtn.hidden = YES;
    self.pv_stopRecordBtn.hidden = NO;
    self.pv_stopRecordBtn.enabled = YES;
    [self pm_updateTrackSlider];
}

- (void)audioStreamEngineRecording:(SEAudioStreamEngine*)player didUpdateWithCurrentTime:(NSTimeInterval)time {
    [self pm_updateTrackSlider];
}

- (void)audioStreamEngineDidStopRecording:(SEAudioStreamEngine*)player {
    self.pv_recordBtn.hidden = NO;
    self.pv_recordBtn.enabled = YES;
    self.pv_stopRecordBtn.hidden = YES;
    self.pv_playBtn.enabled = YES;
    self.pv_scrollLeft.enabled = NO;
    self.pv_scrollRight.enabled = NO;
    self.pv_scrollToEnd.enabled = NO;
    self.pv_scrollToStart.enabled = NO;
    [self pm_updateTrackSlider];
}

- (void)audioStreamEngine:(SEAudioStreamEngine*)engine didOccurError:(NSError*)error {
    UIAlertView* aView = [[UIAlertView alloc] initWithTitle:nil
        message:[error localizedDescription]
        delegate:nil
        cancelButtonTitle:@"OK"
        otherButtonTitles:nil];
    [aView show];
    self.pv_playBtn.hidden = NO;
    self.pv_playBtn.enabled = YES;
    self.pv_pauseBtn.hidden = YES;
    self.pv_recordBtn.enabled = YES;
    self.pv_scrollLeft.enabled = YES;
    self.pv_scrollRight.enabled = YES;
    self.pv_scrollToEnd.enabled = YES;
    self.pv_scrollToStart.enabled = YES;
}

@end
