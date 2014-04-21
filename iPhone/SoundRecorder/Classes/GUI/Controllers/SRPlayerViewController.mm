//  SRPlayerViewController.m
//  SoundRecorder
//  Created by Igor Danich igor.danich@wise-apps.com on 4/7/14.


#import "SRPlayerViewController.h"

#import "SRAudioStreamPlayer.h"

#import <AVFoundation/AVFoundation.h>

#import "SEAudioStream.h"
#import "SEAudioStreamEngine.h"
#import "SESpeexACMAudioStream.h"

@interface SRPlayerViewController()<SEAudioStreamEngineDelegate>
@property(nonatomic,weak) IBOutlet UIButton*        pv_playBtn;
@property(nonatomic,weak) IBOutlet UIButton*        pv_pauseBtn;
@property(nonatomic,weak) IBOutlet UISlider*        pv_progressSlider;
@property(nonatomic,weak) IBOutlet UILabel*         pv_timeLbl;
@property(nonatomic,strong) SEAudioStreamEngine*    pv_player;
- (IBAction)pm_onPlay:(id)sender;
- (IBAction)pm_onPause:(id)sender;
- (IBAction)pm_onSlider:(id)sender;
- (void)pm_update;
@end

@implementation SRPlayerViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
//    self.filePath = [[NSBundle mainBundle] pathForResource:@"rec_spx" ofType:@"wav"];
    
    self.pv_player = [[SEAudioStreamEngine alloc] initWithStream:[[SESpeexACMAudioStream alloc] initWithContentsOfFile:self.filePath]];
    self.pv_player.delegate = self;
    self.pv_progressSlider.value = 0;
    [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategorySoloAmbient error:nil];
    [self pm_update];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    self.navigationController.navigationBarHidden = NO;
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    [self.pv_player stopPlaying];
    self.navigationController.navigationBarHidden = YES;
}

- (IBAction)pm_onPlay:(id)sender {
    self.pv_playBtn.enabled = NO;
    [self.pv_player startPlaying];
    [self pm_update];
}

- (IBAction)pm_onPause:(id)sender {
    self.pv_pauseBtn.enabled = NO;
    [self.pv_player pausePlaying];
    [self pm_update];
}

- (IBAction)pm_onSlider:(id)sender {
    self.pv_player.currentTime = self.pv_progressSlider.value*self.pv_player.duration;
    [self pm_update];
}

- (void)pm_update {
    self.pv_timeLbl.text = [NSString stringWithFormat:@"%3.f / %3.1f", self.pv_player.currentTime, self.pv_player.duration];
    if (!self.pv_progressSlider.isTracking) {
        self.pv_progressSlider.value = self.pv_player.currentTime/self.pv_player.duration;
    }
}

#pragma mark - SEAudioStreamEngine

- (void)audioStreamEngineDidStartPlaying:(SEAudioStreamEngine*)engine {
    self.pv_playBtn.hidden = YES;
    self.pv_pauseBtn.hidden = NO;
    [self pm_update];
}

- (void)audioStreamEngineDidPause:(SEAudioStreamEngine*)engine {
    self.pv_playBtn.hidden = NO;
    self.pv_playBtn.enabled = YES;
    self.pv_pauseBtn.hidden = YES;
    [self pm_update];
}

- (void)audioStreamEngineDidContinue:(SEAudioStreamEngine*)engine {
    self.pv_playBtn.hidden = YES;
    self.pv_pauseBtn.hidden = NO;
    [self pm_update];
}

- (void)audioStreamEnginePlaying:(SEAudioStreamEngine*)engine didUpdateWithCurrentTime:(NSTimeInterval)time {
    [self pm_update];
}

- (void)audioStreamEngineDidFinishPlaying:(SEAudioStreamEngine*)engine stopped:(BOOL)stopped {
    self.pv_playBtn.hidden = NO;
    self.pv_playBtn.enabled = YES;
    self.pv_pauseBtn.hidden = YES;
    [self pm_update];
}

@end
