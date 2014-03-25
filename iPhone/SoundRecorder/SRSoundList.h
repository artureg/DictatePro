//
//  SRSoundList.h
//  SoundRecorder
//
//  Created by Igor on 12/13/13.
//  Copyright (c) 2013 Igor. All rights reserved.
//

#import "SRSound.h"

@protocol SRSoundListDelegate;

@interface SRSoundList : NSObject

@property(nonatomic,weak) id<SRSoundListDelegate>   delegate;
@property(nonatomic,readonly) NSArray*              sounds;
@property(nonatomic,readonly) NSTimeInterval        duration;
@property(nonatomic,readonly) BOOL                  isPlaying;

- (void)addSound:(SRSound*)sound;
- (void)removeSound:(SRSound*)sound;
- (void)moveSound:(SRSound*)sound toIndex:(NSInteger)index;
- (void)splitSound:(SRSound*)sound time:(NSTimeInterval)time;

- (void)clearSound;
- (void)clearAll;
- (void)save;

- (void)play;
- (void)stop;

@end

@protocol SRSoundListDelegate<NSObject>
- (void)soundListDidStartPreparing:(SRSoundList*)sList;
- (void)soundListDidStartPlaying:(SRSoundList*)sList;
- (void)soundList:(SRSoundList*)sList playingTimer:(NSTimeInterval)time duration:(NSTimeInterval)duration;
- (void)soundListDidEndPlaying:(SRSoundList*)sList;
- (void)soundListDidBeginSplit:(SRSoundList*)sList;
- (void)soundListDidEndSplit:(SRSoundList*)sList sound1:(SRSound*)sound1 sound2:(SRSound*)sound2;
@end
