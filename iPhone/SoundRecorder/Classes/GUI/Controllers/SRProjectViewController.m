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

@interface SRProjectViewController()<SEAudioStreamEngineDelegate>
@property(nonatomic,weak) IBOutlet UISlider*                pv_trackSlider;
@property(nonatomic,weak) IBOutlet UISlider*                pv_volumeSlider;
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
    switch (self.pv_engine.state) {
        case kSEAudioStreamEngineStatePlaying:
            self.pv_statusLbl.text = @"Playing...";
            self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", self.pv_engine.currentTime, self.pv_engine.duration];
            self.pv_trackSlider.value = self.pv_engine.currentTime/self.pv_engine.duration;
            break;
        case kSEAudioStreamEngineStateRecording:
            self.pv_statusLbl.text = @"Recording...";
            self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f / %3.1f",
                self.pv_engine.currentTime,
                self.pv_engine.recordingDuration,
                self.pv_engine.duration];
            self.pv_trackSlider.value = self.pv_engine.currentTime/self.pv_engine.duration;
            break;
        default:
            self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", self.pv_engine.currentTime, self.pv_engine.duration];
            self.pv_trackSlider.value = self.pv_engine.currentTime/self.pv_engine.duration;
            self.pv_statusLbl.text = @"Ready";
            break;
    }
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.pv_engine = [[SEProjectEngine alloc] initWithProject:[[SEProject alloc] initWithFolder:documentsFolderPath()]];
    self.pv_engine.delegate = self;
    
    [self.pv_trackSlider setThumbImage:[UIImage imageNamed:@"play_pin-cursor.png"] forState:UIControlStateNormal];
    [self.pv_volumeSlider setThumbImage:[UIImage imageNamed:@"volume-cursor.png"] forState:UIControlStateNormal];
    
    self.pv_trackSlider.value = 0;
    self.pv_statusLbl.text = nil;
    self.pv_volumeSlider.value = 1;
    
    [self pm_updateTrackSlider];
}

- (IBAction)pm_onTrackSlider:(id)sender {
    self.pv_engine.currentTime = self.pv_trackSlider.value*self.pv_engine.duration;
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
//    self.view.userInteractionEnabled = NO;
//    [self.pv_indicator startAnimating];
//    self.pv_statusLbl.text = @"Encoding...";
//    [self.pv_project encodeToSpeexACMWithCompletion:^(NSError *error) {
//        [self.pv_indicator stopAnimating];
//        self.pv_statusLbl.text = nil;
//        self.view.userInteractionEnabled = YES;
//        self.pv_saveBtn.enabled = YES;
//    }];
}

- (IBAction)pm_onSave:(id)sender {
//    SRPlayerViewController* ctrl = [SRPlayerViewController new];
//    ctrl.filePath = self.pv_project.projectSoundSpeexPath;
//    [self.navigationController pushViewController:ctrl animated:YES];
}

- (IBAction)pm_onClear:(id)sender {
//    [self.pv_project clearAll];
//    [self.pv_project clearSound];
//    self.pv_trackSlider.value = 0;
//    self.pv_statusLbl.text = nil;
//    self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", self.pv_trackSlider.value*self.pv_project.duration, self.pv_project.duration];
//    if (![[NSFileManager defaultManager] fileExistsAtPath:self.pv_project.projectSoundSpeexPath]) {
//        self.pv_saveBtn.enabled = NO;
//    }
//    self.pv_volume = 1;
//    self.pv_volumeSlider.value = 1;
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
