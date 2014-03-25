//
//  SRAddSoundViewController.h
//  SoundRecorder
//
//  Created by Igor on 12/13/13.
//  Copyright (c) 2013 Igor. All rights reserved.
//

#import <UIKit/UIKit.h>

@class SRSound;
@class SRSoundList;

@interface SRAddSoundViewController : UIViewController
@property(nonatomic,strong) SRSoundList*    soundList;
@property(nonatomic,strong) SRSound*        sound;
@end
