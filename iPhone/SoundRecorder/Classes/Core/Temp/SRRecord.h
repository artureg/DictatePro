//
//  SRRecord.h
//  SoundRecorder
//
//  Created by Igor Danich

//

#import "SEModel.h"
#import "SRSound.h"

#import <AVFoundation/AVFoundation.h>

@class SRProject;

@interface SRRecord : SEModel

/* Record Info */
@property(nonatomic,weak) SRProject*        project;
@property(nonatomic,readonly) SRSound*      sound;
@property(nonatomic,strong) NSString*       soundPath;
@property(nonatomic,assign) SRSoundRange    timeRange;
@property(nonatomic,readonly) CMTimeRange   assetTimeRange;

@end
