//  SEProject.h
//  SoundRecorder
//  Created by Igor Danich igor.danich@wise-apps.com on 11.04.14.


#import "SEModel.h"
#import "SEAudioStreamEngine.h"

@interface SEProject : SEModel

/** Project name */
@property(nonatomic,strong) NSString* name;

/** Check project if it is change (add or remove record affects that) */
@property(nonatomic,readonly) BOOL isChanged;

/** Initialize with folder of project */
- (instancetype)initWithFolder:(NSString*)folder;

/** Remove all data from project */
- (void)clearProject;

/** Build project and save */
- (NSError*)saveProject;

/** Build project and save in asynchronously (in another thread) */
- (void)saveProjectAsynchronouslyWithCompletion:(void(^)(NSError* error))completion;

@end
