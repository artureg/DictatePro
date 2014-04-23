//
//  IBKTimer.m
//  iCTA
//
//  Created by Igor on 21.11.12.
//  Copyright (c) 2012 PJ-Software. All rights reserved.
//

#import "IBKTimer.h"

@interface IBKTimer()
@property(nonatomic,strong) NSTimer* pv_timer;
@property(nonatomic,copy) TIBKTimerBlock pv_completion;
- (void)pm_onTimer:(NSDictionary*)userInfo;
- (id)pm_initWithTimeInterval:(NSTimeInterval)ti block:(TIBKTimerBlock)block repeats:(BOOL)yesOrNo;
@end

@implementation IBKTimer

+ (IBKTimer*)timerWithTimeInterval:(NSTimeInterval)ti block:(TIBKTimerBlock)block {
    return [self timerWithTimeInterval:ti repeats:NO block:block];
}

+ (IBKTimer*)timerWithTimeInterval:(NSTimeInterval)ti repeats:(BOOL)yesOrNo block:(TIBKTimerBlock)block {
    return [[self alloc] pm_initWithTimeInterval:ti block:block repeats:yesOrNo];
}

- (void)dealloc {
    [self.pv_timer invalidate];
    
}

- (id)pm_initWithTimeInterval:(NSTimeInterval)ti block:(TIBKTimerBlock)block repeats:(BOOL)yesOrNo {
    if (self == [super init]) {
        self.pv_completion = block;
        self.pv_timer = [NSTimer scheduledTimerWithTimeInterval:ti target:self selector:@selector(pm_onTimer:) userInfo:@{@"IBKTimer" : self} repeats:yesOrNo];
    }
    return self;
}

- (void)pm_onTimer:(NSDictionary*)userInfo {
    if (self.pv_completion) {
        self.pv_completion(self);
    }
}

@end
