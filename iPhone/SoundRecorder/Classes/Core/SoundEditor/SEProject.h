//
//  SEProject.h
//  SoundRecorder
//
//  Created by Igor on 11.04.14.
//  Copyright (c) 2014 Wise-Apps. All rights reserved.
//

#import "SEModel.h"
#import "SEAudioStreamEngine.h"
#import "SERecord.h"

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
- (void)saveProject;

/** Build project and save in asynchronously (in another thread) */
- (void)saveProjectAsynchronouslyWithCompletion:(void(^)(NSError* error))completion;

@end
