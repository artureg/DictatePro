//
//  SRRecord.m
//  SoundRecorder
//
//  Created by Igor Danich

//

#import "SRRecord.h"
#import "SRProject.h"

@implementation SRRecord

- (void)updateToDictionary:(NSDictionary*)dictionary {
    [super updateToDictionary:dictionary];
    self.soundPath = dictionary[@"soundPath"];
    self.timeRange = SRSoundRangeMake([dictionary[@"start"] doubleValue], [dictionary[@"duration"] doubleValue]);
}

- (NSMutableDictionary*)dictionaryRepresentation {
    NSMutableDictionary* dict = [NSMutableDictionary dictionary];
    dict[@"soundPath"] = self.soundPath;
    dict[@"start"] = @(self.timeRange.start);
    dict[@"duration"] = @(self.timeRange.duration);
    return dict;
}

- (SRSound*)sound {
    SRSound* sound = [self.project soundWithPath:self.soundPath];
    sound.timeRange = self.timeRange;
    return sound;
}

- (CMTimeRange)assetTimeRange {
    return CMTimeRangeMake(CMTimeMake(self.timeRange.start*1000, 1000), CMTimeMake(self.timeRange.duration*1000, 1000));
}

@end
