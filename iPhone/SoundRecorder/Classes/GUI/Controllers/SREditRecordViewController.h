//
//  SRAddSoundViewController.h
//  SoundRecorder
//
//  Created by Igor Danich

//

#import <UIKit/UIKit.h>

@class SRProject;
@class SRRecord;

@interface SREditRecordViewController : UIViewController
@property(nonatomic,strong) SRProject*      project;
@property(nonatomic,strong) SRRecord*       record;
@end
