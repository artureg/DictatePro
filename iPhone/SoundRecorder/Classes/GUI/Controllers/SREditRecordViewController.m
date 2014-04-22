//
//  SRAddSoundViewController.m
//  SoundRecorder
//
//  Created by Igor Danich igor.danich@wise-apps.com Danich

//

#import "SREditRecordViewController.h"

#import "SRProject.h"
#import "SRRecord.h"

@interface SREditRecordViewController()<SRSoundDelegate>
@property(nonatomic,weak) IBOutlet UIButton*        pv_stopBtn;
@property(nonatomic,weak) IBOutlet UIButton*        pv_recordBtn;
@property(nonatomic,weak) IBOutlet UIButton*        pv_playBtn;
@property(nonatomic,weak) IBOutlet UIButton*        pv_pauseBtn;
@property(nonatomic,weak) IBOutlet UILabel*         pv_progressLbl;
@property(nonatomic,weak) IBOutlet UISlider*        pv_slider;
@property(nonatomic,strong) SRSound*                pv_sound;
- (IBAction)pm_onRecord:(id)sender;
- (IBAction)pm_onStop:(id)sender;
- (IBAction)pm_onPlay:(id)sender;
- (IBAction)pm_onPause:(id)sender;
- (IBAction)pm_onSlider:(id)sender;
- (void)pm_onSplit;
- (void)pm_reloadSound;
@end

@implementation SREditRecordViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc]
        initWithTitle:@"Split"
        style:UIBarButtonItemStyleBordered
        target:self
        action:@selector(pm_onSplit)];
    [self pm_reloadSound];
}

- (void)pm_reloadSound {
    self.navigationItem.rightBarButtonItem.enabled = NO;
    self.pv_recordBtn.hidden = NO;
    self.pv_stopBtn.hidden = YES;
    self.pv_slider.value = 0;
    self.pv_pauseBtn.hidden = YES;
    if (self.record) {
        self.pv_playBtn.enabled = YES;
        self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", 0.0f, self.record.timeRange.duration];
        self.pv_sound = self.record.sound;
        self.pv_sound.delegate = self;
        [self.pv_recordBtn removeFromSuperview];
        self.pv_recordBtn = nil;
    } else {
        self.pv_slider.userInteractionEnabled = NO;
        self.pv_sound = [[SRSound alloc] initWithFilePath:[self.project.projectSoundsPath stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.wav", @(time(NULL))]]];
        self.pv_sound.delegate = self;
        self.pv_playBtn.enabled = NO;
        self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", 0.0f, 0.0f];
    }
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    if (self.record) {
        if (self.pv_sound.isPlaying) {
            [self.pv_sound stop];
            self.pv_sound.delegate = nil;
            self.pv_sound = nil;
        }
    } else {
        if (self.pv_sound.isRecording) {
            self.pv_sound.delegate = nil;
            self.pv_sound = nil;
            return;
        }
        if (self.pv_sound.isPlaying) {
            self.pv_sound.delegate = nil;
            self.pv_sound = nil;
        }
        if (self.pv_sound.canPlay) {
            SRRecord* record = [SRRecord new];
            record.soundPath = self.pv_sound.filePath;
            record.timeRange = SRSoundRangeMake(0, self.pv_sound.duration);
            [self.project addRecord:record];
        } else {
            [self.pv_sound deleteSound];
        }
        self.pv_sound.delegate = nil;
        self.pv_sound = nil;
    }
}

- (void)pm_onSplit {
    if (self.record) {
        [self.project splitRecord:self.record inPosition:self.pv_sound.duration*self.pv_slider.value];
        self.pv_sound = self.record.sound;
        self.pv_sound.delegate = self;
    } else {
        SRRecord* record = [SRRecord new];
        record.soundPath = self.pv_sound.filePath;
        record.timeRange = SRSoundRangeMake(0, self.pv_sound.duration);
        [self.project addRecord:record];
        [self.project splitRecord:record inPosition:self.pv_sound.duration*self.pv_slider.value];
        self.record = record;
        self.pv_sound = self.record.sound;
        self.pv_sound.delegate = self;
    }
    [self pm_reloadSound];
}

- (IBAction)pm_onRecord:(id)sender {
    [self.pv_sound record];
}

- (IBAction)pm_onStop:(id)sender {
    [self.pv_sound stop];
}

- (IBAction)pm_onPlay:(id)sender {
    [self.pv_sound play];
}

- (IBAction)pm_onPause:(id)sender {
    [self.pv_sound pause];
}

- (IBAction)pm_onSlider:(id)sender {
    self.pv_sound.currentTime = self.pv_sound.duration*self.pv_slider.value;
    self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", self.pv_sound.currentTime, self.pv_sound.duration];
    if (!self.pv_sound.isPlaying) {
        self.navigationItem.rightBarButtonItem.enabled = ((self.pv_slider.value > 0)&&(self.pv_slider.value < 1));
    }
}

#pragma mark - SRSoundDelegate

- (void)soundDidStartRecording:(SRSound*)sound {
    self.pv_recordBtn.hidden = YES;
    self.pv_stopBtn.hidden = NO;
    self.pv_playBtn.enabled = NO;
    self.pv_slider.userInteractionEnabled = NO;
}

- (void)sound:(SRSound*)sound recordingPosition:(NSTimeInterval)time duration:(NSTimeInterval)duration {
    self.pv_slider.value = time/duration;
    self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", time, duration];
}

- (void)soundDidEndRecording:(SRSound*)sound {
    if (self.pv_sound.canPlay) {
        self.pv_slider.userInteractionEnabled = YES;
        self.pv_recordBtn.hidden = YES;
        self.pv_stopBtn.hidden = NO;
        self.pv_stopBtn.enabled = NO;
        self.pv_playBtn.enabled = YES;
        self.pv_slider.value = 0;
        self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", 0.0f, self.pv_sound.duration];
    } else {
        self.pv_slider.userInteractionEnabled = NO;
        [self.pv_sound deleteSound];
        self.pv_recordBtn.hidden = NO;
        self.pv_stopBtn.hidden = YES;
        self.pv_playBtn.enabled = NO;
        self.pv_slider.value = 0;
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
    self.pv_slider.value = time/duration;
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
    self.pv_slider.value = 0;
    self.pv_progressLbl.text = [NSString stringWithFormat:@"%3.1f / %3.1f", 0.0f, self.pv_sound.duration];
}

@end
