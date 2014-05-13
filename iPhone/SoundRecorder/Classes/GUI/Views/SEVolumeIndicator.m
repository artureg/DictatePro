//
//  SEVolumeIndicator.m
//  SoundRecorder
//
//  Created by Igor on 5/13/14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SEVolumeIndicator.h"

@interface SEVolumeIndicator()
@property(nonatomic,strong) UIImageView* pv_trackView;
- (void)pm_init;
- (void)pm_onDrag:(UILongPressGestureRecognizer*)gest;
@end

@implementation SEVolumeIndicator

- (void)pm_init {
    self.pv_trackView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"volume-cursor.png"]];
    self.pv_trackView.frame = CGRectMake(0, 0, self.pv_trackView.frame.size.width/2, self.pv_trackView.frame.size.height/2);
    [self addSubview:self.pv_trackView];
    
    UILongPressGestureRecognizer* gest = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(pm_onDrag:)];
    gest.minimumPressDuration = 0.01f;
    [self addGestureRecognizer:gest];
}

- (id)initWithCoder:(NSCoder*)aDecoder {
    if (self = [super initWithCoder:aDecoder]) {
        [self pm_init];
    }
    return self;
}

- (id)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self pm_init];
    }
    return self;
}

- (void)setValue:(CGFloat)value {
    _value = value;
    self.pv_trackView.center = CGPointMake(
        5 + value*(self.frame.size.width - 10),
        value*(self.frame.size.height - 8)
    );
    [self setNeedsDisplay];
}

- (void)drawRect:(CGRect)rect {
    [[UIImage imageNamed:@"volume_0.png"] drawInRect:rect];
    [[UIBezierPath bezierPathWithRect:CGRectMake(0, 0, 5 + self.value*(self.frame.size.width - 10), rect.size.height)] addClip];
    [[UIImage imageNamed:@"volume_1.png"] drawInRect:rect];
}

- (void)pm_onDrag:(UILongPressGestureRecognizer*)gest {
    CGPoint pos = [gest locationInView:self];
    CGFloat value = (pos.x - 5)/(self.frame.size.width - 10);
    if (value < 0) {
        value = 0;
    }
    if (value > 1) {
        value = 1;
    }
    self.value = value;
    [self sendActionsForControlEvents:UIControlEventValueChanged];
}

@end
