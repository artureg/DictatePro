//
//  SEProgressScale.h
//  SoundRecorder
//
//  Created by Igor on 5/13/14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

@interface SEProgressIndicator : UIControl
@property(nonatomic,assign) NSTimeInterval  value;
@property(nonatomic,assign) NSTimeInterval  duration;
@property(nonatomic,assign) NSTimeInterval  recordingDuration;
@property(nonatomic,readonly) BOOL          isTracking;
- (void)clear;
@end
