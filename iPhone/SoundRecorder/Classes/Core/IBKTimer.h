//
//  IBKTimer.h
//  iCTA
//
//  Created by Igor on 21.11.12.
//  Copyright (c) 2012 PJ-Software. All rights reserved.
//

@class IBKTimer;

typedef void (^TIBKTimerBlock)(IBKTimer* aTimer);

@interface IBKTimer : NSObject
+ (IBKTimer*)timerWithTimeInterval:(NSTimeInterval)ti block:(TIBKTimerBlock)block;
+ (IBKTimer*)timerWithTimeInterval:(NSTimeInterval)ti repeats:(BOOL)yesOrNo block:(TIBKTimerBlock)block;
@end
