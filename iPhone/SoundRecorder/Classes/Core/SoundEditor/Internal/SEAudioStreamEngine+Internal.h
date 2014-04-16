//
//  SEAudioStreamEngine+Internal.h
//  SoundRecorder
//
//  Created by Igor on 4/16/14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SEAudioStreamEngine.h"

@interface SEAudioStreamEngine (Internal)
/** Set state of engine */
 - (void)setState:(TSEAudioStreamEngineState)state;
@end
