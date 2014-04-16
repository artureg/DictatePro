//  SEAudioStreamEngine+Internal.h
//  SoundRecorder//  Created by Igor Danich igor.danich@wise-apps.com on 4/16/14.

#import "SEAudioStreamEngine.h"

@interface SEAudioStreamEngine (Internal)
/** Set state of engine */
 - (void)setState:(TSEAudioStreamEngineState)state;
@end
