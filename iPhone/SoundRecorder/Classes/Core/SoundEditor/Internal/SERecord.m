//  SERecord.m
//  SoundRecorder//  Created by Igor Danich igor.danich@wise-apps.com on 11.04.14.

#import "SERecord.h"

#import "SERecordAudioStream.h"

@interface SERecord()
@property(nonatomic,strong) SERecordAudioStream* pv_stream;
@end

@implementation SERecord

- (SERecordAudioStream*)audioStream {
    if (!self.pv_stream) {
        self.pv_stream = [[SERecordAudioStream alloc] initWithRecord:self];
    }
    return self.pv_stream;
}

- (void)updateToDictionary:(NSDictionary*)dictionary {
    self.soundURL = [NSURL URLWithString:dictionary[@"soundPath"]];
    SERecordSoundRange range;
    range.start = [dictionary[@"start"] integerValue];
    range.duration = [dictionary[@"duration"] integerValue];
    self.soundRange = range;
}

- (NSMutableDictionary*)dictionaryRepresentation {
    NSMutableDictionary* dict = [super dictionaryRepresentation];
    if (self.soundURL) {
        dict[@"soundPath"] = [self.soundURL absoluteString];
    }
    dict[@"start"] = @(self.soundRange.start);
    dict[@"duration"] = @(self.soundRange.duration);
    return dict;
}

@end
