//
//  STViewController.m
//  SEKitTest
//
//  Created by Igor on 23.04.14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "STViewController.h"

#import <SEKit/SEProjectEngine.h>
#import <SEKit/SEProject.h>

@interface STViewController()<SEAudioStreamEngineDelegate>
@property(nonatomic,strong) SEProjectEngine* pv_engine;
@property(nonatomic,weak) IBOutlet UISlider* pv_slider;
@property(nonatomic,weak) IBOutlet UIButton* pv_playBtn;
@property(nonatomic,weak) IBOutlet UIButton* pv_pauseBtn;
@property(nonatomic,weak) IBOutlet UIButton* pv_recordBtn;
@property(nonatomic,weak) IBOutlet UIButton* pv_stopRecordBtn;
- (IBAction)pm_onSlider:(id)sender;
- (IBAction)pm_onPlay:(id)sender;
- (IBAction)pm_onPause:(id)sender;
- (IBAction)pm_onRecord:(id)sender;
- (IBAction)pm_onStopRecord:(id)sender;
@end

@implementation STViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.pv_engine = [[SEProjectEngine alloc] initWithProject:[[SEProject alloc] initWithFolder:[NSHomeDirectory() stringByAppendingPathComponent:@"Documents"]]];
    self.pv_engine.delegate = self;
    if (!self.pv_slider.isTracking) {
        self.pv_slider.value = self.pv_engine.currentTime/self.pv_engine.duration;
    }
}

- (IBAction)pm_onSlider:(id)sender {
    self.pv_engine.currentTime = self.pv_slider.value*self.pv_engine.duration;
}

- (IBAction)pm_onPlay:(id)sender {
    [self.pv_engine startPlaying];
}

- (IBAction)pm_onPause:(id)sender {
    [self.pv_engine pausePlaying];
}

- (IBAction)pm_onRecord:(id)sender {
    [self.pv_engine startRecording];
}

- (IBAction)pm_onStopRecord:(id)sender {
    [self.pv_engine stopRecording];
}

#pragma mark - SEAudioStreamEngineDelegate

- (void)audioStreamEngineDidStartPlaying:(SEAudioStreamEngine*)engine {
    if (!self.pv_slider.isTracking) {
        self.pv_slider.value = self.pv_engine.currentTime/self.pv_engine.duration;
    }
}

- (void)audioStreamEngineDidPause:(SEAudioStreamEngine*)engine {
    if (!self.pv_slider.isTracking) {
        self.pv_slider.value = self.pv_engine.currentTime/self.pv_engine.duration;
    }
}

- (void)audioStreamEngineDidContinue:(SEAudioStreamEngine*)engine {
    if (!self.pv_slider.isTracking) {
        self.pv_slider.value = self.pv_engine.currentTime/self.pv_engine.duration;
    }
}

- (void)audioStreamEnginePlaying:(SEAudioStreamEngine*)engine didUpdateWithCurrentTime:(NSTimeInterval)time {
    if (!self.pv_slider.isTracking) {
        self.pv_slider.value = self.pv_engine.currentTime/self.pv_engine.duration;
    }
}

- (void)audioStreamEngineDidFinishPlaying:(SEAudioStreamEngine*)engine stopped:(BOOL)stopped {
    if (!self.pv_slider.isTracking) {
        self.pv_slider.value = self.pv_engine.currentTime/self.pv_engine.duration;
    }
}

- (void)audioStreamEngineDidStartRecording:(SEAudioStreamEngine*)engine {
    if (!self.pv_slider.isTracking) {
        self.pv_slider.value = self.pv_engine.currentTime/self.pv_engine.duration;
    }
}

- (void)audioStreamEngineRecording:(SEAudioStreamEngine*)engine didUpdateWithCurrentTime:(NSTimeInterval)time {
    if (!self.pv_slider.isTracking) {
        self.pv_slider.value = self.pv_engine.currentTime/self.pv_engine.duration;
    }
}

- (void)audioStreamEngineDidStopRecording:(SEAudioStreamEngine*)engine {
    if (!self.pv_slider.isTracking) {
        self.pv_slider.value = self.pv_engine.currentTime/self.pv_engine.duration;
    }
}

- (void)audioStreamEngine:(SEAudioStreamEngine*)engine didOccurError:(NSError*)error {
    UIAlertView* aView = [[UIAlertView alloc]
        initWithTitle:nil
        message:[error localizedDescription]
        delegate:nil
        cancelButtonTitle:@"OK"
        otherButtonTitles:nil];
    [aView show];
}

@end
