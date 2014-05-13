//
//  SEProgressScale.m
//  SoundRecorder
//
//  Created by Igor on 5/13/14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SEProgressIndicator.h"

@interface SEProgressIndicator()
@property(nonatomic,assign) NSTimeInterval  pv_duration;
@property(nonatomic,strong) UIImageView*    pv_progressView;
@property(nonatomic,strong) UIImageView*    pv_recordingView;
@property(nonatomic,strong) UIImageView*    pv_trackView;
@property(nonatomic,assign) BOOL            pv_isDragging;
- (void)pm_init;
- (void)pm_updateLayout;
- (void)pm_onDrag:(UILongPressGestureRecognizer*)press;
@end

@implementation SEProgressIndicator

- (void)pm_init {
    self.pv_progressView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"play_green-line.png"]];
    self.pv_progressView.frame = CGRectZero;
    self.pv_progressView.contentMode = UIViewContentModeScaleToFill;
    self.pv_progressView.layer.cornerRadius = 2;
    self.pv_progressView.backgroundColor = [UIColor greenColor];
    [self addSubview:self.pv_progressView];
    
    self.pv_recordingView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"play_red-line.png"]];
    self.pv_recordingView.frame = CGRectZero;
    self.pv_recordingView.contentMode = UIViewContentModeScaleToFill;
    self.pv_recordingView.layer.cornerRadius = 2;
    self.pv_recordingView.backgroundColor = [UIColor redColor];
    [self addSubview:self.pv_recordingView];
    
    self.pv_trackView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"play_red-cursor.png"]];
    self.pv_trackView.frame = CGRectMake(0, 0, self.pv_trackView.frame.size.width/2, self.pv_trackView.frame.size.height/2);
    [self addSubview:self.pv_trackView];
    self.pv_trackView.center = CGPointMake(0, self.frame.size.height/2);
    
    UILongPressGestureRecognizer* gest = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(pm_onDrag:)];
    gest.minimumPressDuration = 0.01f;
    [self addGestureRecognizer:gest];
}

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [self pm_init];
    }
    return self;
}

- (id)initWithCoder:(NSCoder*)aDecoder {
    if (self = [super initWithCoder:aDecoder]) {
        [self pm_init];
    }
    return self;
}

- (void)setValue:(NSTimeInterval)value {
    if (_value != value) {
        _value = value;
        [self pm_updateLayout];
    }
}

- (void)setRecordingDuration:(NSTimeInterval)recordingDuration {
    if (_recordingDuration != recordingDuration) {
        _recordingDuration = recordingDuration;
        [self pm_updateLayout];
    }
}

- (void)setDuration:(NSTimeInterval)duration {
    if (_duration != duration) {
        _duration = duration;
        [self pm_updateLayout];
    }
}

- (BOOL)isTracking {
    return self.pv_isDragging;
}

- (void)clear {
    self.pv_duration = 0;
    [self pm_updateLayout];
}

- (void)pm_updateLayout {
    if (self.duration >= self.pv_duration) {
        if (self.duration <= 120) {
            self.pv_duration = 120;
        } else if ((self.duration > 120)&&(self.duration <= 600)) {
            self.pv_duration = 600;
        } else if ((self.duration > 600)&&(self.duration <= 1200)) {
            self.pv_duration = 1200;
        } else if ((self.duration > 1200)&&(self.duration <= 2400)) {
            self.pv_duration = 2400;
        } else if ((self.duration > 2400)&&(self.duration <= 4800)) {
            self.pv_duration = 4800;
        } else {
            self.pv_duration = self.duration;
        }
    }
    self.pv_progressView.frame = CGRectMake(
        0,
        (self.frame.size.height - self.frame.size.height/4)/2,
        self.duration*self.frame.size.width/self.pv_duration,
        self.frame.size.height/4
    );
    self.pv_recordingView.frame = CGRectMake(
        self.value*self.frame.size.width/self.pv_duration,
        (self.frame.size.height - self.frame.size.height/4)/2,
        self.recordingDuration*self.frame.size.width/self.pv_duration,
        self.frame.size.height/4
    );
    self.pv_trackView.hidden = (self.recordingDuration > 0);
    self.pv_trackView.center = CGPointMake(self.value*self.frame.size.width/self.pv_duration, self.frame.size.height/2);
}

- (void)pm_onDrag:(UILongPressGestureRecognizer*)press {
    if (self.pv_duration == 0) {
        return;
    }
    CGPoint pos = [press locationInView:self];
    NSTimeInterval value = self.pv_duration*pos.x/self.frame.size.width;
    if (value > self.duration) {
        value = self.duration;
    }
    if (value < 0) {
        value = 0;
    }
    switch (press.state) {
        case UIGestureRecognizerStateBegan: {
            self.pv_isDragging = YES;
            self.userInteractionEnabled = NO;
        }break;
        case UIGestureRecognizerStateChanged: {
            self.pv_trackView.center = CGPointMake(value*self.frame.size.width/self.pv_duration, self.frame.size.height/2);
        }break;
        case UIGestureRecognizerStateCancelled:case UIGestureRecognizerStateEnded:case UIGestureRecognizerStateFailed: {
            self.value = value;
            self.pv_trackView.center = CGPointMake(value*self.frame.size.width/self.pv_duration, self.frame.size.height/2);
            self.pv_isDragging = NO;
            self.userInteractionEnabled = YES;
            [self sendActionsForControlEvents:UIControlEventValueChanged];
        }break;
        default:
            break;
    }
}

@end
